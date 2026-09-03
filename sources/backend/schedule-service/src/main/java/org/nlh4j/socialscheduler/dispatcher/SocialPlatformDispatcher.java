package org.nlh4j.socialscheduler.dispatcher;

import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.nlh4j.socialscheduler.scheduleservice.exception.SocialPlatformException;
import org.nlh4j.socialscheduler.scheduleservice.integration.FacebookClient;
import org.nlh4j.socialscheduler.scheduleservice.integration.InstagramClient;
import org.nlh4j.socialscheduler.scheduleservice.integration.TikTokClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * SocialPlatformDispatcher acts as a facade to route publishing requests to the appropriate 
 * social media platform client based on the platform type defined in the schedule entity.
 * 
 * @traceability [REQ-001], [EXC-001], [EXC-002]
 */
@Component
@RequiredArgsConstructor
public class SocialPlatformDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(SocialPlatformDispatcher.class);

    private final FacebookClient facebookClient;
    private final InstagramClient instagramClient;
    private final TikTokClient tikTokClient;

    /**
     * Dispatches the publishing task to the specific platform client.
     * 
     * @param entity The schedule entity containing content and platform metadata.
     * @throws SocialPlatformException if the platform is unsupported or the client fails.
     */
    public void dispatchPublish(ScheduleEntity entity) {
        String platform = entity.getPlatform();
        logger.info("[PROCESS] Dispatching publish request for Schedule ID: {} to Platform: {}", 
                     entity.getScheduleId(), platform);

        try {
            switch (platform.toUpperCase()) {
                case "FACEBOOK":
                    facebookClient.publish(entity);
                    break;
                case "INSTAGRAM":
                    instagramClient.publish(entity);
                    break;
                case "TIKTOK":
                    tikTokClient.publish(entity);
                    break;
                default:
                    logger.error("[CRITICAL FAIL] [EXC-001] Unsupported platform: {}", platform);
                    throw new SocialPlatformException("Unsupported platform: " + platform);
            }
            logger.info("[PROCESS] Successfully dispatched publish for Schedule ID: {}", entity.getScheduleId());
        } catch (Exception e) {
            // Exception Cause Chain Preservation Law: Forwarding original exception context
            logger.error("[CRITICAL FAIL] [EXC-001] Dispatch failed for Schedule ID: {}. Raw error: {}", 
                         entity.getScheduleId(), e.getMessage());
            throw new SocialPlatformException("Failed to dispatch to " + platform, e);
        }
    }
}