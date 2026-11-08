package com.fn_tracking.fitnessapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI fitnessOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fitness & Nutrition Tracking API")
                        .version("1.0.0")
                        .description("Theo dõi chỉ số sức khỏe, dinh dưỡng hằng ngày và tạo thực đơn bằng AI"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
