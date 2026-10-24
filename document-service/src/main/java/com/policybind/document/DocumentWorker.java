package com.policybind.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.Channels;
import com.policybind.common.DocumentCommand;
import com.policybind.common.DocumentEvent;
import com.policybind.common.DocumentVoidCommand;
import com.policybind.common.PolicyRules;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class DocumentWorker {

    private final DocumentRepository documents;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;
    private final TransactionTemplate tx;

    public DocumentWorker(DocumentRepository documents, OutboxRepository outbox, ObjectMapper mapper,
            EntityManager em, PlatformTransactionManager manager) {
        this.documents = documents;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
        this.tx = new TransactionTemplate(manager);
    }

    @RabbitListener(queues = Channels.DOCUMENT)
    public void onIssue(DocumentCommand command) {
        Boolean dead = tx.execute(status -> {
            if (!InboxClaims.claim(em, command.quoteId() + ":document")) {
                return false;
            }
            if (PolicyRules.brokenForm(command.formToken())) {
                publish(command.quoteId(), "FAILED");
                return true;
            }
            DocumentEntity row = new DocumentEntity();
            row.setQuoteId(command.quoteId());
            row.setFormToken(command.formToken());
            row.setStatus("ISSUED");
            documents.save(row);
            publish(command.quoteId(), "ISSUED");
            return false;
        });
        if (Boolean.TRUE.equals(dead)) {
            throw new AmqpRejectAndDontRequeueException("form broken");
        }
    }

    @RabbitListener(queues = Channels.DOCUMENT_VOID)
    @Transactional
    public void onVoid(DocumentVoidCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":document-void")) {
            return;
        }
        int updated = em.createNativeQuery("UPDATE documents SET status = 'VOID' WHERE quote_id = :id AND status = 'ISSUED'")
                .setParameter("id", command.quoteId())
                .executeUpdate();
        if (updated != 1) {
            throw new IllegalStateException("document void missed " + command.quoteId());
        }
        publish(command.quoteId(), "VOID");
    }

    private void publish(String quoteId, String status) {
        OutboxWriter.kafka(outbox, mapper, Channels.DOCUMENTS, quoteId, new DocumentEvent(quoteId, status));
    }
}
