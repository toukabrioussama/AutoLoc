package tn.esprit.autoloc.autolacapi.service;

import java.util.List;
import tn.esprit.autoloc.autolacapi.domain.Vehicule;

public interface IVehiculeService {

    Vehicule create(Vehicule vehicule);

    Vehicule findById(Long id);

    List<Vehicule> findAll();

    Vehicule update(Long id, Vehicule vehicule);

    void deleteById(Long id);

    Vehicule assignAgence(Long idVehicule, Long idAgence);

    Vehicule assignEquipement(Long idVehicule, Long idEquipement);

    Vehicule unassignEquipement(Long idVehicule);

    /**
     * Crée le véhicule et ses équipements en cascade,
     * puis l'affecte à l'agence donnée.
     */
    Vehicule addAndAssignAgence(Vehicule vehicule, Long idAgence);
}