package tn.esprit.autoloc.autolacapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.autolacapi.domain.Agence;

public interface IAgenceRepository extends JpaRepository<Agence, Long> {
}