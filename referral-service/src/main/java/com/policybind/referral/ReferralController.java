package com.policybind.referral;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ReferralController {

    private final DecisionRepository decisions;

    public ReferralController(DecisionRepository decisions) {
        this.decisions = decisions;
    }

    @GetMapping("/decisions/{quoteId}")
    public DecisionEntity decision(@PathVariable String quoteId) {
        return decisions.findById(quoteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "decision not found"));
    }
}
