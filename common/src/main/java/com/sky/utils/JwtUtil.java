package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * Generate jwt
     * Use Hs256 algorithm, private key using fixed secret key
     *
     * @param secretKey jwt secret key
     * @param ttlMillis jwt expiration time (milliseconds)
     * @param claims    set information
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // Specify the signature algorithm used when signing, that is, the header part
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // Generate JWT time
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // Set jwt body
        JwtBuilder builder = Jwts.builder()
                // If there is a private declaration, you must set this private declaration first, this is to give the builder's claim value, once written in the standard declaration value, it is covered by those standard declarations
                .setClaims(claims)
                // Set the signature algorithm and secret key used for signing
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                // Set expiration time
                .setExpiration(exp);

        return builder.compact();
    }

    /**
     * Token decryption
     *
     * @param secretKey jwt secret key This secret key must be kept on the server, cannot be exposed, otherwise sign can be forged, if multiple clients are connected, it is recommended to change to multiple
     * @param token     encrypted token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        // Get DefaultJwtParser
        Claims claims = Jwts.parser()
                // Set the secret key for signing
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                // Set the jwt to be parsed
                .parseClaimsJws(token).getBody();
        return claims;
    }

}
