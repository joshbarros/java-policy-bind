package com.policybind.commission;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CommissionController {

    private final CommissionRepository commissions;

    public CommissionController(CommissionRepository commissions) {
        this.commissions = commissions;
    }

    @GetMapping("/commissions/{quoteId}")
    public CommissionEntity commission(@PathVariable String quoteId) {
        return commissions.findById(quoteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "commission not found"));
    }
}
