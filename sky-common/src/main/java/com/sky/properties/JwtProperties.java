package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.jwt")
@Data
public class JwtProperties {

    /**
     * Configuration for generating JWT tokens for admin employees
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    /**
     * Configuration for generating JWT tokens for user微信用户
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

}
