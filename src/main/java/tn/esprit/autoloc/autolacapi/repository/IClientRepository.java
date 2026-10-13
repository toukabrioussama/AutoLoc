package tn.esprit.autoloc.autolacapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.autolacapi.domain.Client;

public interface IClientRepository extends JpaRepository<Client, Long> {
}
