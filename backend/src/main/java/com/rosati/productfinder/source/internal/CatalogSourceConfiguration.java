package com.rosati.productfinder.source.internal;

import com.rosati.productfinder.product.ProductSource;
import com.rosati.productfinder.product.ProductSources;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CatalogProperties.class)
class CatalogSourceConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "product-finder.catalogs.newegg", name = "enabled", havingValue = "true")
    NeweggProductSource neweggProductSource(CatalogProperties properties) {
        return new NeweggProductSource(client(properties, properties.getCatalogs().getNewegg(), "X-Newegg-Api-Key"));
    }

    @Bean
    @ConditionalOnProperty(prefix = "product-finder.catalogs.bh-photo-video", name = "enabled", havingValue = "true")
    BhPhotoVideoProductSource bhPhotoVideoProductSource(CatalogProperties properties) {
        return new BhPhotoVideoProductSource(client(properties, properties.getCatalogs().getBhPhotoVideo(), "Authorization"));
    }

    @Bean
    @ConditionalOnProperty(prefix = "product-finder.catalogs.abt", name = "enabled", havingValue = "true")
    AbtProductSource abtProductSource(CatalogProperties properties) {
        return new AbtProductSource(client(properties, properties.getCatalogs().getAbt(), "X-Abt-Api-Key"));
    }

    @Bean
    ProductSources productSources(List<ProductSource> sources) {
        List<ProductSource> immutableSources = List.copyOf(sources);
        return () -> immutableSources;
    }

    private RestClient client(CatalogProperties properties, CatalogProperties.Endpoint endpoint, String header) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        RestClient.Builder builder = RestClient.builder().baseUrl(endpoint.getBaseUrl()).requestFactory(requestFactory);
        if (endpoint.getApiKey() != null && !endpoint.getApiKey().isBlank()) {
            builder.defaultHeader(header, authorizationValue(header, endpoint.getApiKey()));
        }
        return builder.build();
    }

    private String authorizationValue(String header, String apiKey) {
        return "Authorization".equals(header) ? "Bearer " + apiKey : apiKey;
    }
}
