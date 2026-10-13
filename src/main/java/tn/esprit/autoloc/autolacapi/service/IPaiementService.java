package tn.esprit.autoloc.autolacapi.service;

import java.util.List;
import tn.esprit.autoloc.autolacapi.domain.Paiement;

/**
 * Service en lecture seule : la création, la modification et la suppression
 * des paiements passent par le Contrat (cascade).
 */
public interface IPaiementService {

    Paiement findById(Long id);

    List<Paiement> findAll();
}
