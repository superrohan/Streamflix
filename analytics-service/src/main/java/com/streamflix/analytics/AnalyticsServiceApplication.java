package com.streamflix.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Streamflix Analytics Service.
 *
 * Responsibilities:
 * - Consume and aggregate viewing events
 * - Generate real-time metrics for dashboards
 * - Store historical analytics data
 * - Expose APIs for internal analytics dashboards
 *
 * @author Streamflix Engineering
 */
@SpringBootApplication
@EnableScheduling
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
