package com.rosati.productfinder.search.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchResponseTest {
    @Test
    void normalizesNullCollectionsAndFailureText() {
        var response = new SearchResponse(null, null);
        var failure = new SourceFailure(" ", null);
        assertThat(response.products()).isEmpty();
        assertThat(response.failures()).isEmpty();
        assertThat(failure).isEqualTo(new SourceFailure("unknown", "Remote search failed"));
    }
}
