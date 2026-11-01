package learning_management_system.backend.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;

/**
 * Represents a global or tenant-specific system setting.
 */

@CrossOrigin("*")
@Entity
@Table(name = "global_settings")
public class GlobalSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settingId;

    @Column(name = "group", length = 50)
    private String group; // New column for categorization

    @Column(name = "key", nullable = false, unique = true, length = 100)
    private String key;

    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Indicates if the setting is active


    // Constructor
    public GlobalSettings() {}

    public GlobalSettings(Long settingId, String group, String key, String value, String description, LocalDateTime lastUpdated, Boolean isActive) {
        this.settingId = settingId;
        this.group = group;
        this.key = key;
        this.value = value;
        this.description = description;
        this.lastUpdated = lastUpdated;
        this.isActive = isActive;
    }

    // Getter and Setter

    public Long getSettingId() {
        return settingId;
    }

    public void setSettingId(Long settingId) {
        this.settingId = settingId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}

