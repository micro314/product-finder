package com.rosati.productfinder.search.internal;

record SourceFailure(String source, String message) {
    SourceFailure {
        source = source == null || source.isBlank() ? "unknown" : source;
        message = message == null || message.isBlank() ? "Remote search failed" : message;
    }
}
