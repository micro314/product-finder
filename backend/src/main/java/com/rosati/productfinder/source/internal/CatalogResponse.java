package com.rosati.productfinder.source.internal;

import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.net.URI;

final class CatalogResponse {
    private CatalogResponse() {
    }

    static JsonNode requiredArray(JsonNode response, String pointer, String catalog) {
        if (response == null || !response.at(pointer).isArray()) {
            throw new IllegalArgumentException(catalog + " response is missing " + pointer);
        }
        return response.at(pointer);
    }

    static String required(JsonNode item, String pointer, String catalog) {
        String value = text(item, pointer);
        if (value.isBlank()) throw new IllegalArgumentException(catalog + " product is missing " + pointer);
        return value;
    }

    static String text(JsonNode item, String pointer) {
        JsonNode value = item.at(pointer);
        return value.isMissingNode() || value.isNull() ? "" : value.asText();
    }

    static BigDecimal decimal(JsonNode item, String pointer) {
        String value = text(item, pointer);
        return value.isBlank() ? null : new BigDecimal(value);
    }

    static int integer(JsonNode item, String pointer) {
        return Integer.parseInt(required(item, pointer, "Catalog"));
    }

    static URI uri(JsonNode item, String pointer) {
        String value = text(item, pointer);
        return value.isBlank() ? null : URI.create(value);
    }
}
