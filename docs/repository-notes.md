# Atelier 3 : Couche Repository

## 1. Choix d'interface
Toutes les interfaces étendent `JpaRepository<Entité, Long>` (CRUD, List, tri, pagination, flush).

| Interface | Étend | Justification |
|---|---|---|
| IAgenceRepository | JpaRepository<Agence, Long> | CRUD complet, List, tri et pagination. |
| IEmployeRepository | JpaRepository<Employe, Long> | CRUD complet, tri et pagination. |
| IVehiculeRepository | JpaRepository<Vehicule, Long> | Catalogue : List, tri par catégorie ou statut, pagination. |
| IEquipementRepository | JpaRepository<Equipement, Long> | CRUD complet, cohérence de la couche. |
| IClientRepository | JpaRepository<Client, Long> | Pagination et tri, saveAndFlush disponible. |
| IReservationRepository | JpaRepository<Reservation, Long> | CRUD complet, tri par date. |
| IContratRepository | JpaRepository<Contrat, Long> | CRUD complet, List, saveAndFlush. Éviter deleteAllInBatch (cascade et orphanRemoval contournés). |
| IPaiementRepository | JpaRepository<Paiement, Long> | Lecture des paiements ; création et suppression via le Contrat (composition). |
| IMaintenanceRepository | JpaRepository<Maintenance, Long> | CRUD complet, tri chronologique. |

