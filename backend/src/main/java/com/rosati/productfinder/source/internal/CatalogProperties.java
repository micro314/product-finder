package com.rosati.productfinder.source.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("product-finder")
class CatalogProperties {
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(5);
    private int fullRefreshLimit = 10_000;
    private Catalogs catalogs = new Catalogs();

    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
    public int getFullRefreshLimit() { return fullRefreshLimit; }
    public void setFullRefreshLimit(int fullRefreshLimit) { this.fullRefreshLimit = fullRefreshLimit; }
    public Catalogs getCatalogs() { return catalogs; }
    public void setCatalogs(Catalogs catalogs) { this.catalogs = catalogs == null ? new Catalogs() : catalogs; }

    static class Catalogs {
        private Endpoint newegg = new Endpoint();
        private Endpoint bhPhotoVideo = new Endpoint();
        private Endpoint abt = new Endpoint();

        public Endpoint getNewegg() { return newegg; }
        public void setNewegg(Endpoint newegg) { this.newegg = newegg; }
        public Endpoint getBhPhotoVideo() { return bhPhotoVideo; }
        public void setBhPhotoVideo(Endpoint bhPhotoVideo) { this.bhPhotoVideo = bhPhotoVideo; }
        public Endpoint getAbt() { return abt; }
        public void setAbt(Endpoint abt) { this.abt = abt; }
    }

    static class Endpoint {
        private boolean enabled;
        private String baseUrl;
        private String apiKey;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }
}
