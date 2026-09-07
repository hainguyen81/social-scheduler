/**
 * Enum representing the tone/mode for AI-generated social media content recommendations.
 * Used to influence prompt engineering and content style in the ai-service layer.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.aiservice.dto.enums;

import lombok.Getter;

@Getter
public enum Tone {
    PROFESSIONAL("Professional"),
    CASUAL("Casual"),
    HUMOROUS("Humorous"),
    INSPIRATIONAL("Inspirational");

    private final String displayName;

    Tone(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name for this tone, to be injected into OpenAI prompt templates.
     * @return String prompt modifier describing the content tone.
     */
    public String getPromptModifier() {
        return switch (this) {
            case PROFESSIONAL -> "professional, formal and business-oriented";
            case CASUAL -> "casual, conversational and friendly";
            case HUMOROUS -> "humorous, light-hearted and witty";
            case INSPIRATIONAL -> "inspirational, motivational and uplifting";
        };
    }
}
