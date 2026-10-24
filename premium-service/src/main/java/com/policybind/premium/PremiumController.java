package com.policybind.premium;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PremiumController {

    private final PremiumRepository premiums;

    public PremiumController(PremiumRepository premiums) {
        this.premiums = premiums;
    }

    @GetMapping("/premiums/{quoteId}")
    public PremiumEntity premium(@PathVariable String quoteId) {
        return premiums.findById(quoteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "premium not found"));
    }
}
