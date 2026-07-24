package com.rosati.productfinder.source.internal;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogPropertiesTest {
    @Test
    void storesTimeoutsAndEachCatalogEndpoint() {
        var properties = new CatalogProperties();
        properties.setConnectTimeout(Duration.ofSeconds(3));
        properties.setReadTimeout(Duration.ofSeconds(8));
        var catalogs = new CatalogProperties.Catalogs();
        var newegg = new CatalogProperties.Endpoint();
        newegg.setEnabled(true);
        newegg.setBaseUrl("https://newegg.test");
        newegg.setApiKey("key");
        catalogs.setNewegg(newegg);
        catalogs.setBhPhotoVideo(new CatalogProperties.Endpoint());
        catalogs.setAbt(new CatalogProperties.Endpoint());
        properties.setCatalogs(catalogs);

        assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(properties.getReadTimeout()).isEqualTo(Duration.ofSeconds(8));
        assertThat(properties.getCatalogs().getNewegg().getBaseUrl()).isEqualTo("https://newegg.test");
        assertThat(properties.getCatalogs().getNewegg().isEnabled()).isTrue();
        assertThat(properties.getCatalogs().getNewegg().getApiKey()).isEqualTo("key");
        assertThat(properties.getCatalogs().getBhPhotoVideo()).isNotNull();
        assertThat(properties.getCatalogs().getAbt()).isNotNull();
        properties.setCatalogs(null);
        assertThat(properties.getCatalogs()).isNotNull();
    }
}
