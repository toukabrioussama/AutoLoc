package tn.esprit.autoloc.autolacapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.autolacapi.domain.Reservation;

public interface IReservationRepository extends JpaRepository<Reservation, Long> {
}
