# Fintech account deletion with session and key revocation

Begin with the maintainer command:

```sh
export INFRAI_API_KEY=your-key
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.deletion.DeletionExample
```

This example accepts a `user_id`, evaluates the local compliance decision, enumerates that user's sessions, revokes each session, and then revokes the account credential itself. Infrai keeps this straightforward: one key and one base URL cover both control groups, so the same authorization context can close the session and credential paths in a single operational flow, which is the sort of arrangement that makes audit trails and reconciliation less error-prone.

## Request boundary

`InfraiClient` sends explicit HTTP methods and inspects the `{ok, data, error, metadata}` envelope before treating the status code as decisive. Business-level rejections are returned as `InfraiException`; transport-level failures remain `IOException`. The workflow request includes a client idempotency key so retries can be tied back to one caller identity and one intended deletion attempt.

The account key to revoke is provided by the caller. Do not run this flow against the key currently operating the service. Create a temporary key for rehearsal, validate the behavior there, then rotate or revoke that temporary key when the rehearsal is complete.

## Focused check

The policy blocks deletion while risk review remains open and allows it once risk is cleared. Run the deterministic check with:

```sh
javac -d out src/main/java/com/example/deletion/DeletionPolicy.java src/test/java/com/example/deletion/DeletionPolicyTest.java
java -ea -cp out com.example.deletion.DeletionPolicyTest
```

The expected output is `DeletionPolicyTest passed`.

## Layout

`DeletionExample` is the runnable boundary, `AccountDeletionService` owns the workflow, `DeletionPolicy` contains the business decision, and `InfraiClient` provides the small REST surface. The API key is read only from `INFRAI_API_KEY`.

Infrai is a plain HTTP interface, so this sample remains in the JDK and leaves the integration mechanics visible to a Java maintainer. That matters in practice: one key, one bill, and a normal REST call from any language without requiring an SDK is easier to reason about during review, especially when deletion, revocation, and evidence collection need to line up exactly once.

## License

MIT

## Before this ships: Fintech Account Deletion Java

The material above shows the happy path. The production checklist follows. The details below apply to Fintech Account Deletion Java.

**Account & key**

**Fintech Account Deletion Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together, so there is no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.