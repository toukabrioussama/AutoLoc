package tn.esprit.autoloc.autolacapi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.esprit.autoloc.autolacapi.domain.Contrat;
import tn.esprit.autoloc.autolacapi.domain.ModePaiement;
import tn.esprit.autoloc.autolacapi.domain.Paiement;
import tn.esprit.autoloc.autolacapi.domain.Reservation;
import tn.esprit.autoloc.autolacapi.exception.ResourceNotFoundException;
import tn.esprit.autoloc.autolacapi.repository.IContratRepository;
import tn.esprit.autoloc.autolacapi.repository.IReservationRepository;
import tn.esprit.autoloc.autolacapi.service.location.legacy.SmsGateway;
import tn.esprit.autoloc.autolacapi.service.location.legacy.SmtpClient;

/**
 * Confirmation des réservations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationManager {

    private static final BigDecimal REMISE_PARTICULIER_LONGUE_DUREE = new BigDecimal("0.90");
    private static final BigDecimal REMISE_ENTREPRISE = new BigDecimal("0.85");
    private static final BigDecimal REMISE_VIP = new BigDecimal("0.80");
    private static final BigDecimal REMISE_CODE_BIENVENUE = new BigDecimal("20");
    private static final BigDecimal FRAIS_CARTE = new BigDecimal("1.01"); // 1 %
    private static final long SEUIL_LONGUE_DUREE = 7;

    private static final String SMTP_HOST = "smtp.autoloc.tn";
    private static final int SMTP_PORT = 25;
    private static final String SMS_URL = "https://sms.autoloc.tn";

    private final IReservationRepository reservationRepository;
    private final IContratRepository contratRepository;

    @Transactional
    public Contrat confirmerReservation(Long reservationId, String modePaiement,
                                        String codePromo, String canal) {

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));

        // 1. disponibilité
        verifierDisponibilite(r);

        // 2. tarif de base
        long jours = ChronoUnit.DAYS.between(r.getDateDebut(), r.getDateFin()) + 1;
        BigDecimal montant = r.getVehicule().getTarifJournalier()
                .multiply(BigDecimal.valueOf(jours));

        // 3. remises
        montant = appliquerRemises(montant, typeClient(r), jours, codePromo);

        // 4. paiement
        ModePaiement mode = lireModePaiement(modePaiement);
        if (mode == ModePaiement.CARTE) {
            montant = montant.multiply(FRAIS_CARTE);
        }
        montant = montant.setScale(2, RoundingMode.HALF_UP);
        log.info("Réservation {} : paiement {} de {}", reservationId, mode, montant);

        Paiement p = new Paiement();
        p.setDatePaiement(LocalDate.now());
        p.setModePaiement(mode);
        p.setMontant(montant);

        // 5. contrat (les paiements sont persistés en cascade)
        Contrat c = new Contrat();
        c.setDateSignature(LocalDate.now());
        c.setMontantTotal(montant);
        c.setValide(true);
        c.getPaiements().add(p);
        p.setContrat(c);
        Contrat res = contratRepository.save(c);

        // 6. notification, envoyée seulement si la transaction est validée
        notifierApresCommit(canal, r, res);

        return res;
    }

    /**
     * Type du client sous forme de texte : "PARTICULIER", "ENTREPRISE" ou "VIP".
     * ADAPTEZ cette seule ligne au nom réel dans votre entité Client :
     *  - champ String      : return r.getClient().getTypeClient();
     *  - champ enum        : return r.getClient().getTypeClient().name();
     */
    private String typeClient(Reservation r) {
        return r.getClient().getTypeClient().name();
    }

    private void verifierDisponibilite(Reservation r) {
        if (r.getDateDebut() == null || r.getDateFin() == null
                || r.getDateFin().isBefore(r.getDateDebut())) {
            throw new IllegalArgumentException("Dates de réservation invalides");
        }
        boolean chevauchement = reservationRepository.existsChevauchement(
                r.getVehicule().getIdVehicule(),
                r.getIdReservation(),
                r.getDateDebut(),
                r.getDateFin());
        if (chevauchement) {
            throw new IllegalStateException("Véhicule indisponible sur cette période");
        }
    }

    private BigDecimal appliquerRemises(BigDecimal montant, String typeClient,
                                        long jours, String codePromo) {
        if (typeClient != null) {
            switch (typeClient) {
                case "PARTICULIER" -> {
                    if (jours >= SEUIL_LONGUE_DUREE) {
                        montant = montant.multiply(REMISE_PARTICULIER_LONGUE_DUREE);
                    }
                }
                case "ENTREPRISE" -> montant = montant.multiply(REMISE_ENTREPRISE);
                case "VIP" -> montant = montant.multiply(REMISE_VIP);
                default -> { /* aucune remise */ }
            }
        }
        if ("BIENVENUE".equals(codePromo)) {
            montant = montant.subtract(REMISE_CODE_BIENVENUE).max(BigDecimal.ZERO);
        }
        return montant;
    }

    private ModePaiement lireModePaiement(String modePaiement) {
        if (modePaiement == null) {
            throw new IllegalArgumentException("Mode de paiement obligatoire");
        }
        try {
            return ModePaiement.valueOf(modePaiement);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Mode de paiement inconnu : " + modePaiement);
        }
    }

    /** Planifie l'envoi après le commit : pas de notification pour un contrat annulé. */
    private void notifierApresCommit(String canal, Reservation r, Contrat contrat) {
        // Valeurs lues maintenant, pour ne pas toucher aux entités après la transaction
        final String email = r.getClient().getEmail();
        final String telephone = r.getClient().getTelephone();
        final String message = "Contrat " + contrat.getIdContrat() + " confirmé";

        Runnable envoi = () -> envoyer(canal, email, telephone, message);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            envoi.run();
                        }
                    });
        } else {
            envoi.run();
        }
    }

    private void envoyer(String canal, String email, String telephone, String message) {
        try {
            if ("EMAIL".equals(canal)) {
                new SmtpClient(SMTP_HOST, SMTP_PORT).send(email, message);
            } else if ("SMS".equals(canal)) {
                new SmsGateway(SMS_URL).send(telephone, message);
            } else {
                log.warn("Canal de notification inconnu ou absent : {}", canal);
            }
        } catch (Exception e) {
            // Un échec d'envoi ne doit pas remettre en cause le contrat déjà enregistré
            log.error("Échec de la notification : {}", message, e);
        }
    }
}