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

## 2. Anomalies SonarQube for IDE

Analyse initiale du projet (24 fichiers Java) : 0 issue.
Après ajout du programme de démonstration (DemoRunner), 3 anomalies détectées :

| Anomalie | Règle / explication | Correction apportée |
|---|---|---|
| `LocalDate.now()` sans fuseau (setDateSignature) | Règle <code lu dans SonarLint> : le résultat dépend du fuseau de la machine | `LocalDate.now(ZoneId.of("Africa/Tunis"))` |
| `LocalDate.now()` sans fuseau (setDatePaiement) | Même règle | Même correction, avec une constante `ZONE` |
| Littéral `"Nombre de paiements en base : {}"` dupliqué 3 fois | Règle <code lu dans SonarLint> : littéral dupliqué, à remplacer par une constante | Constante `MSG_NB_PAIEMENTS` |

Ces anomalies concernaient uniquement le code de démonstration, supprimé avant le commit.