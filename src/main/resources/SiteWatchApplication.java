package com.sitewatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SiteWatchApplication
 *
 * Main entry point for the SiteWatch application.
 * Monitors configured websites for new or changed content matching user-defined rules.
 *
 * @author Infinity Iron
 */
@SpringBootApplication
@EnableScheduling
public class SiteWatchApplication {

    /**
     * Application entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SiteWatchApplication.class, args);
    }
}
