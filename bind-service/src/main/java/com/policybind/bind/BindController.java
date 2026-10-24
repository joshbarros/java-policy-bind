package com.policybind.bind;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class BindController {

    private final PolicyRepository policies;

    public BindController(PolicyRepository policies) {
        this.policies = policies;
    }

    @GetMapping("/policies/{quoteId}")
    public PolicyEntity policy(@PathVariable String quoteId) {
        return policies.findById(quoteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "policy not found"));
    }
}
