package uk.gov.di.cri.experian.kbv.api;

public class App {
    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.keyStore", System.getenv("KEYSTORE_PATH"));
        System.setProperty("javax.net.ssl.keyStorePassword", System.getenv("KEYSTORE_PASSWORD"));

        new ExperianApi();
    }
}
