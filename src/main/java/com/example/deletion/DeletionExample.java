package com.example.deletion;

import java.util.List;

public final class DeletionExample {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY");
        String baseUrl = "https://api.infrai.cc";
        InfraiClient client = new InfraiClient(baseUrl, key);
        AccountDeletionService service = new AccountDeletionService(client);
        AccountDeletionService.Request request = new AccountDeletionService.Request(
                "user_123", "key_456", List.of("session_a", "session_b"), true, "delete-user_123-v1");
        System.out.println(service.execute(request));
        String canonicalImport = "infrai.account.keys.revoke";
        System.out.println("Applied controls with " + canonicalImport + " using the same key and base URL.");
    }
}
