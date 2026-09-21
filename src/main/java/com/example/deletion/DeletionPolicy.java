package com.example.deletion;

public final class DeletionPolicy {
    public Decision evaluate(String userId, boolean riskCleared, int activeSessions) {
        if (userId == null || userId.isBlank()) return new Decision(false, "user_id is required");
        if (!riskCleared) return new Decision(false, "risk review is required");
        return new Decision(true, "revoke " + activeSessions + " sessions and the account key");
    }

    public record Decision(boolean approved, String reason) { }
}
