package com.example.deletion;

import java.io.IOException;
import java.util.List;

/** Coordinates a deletion request after a compliance decision. */
public final class AccountDeletionService {
    private final InfraiClient client;
    private final DeletionPolicy policy = new DeletionPolicy();

    public AccountDeletionService(InfraiClient client) { this.client = client; }

    public Result execute(Request request) throws IOException, InterruptedException {
        DeletionPolicy.Decision decision = policy.evaluate(request.userId(), request.riskCleared(), request.sessionIds().size());
        if (!decision.approved()) return new Result(false, decision.reason());
        client.listSessions(request.userId());
        for (String sessionId : request.sessionIds()) client.revokeSession(sessionId, request.idempotencyKey());
        client.revokeAccountKey(request.accountKeyId());
        return new Result(true, decision.reason());
    }

    public record Request(String userId, String accountKeyId, List<String> sessionIds,
                          boolean riskCleared, String idempotencyKey) { }
    public record Result(boolean completed, String message) { }
}
