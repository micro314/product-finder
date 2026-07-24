package com.rosati.productfinder.source.internal;

import com.rosati.productfinder.product.ProductSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogSourceConfigurationTest {
    @Test
    void createsAndRegistersAllThreeCatalogAdapters() {
        var properties = new CatalogProperties();
        configure(properties.getCatalogs().getNewegg(), "https://newegg.test", "newegg-key");
        configure(properties.getCatalogs().getBhPhotoVideo(), "https://bh.test", "bh-key");
        configure(properties.getCatalogs().getAbt(), "https://abt.test", "abt-key");
        var configuration = new CatalogSourceConfiguration();
        var newegg = configuration.neweggProductSource(properties);
        var bhPhotoVideo = configuration.bhPhotoVideoProductSource(properties);
        var abt = configuration.abtProductSource(properties);

        assertThat(configuration.productSources(List.of(newegg, bhPhotoVideo, abt)).all())
                .extracting(ProductSource::name).containsExactly("Newegg", "BHPhotoVideo", "Abt");
    }

    private void configure(CatalogProperties.Endpoint endpoint, String baseUrl, String apiKey) {
        endpoint.setBaseUrl(baseUrl);
        endpoint.setApiKey(apiKey);
    }
}
