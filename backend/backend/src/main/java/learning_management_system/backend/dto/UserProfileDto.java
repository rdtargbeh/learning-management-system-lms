package learning_management_system.backend.dto;

import java.time.LocalDate;
import learning_management_system.backend.enums.ProfileVisibility;

/** Personal details every user has (table user_profile). */
public record UserProfileDto(String firstName, String middleName, String lastName, String preferredName, String displayName,
                             String pronouns, String phoneNumber, String address, String city, String stateRegion, String postalCode,
                             String country, String timezone, String preferredLanguage, LocalDate dateOfBirth, String gender,
                             String bio, ProfileVisibility profileVisibility) {}
