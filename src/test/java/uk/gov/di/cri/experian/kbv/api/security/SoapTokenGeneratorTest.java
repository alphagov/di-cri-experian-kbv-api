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

    private Base64TokenEncoder base64TokenEncoder;

    @Test
    void shouldReturnAGeneratedBase64EncodedTokenWhenGivenASoapTokenService() {
        base64TokenEncoder = new Base64TokenEncoder("token");

        assertEquals(
                Base64.getEncoder().encodeToString("token".getBytes(StandardCharsets.UTF_8)),
                base64TokenEncoder.getToken());
    }

    @Test
    void shouldThrowAnExceptionWhenGivenSoapTokenServiceIsNotValid() {
        base64TokenEncoder = new Base64TokenEncoder(null);

        NullPointerException soapTokenNullRefException =
                assertThrows(NullPointerException.class, () -> base64TokenEncoder.getToken());

        assertEquals("The input must not be null", soapTokenNullRefException.getMessage());
        ;
    }

    @Test
    void shouldThrowAnRuntimeExceptionWhenGivenSoapTokenServiceIsNotValid() {
        base64TokenEncoder = new Base64TokenEncoder("Error");

        assertThrows(RuntimeException.class, () -> base64TokenEncoder.getToken());
    }
}
