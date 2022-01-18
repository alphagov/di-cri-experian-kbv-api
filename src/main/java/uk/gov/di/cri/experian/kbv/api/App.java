package uk.gov.di.cri.experian.kbv.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

public class App {
    public static void main(String[] args) throws IOException {
        String keystoreBase64 = System.getenv("KEYSTORE");
        Path tempFile = Files.createTempFile(UUID.randomUUID().toString(), null);
        Files.write(tempFile, Base64.getDecoder().decode(keystoreBase64));
        System.setProperty("javax.net.ssl.keyStore", tempFile.toString());
        System.setProperty("javax.net.ssl.keyStorePassword", System.getenv("KEYSTORE_PASSWORD"));

        new ExperianApi();
    }
}
