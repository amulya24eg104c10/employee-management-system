package com.ems.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;
import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthStore store;
    private final String[] origins;

    public WebConfig(AuthStore store, @Value("${app.cors.allowed-origins}") String allowed) {
        this.store = store;
        this.origins = Arrays.stream(allowed.split(","))
                .map(s -> s.trim().replaceAll("/+$", ""))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/api/**").allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry reg) {
        reg.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object h) throws Exception {
                if ("OPTIONS".equals(req.getMethod())) return true;
                String auth = req.getHeader("Authorization");
                String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
                if (store.valid(token)) return true;
                res.setStatus(401);
                res.setContentType("application/json");
                res.getWriter().write("{\"message\":\"Unauthorized\"}");
                return false;
            }
        }).addPathPatterns("/api/**").excludePathPatterns("/api/auth/login", "/api/health");
    }
}
