package tn.esprit.autoloc.autolacapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.autolacapi.domain.Paiement;

public interface IPaiementRepository extends JpaRepository<Paiement, Long>  {
}
