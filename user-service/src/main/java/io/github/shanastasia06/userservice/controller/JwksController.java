package io.github.shanastasia06.userservice.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwksController {
    private final RSAKey rsaKey;

    @GetMapping("/.well-known/jwks.json")
    public Map<String,Object> jwks(){
        // returns the copy with the private part removed
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }
}
