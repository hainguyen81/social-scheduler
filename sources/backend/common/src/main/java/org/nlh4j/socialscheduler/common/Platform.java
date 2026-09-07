/**
 * Enum representing the supported social media platforms for scheduling and content generation.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.common;

import lombok.Getter;

@Getter
public enum Platform {
	GENERAL("General Social Media"),
    FACEBOOK("Facebook Graph API"),
    INSTAGRAM("Instagram Graph API"),
    TIKTOK("TikTok Open API"),
    OPENAI("OpenAI API");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name identifier for this platform, used in API requests and prompt contexts.
     * @return String display name for the social media platform.
     */
    public String getDisplayName() {
        return displayName;
    }
}