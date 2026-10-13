## A. Modes d'injection comparés

| Critère | Constructeur | Attribut |
|---|---|---|
| Champ `final` possible ? | Oui | Non |
| Dépendance visible dans la signature de la classe ? | Oui (paramètre du constructeur) | Non (champ privé caché) |
| Utilisable avec `new` hors conteneur sans dépendance `null` ? | Oui : impossible de créer l'objet sans fournir le repository | Non : le champ reste `null`, NullPointerException à l'appel |
| Signalé par SonarQube for IDE ? | Non | Oui (S6813, à vérifier dans le panneau) |

## B. Services

| Service | Dépendance injectée | Mode | Justification |
|---|---|---|---|
| ContratServiceImpl | IContratRepository | Constructeur | Dépendance obligatoire, champ `final`, typée par interface ; service sans état |
| AgenceServiceImpl | IAgenceRepository | Constructeur | Idem |
| EmployeServiceImpl | IEmployeRepository | Constructeur | Idem |
| VehiculeServiceImpl | IVehiculeRepository | Constructeur | Idem |
| EquipementServiceImpl | IEquipementRepository | Constructeur | Idem |
| ClientServiceImpl | IClientRepository | Constructeur | Idem |
| ReservationServiceImpl | IReservationRepository | Constructeur | Idem |
| PaiementServiceImpl | IPaiementRepository | Constructeur | Idem ; lecture seule (findById, findAll) : création et modification passent par le Contrat |
| MaintenanceServiceImpl | IMaintenanceRepository | Constructeur | Idem |

## C. Messages d'erreur du conteneur

### Message 1
- **Exception** : `NoSuchBeanDefinitionException` (enveloppée dans `UnsatisfiedDependencyException`).
- **Bean concerné** : `IContratService`, demandé par le constructeur de `ContratController`.
- **Causes probables côté `ContratServiceImpl`** :
    1. l'annotation `@Service` est oubliée, donc la classe n'est pas un bean ;
    2. la classe est placée hors du package racine `tn.esprit.autoloc` : le scan ne la détecte pas.
- **Correction** : ajouter `@Service` et vérifier que le package est sous la racine scannée.

### Message 2
- **Exception** : `NoUniqueBeanDefinitionException`.
- **Beans concernés** : `emailNotificateur` et `smsNotificateur`, demandés par `NotificationController`.
- **Cause** : deux beans du même type, Spring ne sait pas lequel choisir.
- **Correction 1** : `@Primary` sur la **classe** du bean à utiliser par défaut (ex. `EmailNotificateur`).
- **Correction 2** : `@Qualifier("emailNotificateur")` sur le **paramètre du constructeur** du contrôleur.

### Message 3
- **Exception** : `BeanCurrentlyInCreationException` (dépendance circulaire).
- **Beans concernés** : `clientServiceImpl` et `reservationServiceImpl`.
- **Pourquoi c'est impossible à contourner avec le constructeur** : pour créer A, il faut déjà B, et pour créer B, il faut déjà A. Aucun des deux ne peut être instancié en premier.
- **Piste de conception** : extraire la responsabilité commune dans un troisième service dont les deux dépendent, pour que la dépendance devienne à sens unique.

E. Questions de compréhension

1. Où est le new de ContratServiceImpl et qui l’exécute ?
   Dans notre code, il n’y en a aucun. C’est le conteneur Spring (ApplicationContext, créé par SpringApplication.run(...)) qui l’exécute : il détecte la classe grâce à @Service et au scan des composants, puis appelle son constructeur en lui fournissant le proxy de IContratRepository.

2. Pourquoi un contrôleur dépendra-t-il de IContratService et non de ContratServiceImpl ?
   Pour dépendre d’une abstraction et non d’une implémentation. On peut ainsi changer d’implémentation ou fournir une doublure de test sans modifier le contrôleur, ce qui réduit le couplage entre les couches. Cela respecte la règle : chaque couche dépend de l’interface de la couche inférieure.

3. Pourquoi un service singleton doit-il rester sans état ? Exemple de bug.
   Le conteneur ne crée qu’une seule instance, partagée par toutes les requêtes (donc tous les threads). Un attribut modifiable propre à un appel est alors écrasé par les autres requêtes.
   Exemple de bug : un attribut private Contrat contratCourant; dans ContratServiceImpl. Deux requêtes simultanées l’écrasent mutuellement : l’une peut modifier ou renvoyer le contrat de l’autre.

4. Pourquoi update charge-t-il l’entité existante avant save ?
   Passer directement l’objet reçu à save écraserait toutes les colonnes : les attributs non fournis seraient mis à null. Avec orphanRemoval, cela pourrait aussi vider la collection de paiements. On charge donc l’entité gérée, on recopie uniquement les attributs modifiables (sans toucher à l’identifiant ni aux collections), puis on sauvegarde.

5. Différences entre les notions

@Component / @Bean : @Component se pose sur une classe qu’on possède, et elle est détectée par le scan. @Bean se pose sur une méthode d’une classe @Configuration : on l’utilise pour une classe qu’on ne peut pas annoter (bibliothèque, JDK) ou pour une construction particulière.
@Primary / @Qualifier : @Primary se pose sur la classe du bean et le désigne comme candidat par défaut. @Qualifier("nomDuBean") se pose à l’injection et fait un choix explicite.
IPaiementService en lecture seule : les paiements sont créés, modifiés et supprimés via le Contrat (cascade). Les exposer séparément contournerait cette règle de gestion et pourrait créer des paiements sans contrat cohérent.