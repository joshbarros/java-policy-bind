package com.policybind.endorsement;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class EndorsementController {

    private final EndorsementRepository endorsements;

    public EndorsementController(EndorsementRepository endorsements) {
        this.endorsements = endorsements;
    }

    @GetMapping("/endorsements/{quoteId}")
    public EndorsementEntity endorsement(@PathVariable String quoteId) {
        return endorsements.findById(quoteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "endorsement not found"));
    }
}
