package uk.gov.di.cri.experian.kbv.api.config;

public class KbvApiConfig implements ApiConfig {
    private final String keystorePath;
    private final String keystorePassword;
    private final String endpointUri;

    public KbvApiConfig() {
        this.keystorePath = System.getenv("KEYSTORE_PATH");
        this.keystorePassword = System.getenv("KEYSTORE_PASSWORD");
        this.endpointUri = System.getenv("ENDPOINT_URI");
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public String getEndpointUri() {
        return endpointUri;
    }
}
