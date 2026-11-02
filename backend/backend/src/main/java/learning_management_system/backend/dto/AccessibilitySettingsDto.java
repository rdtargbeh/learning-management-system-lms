package learning_management_system.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * A user's accessibility settings (table user_accessibility_settings). As a PATCH body null leaves a value unchanged;
 * textSizePercentage 50-200, extendedTimePercentage one of 100/125/150/175/200/250.
 */
public record AccessibilitySettingsDto(Boolean highContrastEnabled, @Min(50) @Max(200) Integer textSizePercentage,
                                       Boolean screenReaderEnabled, Boolean dyslexiaFriendlyFont, @Size(max = 100) String fontFamilyAccessible,
                                       Boolean captionsEnabled, Boolean transcriptsEnabled, Boolean extendedTimeOnAssessments,
                                       Integer extendedTimePercentage, Boolean reducedMotionEnabled, Boolean colorBlindMode,
                                       @Size(max = 50) String colorBlindType, Boolean keyboardNavigationEnabled,
                                       Boolean disableAnimations, Boolean lowDataMode, String accessibilityNotes) {}
