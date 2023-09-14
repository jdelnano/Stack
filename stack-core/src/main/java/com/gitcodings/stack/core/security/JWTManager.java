package com.gitcodings.stack.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ClassNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Manager class for JWT
 */
public class JWTManager
{
    public static final JWSAlgorithm   JWS_ALGORITHM = JWSAlgorithm.ES256;
    public static final JOSEObjectType JWS_TYPE      = JOSEObjectType.JWT;

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_ID    = "id";

    private static final Set<String> REQUIRED_CLAIMS =
        new HashSet<>(
            Arrays.asList(
                JWTClaimNames.EXPIRATION_TIME,
                JWTClaimNames.SUBJECT,
                JWTClaimNames.ISSUED_AT,
                CLAIM_ROLES,
                CLAIM_ID)
        );

    private final ECKey ecKey;

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    private final JWSSigner   signer;
    private final JWSVerifier verifier;

    private final Duration accessTokenExpire;
    private final Duration refreshTokenExpire;
    private final Duration maxRefreshTokenLifeTime;

    private JWTManager(Builder builder)
        throws ClassNotFoundException, IOException, ParseException, JOSEException
    {
        this.ecKey = buildECKey(builder.keyFileName);
        this.signer = new ECDSASigner(ecKey);
        this.verifier = new ECDSAVerifier(ecKey);
        this.jwtProcessor = buildClaimsSetVerifier();

        this.accessTokenExpire = builder.accessTokenExpire;
        this.refreshTokenExpire = builder.refreshTokenExpire;
        this.maxRefreshTokenLifeTime = builder.maxRefreshTokenLifeTime;
    }

    public Duration getAccessTokenExpire()
    {
        return accessTokenExpire;
    }

    public Duration getRefreshTokenExpire()
    {
        return refreshTokenExpire;
    }

    public Duration getMaxRefreshTokenLifeTime()
    {
        return maxRefreshTokenLifeTime;
    }

    public ECKey getEcKey()
    {
        return ecKey;
    }

    public ConfigurableJWTProcessor<SecurityContext> getJwtProcessor()
    {
        return jwtProcessor;
    }

    public JWSSigner getSigner()
    {
        return signer;
    }

    public JWSVerifier getVerifier()
    {
        return verifier;
    }

    private ECKey buildECKey(String fileName) throws ClassNotFoundException, FileNotFoundException, IOException, ParseException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        // Class cls = Class.forName("JWTManager");

        // // returns the ClassLoader object associated with this Class
        // ClassLoader cLoader = cls.getClassLoader();
        //// Get the InputStream for the file
        //InputStream inputStream = cLoader.getResourceAsStream(fileName);
        
        // Check if the file exists
        if (inputStream == null) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        // Read the InputStream and convert it to a Map<String, Object>
        Map<String, Object> jsonObject;
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            jsonObject = new ObjectMapper().readerForMapOf(Object.class).readValue(reader);
    }

        // Parse the JSON object using ECKey.parse() (assuming it's a valid method)
        return ECKey.parse(jsonObject);
    }

    private ConfigurableJWTProcessor<SecurityContext> buildClaimsSetVerifier()
    {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        jwtProcessor.setJWSTypeVerifier(
            new DefaultJOSEObjectTypeVerifier<>(JWS_TYPE)
        );

        jwtProcessor.setJWSKeySelector(
            new JWSVerificationKeySelector<>(
                JWS_ALGORITHM,
                new ImmutableJWKSet<>(new JWKSet(ecKey))
            )
        );

        jwtProcessor.setJWTClaimsSetVerifier(
            new DefaultJWTClaimsVerifier<>(
                null,
                null,
                REQUIRED_CLAIMS,
                null)
        );

        return jwtProcessor;
    }

    public static class Builder
    {
        private String   keyFileName;
        private Duration accessTokenExpire;
        private Duration refreshTokenExpire;
        private Duration maxRefreshTokenLifeTime;

        public Builder keyFileName(String keyFileName)
        {
            this.keyFileName = Objects.requireNonNull(keyFileName);
            return this;
        }

        public Builder accessTokenExpire(Duration accessTokenExpire)
        {
            this.accessTokenExpire = Objects.requireNonNull(accessTokenExpire);
            return this;
        }

        public Builder refreshTokenExpire(Duration refreshTokenExpire)
        {
            this.refreshTokenExpire = Objects.requireNonNull(refreshTokenExpire);
            return this;
        }

        public Builder maxRefreshTokenLifeTime(Duration maxRefreshTokenLifeTime)
        {
            this.maxRefreshTokenLifeTime = Objects.requireNonNull(maxRefreshTokenLifeTime);
            return this;
        }

        public JWTManager build()
        {
            Objects.requireNonNull(
                keyFileName,
                "keyFileName Cannot be null");
            Objects.requireNonNull(
                accessTokenExpire,
                "accessTokenExpire Cannot be null");
            Objects.requireNonNull(
                refreshTokenExpire,
                "refreshTokenExpire Cannot be null");
            Objects.requireNonNull(
                maxRefreshTokenLifeTime,
                "maxRefreshTokenLifeTime Cannot be null");

            try {
                return new JWTManager(this);
            } catch (ClassNotFoundException | IOException | ParseException | JOSEException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
