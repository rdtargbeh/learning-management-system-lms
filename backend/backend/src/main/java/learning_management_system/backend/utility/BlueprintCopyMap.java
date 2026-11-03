package learning_management_system.backend.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Which item in an associated course is the copy of which blueprint item. Kept in the {@code copies} part of a
 * blueprint_sync_run's changes ({@code {courseId: {"TYPE:blueprintItemId": copyId}}}); every run stores the full map.
 */
public final class BlueprintCopyMap {

    private BlueprintCopyMap() {
    }

    public static String key(String objectType, UUID blueprintItemId) {
        return objectType + ":" + blueprintItemId;
    }

    /** courseId -> (key -> copy id), read from a run's changes. */
    public static Map<UUID, Map<String, UUID>> read(JsonNode changes) {
        Map<UUID, Map<String, UUID>> out = new HashMap<>();
        if (changes == null || !changes.has("copies")) return out;
        Iterator<Map.Entry<String, JsonNode>> courses = changes.get("copies").fields();
        while (courses.hasNext()) {
            Map.Entry<String, JsonNode> c = courses.next();
            Map<String, UUID> m = new HashMap<>();
            c.getValue().fields().forEachRemaining(e -> m.put(e.getKey(), UUID.fromString(e.getValue().asText())));
            out.put(UUID.fromString(c.getKey()), m);
        }
        return out;
    }

    public static ObjectNode write(ObjectMapper json, Map<UUID, Map<String, UUID>> copies) {
        ObjectNode root = json.createObjectNode();
        copies.forEach((course, m) -> {
            ObjectNode n = root.putObject(course.toString());
            m.forEach((k, v) -> n.put(k, v.toString()));
        });
        return root;
    }

    /** The blueprint item a course item was copied from, or null. */
    public static UUID blueprintItemFor(Map<String, UUID> courseMap, String objectType, UUID copyId) {
        if (courseMap == null) return null;
        for (Map.Entry<String, UUID> e : courseMap.entrySet()) {
            if (e.getValue().equals(copyId) && e.getKey().startsWith(objectType + ":")) {
                return UUID.fromString(e.getKey().substring(objectType.length() + 1));
            }
        }
        return null;
    }
}
