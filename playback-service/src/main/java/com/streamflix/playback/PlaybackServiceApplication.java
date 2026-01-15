package com.streamflix.playback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Streamflix Playback Service.
 *
 * Responsibilities:
 * - Track playback position for resume functionality
 * - Maintain watch history per profile
 * - Track device information for concurrent stream limits
 * - Emit playback events for analytics and recommendations
 *
 * Design Principles:
 * - Stateless service design
 * - High write throughput for progress updates
 * - Eventually consistent watch history
 *
 * @author Streamflix Engineering
 */
@SpringBootApplication
public class PlaybackServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlaybackServiceApplication.class, args);
    }
}
