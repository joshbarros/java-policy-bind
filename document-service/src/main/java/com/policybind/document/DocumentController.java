package com.policybind.document;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class DocumentController {

    private final DocumentRepository documents;

    public DocumentController(DocumentRepository documents) {
        this.documents = documents;
    }

    @GetMapping("/documents/{quoteId}")
    public DocumentEntity document(@PathVariable String quoteId) {
        return documents.findById(quoteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "document not found"));
    }
}
