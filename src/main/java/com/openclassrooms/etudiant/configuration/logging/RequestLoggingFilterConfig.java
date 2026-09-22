package com.openclassrooms.etudiant.configuration.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {
    @Bean
    public CommonsRequestLoggingFilter commonsRequestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();

        filter.setIncludeQueryString(false);
        filter.setIncludePayload(false);
        filter.setIncludeHeaders(false);

        filter.setAfterMessagePrefix("REQUEST: ");

        return filter;
    }
}
