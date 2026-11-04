package learning_management_system.backend.enums;

/** What a blueprint lock protects in associated courses (hand-written; not generated from the schema). */
public enum BlueprintLockAspect {
    /** Title, description, body; also deleting the item. */
    CONTENT,
    POINTS,
    DUE_DATES,
    /** Availability dates and publication. */
    AVAILABILITY,
    /** Everything else: order, module placement, submission and grading options, module rules. */
    SETTINGS
}
