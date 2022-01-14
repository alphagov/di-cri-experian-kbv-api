package uk.gov.di.cri.experian.kbv.api.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SoapTokenGeneratorTest {

    private SoapTokenGenerator soapTokenGenerator;

    @Test
    void shouldReturnAGeneratedBase64EncodedTokenWhenGivenASoapTokenService() {
        soapTokenGenerator = new SoapTokenGenerator("token");

        assertEquals(
                Base64.getEncoder().encodeToString("token".getBytes(StandardCharsets.UTF_8)),
                soapTokenGenerator.getToken());
    }

    @Test
    void shouldThrowAnExceptionWhenGivenSoapTokenServiceIsNotValid() {
        soapTokenGenerator = new SoapTokenGenerator(null);

        NullPointerException soapTokenNullRefException =
                assertThrows(NullPointerException.class, () -> soapTokenGenerator.getToken());

        assertEquals("The input must not be null", soapTokenNullRefException.getMessage());
        ;
    }

    @Test
    void shouldThrowAnRuntimeExceptionWhenGivenSoapTokenServiceIsNotValid() {
        soapTokenGenerator = new SoapTokenGenerator("Error");

        assertThrows(RuntimeException.class, () -> soapTokenGenerator.getToken());
    }
}
