package tn.esprit.autoloc.autolacapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.autolacapi.domain.Contrat;

public interface IContratRepository extends JpaRepository<Contrat, Long> {
}
