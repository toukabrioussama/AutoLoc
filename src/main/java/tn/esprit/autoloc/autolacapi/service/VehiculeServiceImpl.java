package tn.esprit.autoloc.autolacapi.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tn.esprit.autoloc.autolacapi.domain.Agence;
import tn.esprit.autoloc.autolacapi.domain.Equipement;
import tn.esprit.autoloc.autolacapi.domain.Vehicule;
import tn.esprit.autoloc.autolacapi.exception.ResourceNotFoundException;
import tn.esprit.autoloc.autolacapi.repository.IAgenceRepository;
import tn.esprit.autoloc.autolacapi.repository.IEquipementRepository;
import tn.esprit.autoloc.autolacapi.repository.IVehiculeRepository;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements IVehiculeService {

    private final IVehiculeRepository vehiculeRepository;
    private final IAgenceRepository agenceRepository;
    private final IEquipementRepository equipementRepository;

    @Override
    public Vehicule create(Vehicule vehicule) {
        if (vehicule.getIdVehicule() != null) {
            throw new IllegalArgumentException(
                    "Un nouveau véhicule ne doit pas avoir d'identifiant");
        }
        verifier(vehicule);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule findById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    @Override
    public List<Vehicule> findAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    public Vehicule update(Long id, Vehicule vehicule) {
        Vehicule existant = findById(id);
        verifier(vehicule);
        existant.setTarifJournalier(vehicule.getTarifJournalier());
        // recopier ici les autres attributs modifiables de Vehicule (Atelier 2)
        return vehiculeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicule", id);
        }
        vehiculeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Vehicule assignAgence(Long idVehicule, Long idAgence) {
        Vehicule vehicule = findById(idVehicule);
        Agence agence = agenceRepository.findById(idAgence)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", idAgence));
        vehicule.setAgence(agence);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    @Transactional
    public Vehicule assignEquipement(Long idVehicule, Long idEquipement) {
        Vehicule vehicule = findById(idVehicule);
        Equipement equipement = equipementRepository.findById(idEquipement)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement", idEquipement));
        vehicule.getEquipements().add(equipement); // Vehicule est le côté propriétaire
        return vehiculeRepository.save(vehicule);
    }

    @Override
    @Transactional
    public Vehicule unassignEquipement(Long idVehicule) {
        Vehicule vehicule = findById(idVehicule);
        // L'interface ne reçoit que l'id du véhicule : on retire tous ses équipements
        vehicule.getEquipements().clear();
        return vehiculeRepository.save(vehicule);
    }

    @Override
    @Transactional
    public Vehicule addAndAssignAgence(Vehicule vehicule, Long idAgence) {
        if (vehicule.getIdVehicule() != null) {
            throw new IllegalArgumentException(
                    "Un nouveau véhicule ne doit pas avoir d'identifiant");
        }
        verifier(vehicule);

        Agence agence = agenceRepository.findById(idAgence)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", idAgence));

        // Les équipements doivent être nouveaux : ils sont persistés en cascade
        if (vehicule.getEquipements() != null) {
            for (Equipement e : vehicule.getEquipements()) {
                if (e.getIdEquipement() != null) {
                    throw new IllegalArgumentException(
                            "Un nouvel équipement ne doit pas avoir d'identifiant");
                }
            }
        }

        vehicule.setAgence(agence);
        return vehiculeRepository.save(vehicule); // véhicule + équipements + lignes vehicule_equipement
    }

    private void verifier(Vehicule vehicule) {
        if (vehicule.getTarifJournalier() != null
                && vehicule.getTarifJournalier().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le tarif journalier ne peut pas être négatif");
        }
    }
}