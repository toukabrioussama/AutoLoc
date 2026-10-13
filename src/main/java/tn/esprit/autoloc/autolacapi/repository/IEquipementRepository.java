package tn.esprit.autoloc.autolacapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.autolacapi.domain.Equipement;

public interface IEquipementRepository extends JpaRepository<Equipement, Long> {
}
