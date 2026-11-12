package tn.esprit.autoloc.autolacapi.web.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.autoloc.autolacapi.domain.Paiement;
import tn.esprit.autoloc.autolacapi.service.IPaiementService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("Paiements")
public class PaiementRestController {
    private final IPaiementService paiementService;

    @GetMapping
    public List<Paiement> getAll() {
        return paiementService.findAll();
    }

    @GetMapping("/{id}")
    public Paiement getPaiementById(@PathVariable Long id) {
        return paiementService.findById(id);
    }
}
