package com.policybind.rating;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
public class RatingController {

    private final RatingRepository ratings;

    public RatingController(RatingRepository ratings) {
        this.ratings = ratings;
    }

    @GetMapping("/ratings/{quoteId}")
    public RatingEntity rating(@PathVariable String quoteId) {
        return ratings.findById(quoteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "rating not found"));
    }
}
