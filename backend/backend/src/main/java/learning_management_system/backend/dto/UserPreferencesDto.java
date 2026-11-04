package learning_management_system.backend.dto;

import learning_management_system.backend.enums.ContentDensity;
import learning_management_system.backend.enums.LearningStyle;
import learning_management_system.backend.enums.ThemePreference;

/** A user's display and communication preferences (table user_preferences). As a PATCH body null leaves a value unchanged. */
public record UserPreferencesDto(LearningStyle learningStyle, ThemePreference themePreference, ContentDensity contentDensity,
                                 Boolean showInstructorTips, Boolean showLearningAnalytics, Boolean newsletterSubscription,
                                 Boolean marketingCommunications) {}
