package com.policybind.capacity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.Channels;
import com.policybind.common.TreatyOpened;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CapacityController {

    private final TreatyRepository treaties;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;

    public CapacityController(TreatyRepository treaties, OutboxRepository outbox, ObjectMapper mapper) {
        this.treaties = treaties;
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @PostMapping("/treaties")
    @Transactional
    public ResponseEntity<TreatyEntity> create(@RequestBody CreateTreaty request) {
        if (request == null || blank(request.code()) || request.limitCents() == null || request.limitCents() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "treaty fields are required");
        }
        if (treaties.existsById(request.code().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "treaty exists");
        }
        TreatyEntity row = new TreatyEntity();
        row.setCode(request.code().trim());
        row.setLimitCents(request.limitCents());
        row.setHeldCents(0);
        row.setBoundCents(0);
        treaties.save(row);
        OutboxWriter.kafka(outbox, mapper, Channels.TREATIES, row.getCode(),
                new TreatyOpened(row.getCode(), row.getLimitCents()));
        return ResponseEntity.status(HttpStatus.CREATED).body(row);
    }

    @GetMapping("/treaties/{code}")
    public TreatyEntity treaty(@PathVariable String code) {
        return treaties.findById(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "treaty not found"));
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateTreaty(String code, Integer limitCents) {
    }
}
