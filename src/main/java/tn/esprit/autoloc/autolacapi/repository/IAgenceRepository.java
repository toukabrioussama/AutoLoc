package tn.esprit.autoloc.autolacapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.autolacapi.domain.Vehicule;

public interface IAgenceRepository extends JpaRepository<Vehicule, Long> {
}
