package tn.esprit.autoloc.autolacapi.service;

import java.util.List;
import tn.esprit.autoloc.autolacapi.domain.Agence;

public interface IAgenceService {

    Agence create(Agence agence);

    Agence findById(Long id);

    List<Agence> findAll();

    Agence update(Long id, Agence agence);

    void deleteById(Long id);
}