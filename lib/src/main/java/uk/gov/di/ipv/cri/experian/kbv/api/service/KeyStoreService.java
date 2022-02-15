package uk.gov.di.ipv.cri.experian.kbv.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class KeyStoreService {
    private final SecretsProvider secretsProvider;
    public static final String KBV_API_KEYSTORE = "/dev/di-ipv-cri-experian-kbv-api/keystore";
    public static final String KBV_API_KEYSTORE_PASSWORD = "/dev/di-ipv-cri-experian-kbv-api/keystore-password";
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreService.class);

    public KeyStoreService(SecretsProvider secretsProvider) {
        this.secretsProvider = secretsProvider;
    }

    public String getValue() {
        try {
            String keystoreBase64 = secretsProvider.get(KBV_API_KEYSTORE);
            Path tempFile = Files.createTempFile(null, null);
            Files.write(tempFile, Base64.getDecoder().decode(keystoreBase64));
            return tempFile.toString();
        } catch (IOException e) {
            LOGGER.error("Initialisation failed", e);
            return null;
        }
    }

    public String getPassword() {
       return secretsProvider.get(KBV_API_KEYSTORE_PASSWORD);
    }
}
