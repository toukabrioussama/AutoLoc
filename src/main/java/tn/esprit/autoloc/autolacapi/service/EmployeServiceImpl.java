package tn.esprit.autoloc.autolacapi.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.autolacapi.domain.Employe;
import tn.esprit.autoloc.autolacapi.exception.ResourceNotFoundException;
import tn.esprit.autoloc.autolacapi.repository.IEmployeRepository;

@Service
@RequiredArgsConstructor
public class EmployeServiceImpl implements IEmployeService {

    private final IEmployeRepository employeRepository;

    @Override
    public Employe create(Employe employe) {
        if (employe.getIdEmploye() != null) {
            throw new IllegalArgumentException(
                    "Un nouvel employé ne doit pas avoir d'identifiant");
        }
        verifier(employe);
        return employeRepository.save(employe);
    }

    @Override
    public Employe findById(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", id));
    }

    @Override
    public List<Employe> findAll() {
        return employeRepository.findAll();
    }

    @Override
    public Employe update(Long id, Employe employe) {
        Employe existant = findById(id);
        verifier(employe);
        existant.setNom(employe.getNom());
        existant.setRole(employe.getRole());
        // recopier les autres attributs modifiables (Atelier 2)
        return employeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employe", id);
        }
        employeRepository.deleteById(id);
    }

    private void verifier(Employe employe) {
        if (employe.getNom() == null || employe.getNom().isBlank()
                || employe.getRole() == null) {
            throw new IllegalArgumentException("Le nom et le rôle de l'employé sont obligatoires");
        }
    }
}