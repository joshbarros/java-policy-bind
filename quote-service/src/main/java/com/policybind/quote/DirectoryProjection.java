package com.policybind.quote;

import com.policybind.common.AccountRegistered;
import com.policybind.common.Channels;
import com.policybind.common.ProductOpened;
import com.policybind.common.TreatyOpened;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DirectoryProjection {

    private final AccountDirectoryRepository accounts;
    private final ProductDirectoryRepository products;
    private final TreatyDirectoryRepository treaties;

    public DirectoryProjection(AccountDirectoryRepository accounts, ProductDirectoryRepository products,
            TreatyDirectoryRepository treaties) {
        this.accounts = accounts;
        this.products = products;
        this.treaties = treaties;
    }

    @KafkaListener(topics = Channels.ACCOUNTS, groupId = "quote-accounts")
    @Transactional
    public void onAccount(AccountRegistered event) {
        if (accounts.existsById(event.accountId())) {
            return;
        }
        AccountDirectory row = new AccountDirectory();
        row.setAccountId(event.accountId());
        row.setName(event.name());
        row.setEmail(event.email());
        accounts.save(row);
    }

    @KafkaListener(topics = Channels.PRODUCTS, groupId = "quote-products")
    @Transactional
    public void onProduct(ProductOpened event) {
        if (products.existsById(event.productCode())) {
            return;
        }
        ProductDirectory row = new ProductDirectory();
        row.setCode(event.productCode());
        row.setRateCents(event.rateCents());
        row.setTermDays(event.termDays());
        products.save(row);
    }

    @KafkaListener(topics = Channels.TREATIES, groupId = "quote-treaties")
    @Transactional
    public void onTreaty(TreatyOpened event) {
        if (treaties.existsById(event.treatyCode())) {
            return;
        }
        TreatyDirectory row = new TreatyDirectory();
        row.setCode(event.treatyCode());
        treaties.save(row);
    }
}
