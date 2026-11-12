package tn.esprit.autoloc.autolacapi.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tn.esprit.autoloc.autolacapi.domain.Reservation;

public interface IReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Vrai si une autre réservation du même véhicule chevauche la période donnée.
     * Deux périodes se chevauchent si debut1 <= fin2 et fin1 >= debut2.
     */
    @Query("""
            select count(x) > 0 from Reservation x
            where x.vehicule.idVehicule = :idVehicule
              and x.idReservation <> :idReservation
              and x.dateDebut <= :dateFin
              and x.dateFin >= :dateDebut
            """)
    boolean existsChevauchement(@Param("idVehicule") Long idVehicule,
                                @Param("idReservation") Long idReservation,
                                @Param("dateDebut") LocalDate dateDebut,
                                @Param("dateFin") LocalDate dateFin);
}
