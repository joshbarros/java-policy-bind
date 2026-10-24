package com.policybind.quote;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.CapacityReturnCommand;
import com.policybind.common.Channels;
import com.policybind.common.CommissionReverseCommand;
import com.policybind.common.EndorseCommand;
import com.policybind.common.PolicyRules;
import com.policybind.common.PremiumReturnCommand;
import com.policybind.common.QuoteFact;
import com.policybind.common.RateCommand;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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
public class QuoteController {

    private final QuoteRepository quotes;
    private final AccountDirectoryRepository accounts;
    private final ProductDirectoryRepository products;
    private final TreatyDirectoryRepository treaties;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public QuoteController(QuoteRepository quotes, AccountDirectoryRepository accounts,
            ProductDirectoryRepository products, TreatyDirectoryRepository treaties, OutboxRepository outbox,
            ObjectMapper mapper, EntityManager em) {
        this.quotes = quotes;
        this.accounts = accounts;
        this.products = products;
        this.treaties = treaties;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @PostMapping("/quotes")
    @Transactional
    public ResponseEntity<QuoteEntity> create(@RequestBody CreateQuote request) {
        if (request == null || blank(request.requestId()) || blank(request.accountId()) || blank(request.productCode())
                || blank(request.treatyCode()) || blank(request.approverToken()) || blank(request.formToken())
                || blank(request.premiumToken()) || request.units() == null || request.units() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quote fields are required");
        }
        QuoteEntity existing = quotes.findByRequestId(request.requestId().trim()).orElse(null);
        if (existing != null) {
            return ResponseEntity.ok(existing);
        }
        AccountDirectory account = accounts.findById(request.accountId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "account not found"));
        ProductDirectory product = products.findById(request.productCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        if (!treaties.existsById(request.treatyCode().trim())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "treaty not found");
        }
        QuoteEntity row = new QuoteEntity();
        row.setId(UUID.randomUUID().toString());
        row.setRequestId(request.requestId().trim());
        row.setAccountId(account.getAccountId());
        row.setName(account.getName());
        row.setEmail(account.getEmail());
        row.setProductCode(product.getCode());
        row.setTreatyCode(request.treatyCode().trim());
        row.setUnits(request.units());
        row.setRateCents(product.getRateCents());
        row.setTermDays(product.getTermDays());
        row.setApproverToken(request.approverToken().trim());
        row.setFormToken(request.formToken().trim());
        row.setPremiumToken(request.premiumToken().trim());
        row.setPremiumCents(0);
        row.setReferred(false);
        row.setPolicyNumber("");
        row.setCommissionCents(0);
        row.setReturnCents(0);
        row.setElapsedDays(0);
        row.setStatus("DRAFT");
        row.setCapacityStatus("NONE");
        row.setReason("");
        quotes.save(row);
        OutboxWriter.rabbit(outbox, mapper, Channels.RATE, row.getId(),
                new RateCommand(row.getId(), row.getRateCents(), row.getUnits(), row.getTermDays()));
        return ResponseEntity.status(HttpStatus.CREATED).body(row);
    }

    @PostMapping("/quotes/{id}/cancel")
    @Transactional
    public QuoteEntity cancel(@PathVariable String id, @RequestBody CancelQuote request) {
        QuoteEntity row = em.find(QuoteEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (row == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "quote not found");
        }
        if (request == null || request.elapsedDays() == null || request.elapsedDays() < 0
                || request.elapsedDays() > row.getTermDays()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "elapsed days are outside the term");
        }
        if (!"IN_FORCE".equals(row.getStatus()) || !"COMMITTED".equals(row.getCapacityStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "quote is not in force");
        }
        int returned = PolicyRules.proRataReturn(row.getPremiumCents(), row.getTermDays(), request.elapsedDays());
        row.setStatus("CANCELLED");
        row.setReason("CANCEL");
        row.setReturnCents(returned);
        row.setElapsedDays(request.elapsedDays());
        OutboxWriter.kafka(outbox, mapper, Channels.QUOTES, row.getId(), new QuoteFact(row.getId(), "CANCELLED", "CANCEL"));
        OutboxWriter.rabbit(outbox, mapper, Channels.RETURN_CAPACITY, row.getId(),
                new CapacityReturnCommand(row.getId(), row.getTreatyCode(), returned));
        OutboxWriter.rabbit(outbox, mapper, Channels.PREMIUM_RETURN, row.getId(),
                new PremiumReturnCommand(row.getId(), returned));
        OutboxWriter.rabbit(outbox, mapper, Channels.REVERSE, row.getId(),
                new CommissionReverseCommand(row.getId(), PolicyRules.commission(returned)));
        OutboxWriter.rabbit(outbox, mapper, Channels.ENDORSE, row.getId(),
                new EndorseCommand(row.getId(), row.getPremiumCents(), returned, request.elapsedDays()));
        return row;
    }

    @GetMapping("/quotes/{id}")
    public QuoteEntity quote(@PathVariable String id) {
        return quotes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "quote not found"));
    }

    @GetMapping("/directory/{accountId}")
    public AccountDirectory directory(@PathVariable String accountId) {
        return accounts.findById(accountId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "account not found"));
    }

    @GetMapping("/products/{code}")
    public ProductDirectory product(@PathVariable String code) {
        return products.findById(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
    }

    @GetMapping("/treaties/{code}")
    public TreatyDirectory treaty(@PathVariable String code) {
        return treaties.findById(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "treaty not found"));
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateQuote(String requestId, String accountId, String productCode, String treatyCode, Integer units,
            String approverToken, String formToken, String premiumToken) {
    }

    public record CancelQuote(Integer elapsedDays) {
    }
}
