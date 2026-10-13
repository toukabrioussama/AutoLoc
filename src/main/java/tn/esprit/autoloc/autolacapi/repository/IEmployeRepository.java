package tn.esprit.autoloc.autolacapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.autolacapi.domain.Employe;

public interface IEmployeRepository extends JpaRepository<Employe, Long> {
}
