package io.github.shanastasia06.userservice;

import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class UserApiTest {
    private static final String PASSWORD = "StrongP@ss123";

    @Autowired
    private MockMvc mockMvc;

    // helper methods
    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private ResultActions register(String email, String password) throws Exception {
        String body = """
                {"email": "%s", "password": "%s", "firstName": "Anna", "lastName": "Smith"}
                """.formatted(email, password);

        return mockMvc.perform(post("/api/v1/auth/register")
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(body));
    }

    private ResultActions login(String email, String password) throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(body));
    }

    private String registerAndLogin(String email) throws Exception {
        register(email, PASSWORD).andExpect(status().isCreated());
        String response = login(email, PASSWORD)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    @Test
    void register_validRequest_returns201WithoutSensitiveFields()  throws Exception {
        String email = uniqueEmail();

        register(email, PASSWORD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.roles").doesNotExist());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD).andExpect(status().isCreated());
        register(email, PASSWORD).andDo(print())
                .andExpect(status().isConflict());;
    }

    @Test
    void register_invalidInput_returns400() throws Exception {
        register("not-an-email", "short").andExpect(status().isBadRequest());
    }

    @Test
    void login_correctCredentials_returnsBearerToken() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD).andExpect(status().isCreated());

        login(email, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_wrongPasswordAndUnknownEmail_returnSame401() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD).andExpect(status().isCreated());

        String wrongPassword = login(email, "WrongP@ss123")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String unknownEmail = login(uniqueEmail(), PASSWORD)
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(JsonPath.<String>read(wrongPassword, "$.message"))
                .isEqualTo(JsonPath.<String>read(unknownEmail, "$.message"));
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withWrongToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withTamperedToken_returns401() throws Exception {
        String token = registerAndLogin(uniqueEmail());

        String[] parts = token.split("\\.");
        char first = parts[1].charAt(0);
        parts[1] = (first == 'A' ? 'B' : 'A') + parts[1].substring(1);
        String tampered = String.join(".", parts);

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withValidToken_returnsOwnProfile() throws Exception {
        String email = uniqueEmail();
        String token = registerAndLogin(email);

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstName").value("Anna"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void patchMe_onlyFirstname_keepsLastName() throws Exception {
        String token = registerAndLogin(uniqueEmail());

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("{\"firstName\":\"Johnny\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void jwks_exposesOnlyThePublicKey() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].kid").isNotEmpty())
                .andExpect(jsonPath("$.keys[0].n").isNotEmpty())
                .andExpect(jsonPath("$.keys[0].e").isNotEmpty())
                .andExpect(jsonPath("$.keys[0].d").doesNotExist())
                .andExpect(jsonPath("$.keys[0].p").doesNotExist())
                .andExpect(jsonPath("$.keys[0].q").doesNotExist());
    }

    @Test
    void issuedToken_isVerifiableWithPublishedKeyAndCarriesClaims() throws Exception {
        String token = registerAndLogin(uniqueEmail());

        String jwksJson = mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        SignedJWT jwt = SignedJWT.parse(token);
        JWK key = JWKSet.parse(jwksJson).getKeyByKeyId(jwt.getHeader().getKeyID());

        assertThat(key).isNotNull();
        assertThat(jwt.verify(new RSASSAVerifier(key.toRSAKey()))).isTrue();
        assertThat(jwt.getJWTClaimsSet().getStringListClaim("roles")).asList().containsExactly("USER");
        assertThat(jwt.getJWTClaimsSet().getSubject()).isNotBlank();
    }
}
