package org.nlh4j.socialscheduler.aiservice.fallback;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.nlh4j.socialscheduler.aiservice.dto.RecommendationRequestDto;
import org.nlh4j.socialscheduler.aiservice.dto.RecommendationResponseDto;
import org.nlh4j.socialscheduler.aiservice.exception.FallbackContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DefaultContentFallback provides safe fallback content generation when AI service is unavailable.
 * Implements static template mapping by platform and tone for marketing content.
 * 
 * @traceability [REQ-002], [EXC-004]
 */
@Component
public class DefaultContentFallback {

    private static final Logger log = LoggerFactory.getLogger(DefaultContentFallback.class);

    /**
     * Static map of fallback templates keyed by "{platform}_{tone}".
     * Each template is 200-300 characters of platform-appropriate marketing content.
     */
    private static final Map<String, String> FALLBACK_TEMPLATES = new HashMap<>();

    static {
        // Facebook templates
        FALLBACK_TEMPLATES.put("FACEBOOK_PROFESSIONAL",
                "Discover how our innovative solutions transform business operations with measurable ROI. "
                + "Join thousands of satisfied clients who have streamlined workflows and increased productivity. "
                + "Learn more about our award-winning services today!");
        FALLBACK_TEMPLATES.put("FACEBOOK_CASUAL",
                "Hey there! 👋 We've got something awesome to share that'll make your day better. "
                + "Check out what our community is loving right now - it's practical, fun, and totally worth your time!");
        FALLBACK_TEMPLATES.put("FACEBOOK_HUMOROUS",
                "Warning: May cause sudden outbreaks of productivity and joy. Side effects include smiling at your screen "
                + "and telling colleagues about this amazing find. Consult your team if symptoms persist!");
        FALLBACK_TEMPLATES.put("FACEBOOK_INSPIRATIONAL",
                "Every great achievement starts with a single step toward improvement. "
                + "What will you accomplish today with tools designed to elevate your potential? "
                + "The journey to excellence begins now.");

        // Instagram templates
        FALLBACK_TEMPLATES.put("INSTAGRAM_PROFESSIONAL",
                "✨ Elevate your brand presence with strategic insights that drive real engagement. "
                + "Swipe to see how leading businesses transform challenges into growth opportunities. "
                + "#BusinessGrowth #Innovation #Leadership");
        FALLBACK_TEMPLATES.put("INSTAGRAM_CASUAL",
                "Just dropped something you'll actually want to save! 💫 "
                + "Simple, effective, and made for real life. Double tap if you're feeling inspired! ✨");
        FALLBACK_TEMPLATES.put("INSTAGRAM_HUMOROUS",
                "When your to-do list needs a humor intervention... 😅 "
                + "We bring the laughs so you can bring the results. Tag someone who needs this break!");
        FALLBACK_TEMPLATES.put("INSTAGRAM_INSPIRATIONAL",
                "Your potential is limitless when you have the right support system. "
                + "Keep pushing forward - amazing things happen when you believe in your journey. 🌟");

        // TikTok templates
        FALLBACK_TEMPLATES.put("TIKTOK_PROFESSIONAL",
                "📊 3 quick tips that actually work for business growth in 2024: "
                + "1) Focus on solving real problems 2) Measure what matters 3) Iterate based on feedback. "
                + "Follow for more actionable insights!");
        FALLBACK_TEMPLATES.put("TIKTOK_CASUAL",
                "POV: You just found the easiest way to level up your daily routine. "
                + "It's simpler than you think and actually enjoyable. Try it today! 👇");
        FALLBACK_TEMPLATES.put("TIKTOK_HUMOROUS",
                "Me trying to adult vs. me remembering there's a better way: "
                + "[insert relatable struggle] → [insert our solution] → instant life upgrade. "
                + "Send this to someone who needs to laugh!");
        FALLBACK_TEMPLATES.put("TIKTOK_INSPIRATIONAL",
                "Small consistent actions create massive transformation over time. "
                + "What's one small step you can take today toward your bigger goals? "
                + "Start where you are, use what you have, do what you can. 💪");
    }

    /**
     * Generates fallback content based on request parameters.
     * 
     * @param request contains userId, platform, topic, tone, and maxLength
     * @return RecommendationResponseDto with fallback content
     * @throws FallbackContentException if template resolution fails
     */
    public RecommendationResponseDto provide(RecommendationRequestDto request) {
        String platform = request.getPlatform().name();
        String tone = request.getTone().name();
        String key = platform + "_" + tone;
        
        String template = FALLBACK_TEMPLATES.get(key);
        if (template == null || template.isBlank()) {
            template = "Stay tuned for exciting updates from our brand!";
            log.warn("No template found for key {}, using default fallback", key);
        }

        // Validate template length (200-300 chars as per requirement)
        if (template.length() < 200 || template.length() > 300) {
            log.error("Fallback template for key {} has invalid length: {} characters", key, template.length());
            throw new FallbackContentException(
                    String.format("Fallback template length invalid for key %s: %d chars", key, template.length()));
        }

        log.info("Fallback content provided for userId={} platform={} tone={}", 
                request.getUserId(), platform, tone);

        return RecommendationResponseDto.builder()
                .recommendationId(UUID.randomUUID())
                .userId(request.getUserId())
                .platform(platform)
                .content(template)
                .confidenceScore(BigDecimal.valueOf(0.30))
                .isFallback(true)
                .generatedAt(OffsetDateTime.now())
                .build();
    }
}