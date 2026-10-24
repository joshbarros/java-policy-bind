package com.policybind.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.Channels;
import com.policybind.common.ProductOpened;
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
public class ProductController {

    private final ProductRepository products;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;

    public ProductController(ProductRepository products, OutboxRepository outbox, ObjectMapper mapper) {
        this.products = products;
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @PostMapping("/products")
    @Transactional
    public ResponseEntity<ProductEntity> create(@RequestBody CreateProduct request) {
        if (request == null || blank(request.code()) || request.rateCents() == null || request.rateCents() <= 0
                || request.termDays() == null || request.termDays() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "product fields are required");
        }
        if (products.existsById(request.code().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "product exists");
        }
        ProductEntity row = new ProductEntity();
        row.setCode(request.code().trim());
        row.setRateCents(request.rateCents());
        row.setTermDays(request.termDays());
        products.save(row);
        OutboxWriter.kafka(outbox, mapper, Channels.PRODUCTS, row.getCode(),
                new ProductOpened(row.getCode(), row.getRateCents(), row.getTermDays()));
        return ResponseEntity.status(HttpStatus.CREATED).body(row);
    }

    @GetMapping("/products/{code}")
    public ProductEntity product(@PathVariable String code) {
        return products.findById(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateProduct(String code, Integer rateCents, Integer termDays) {
    }
}
