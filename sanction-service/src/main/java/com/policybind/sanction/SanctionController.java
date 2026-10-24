package com.policybind.sanction;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class SanctionController {

    private final ScreenRepository screens;

    public SanctionController(ScreenRepository screens) {
        this.screens = screens;
    }

    @GetMapping("/screens/{quoteId}")
    public ScreenEntity screen(@PathVariable String quoteId) {
        return screens.findById(quoteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "screen not found"));
    }
}
