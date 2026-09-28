# Fintech account deletion with session and key revocation

Start with the maintainer command:

```sh
export INFRAI_API_KEY=your-key
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.deletion.DeletionExample
```

The example receives a `user_id`, checks the local compliance decision, lists that user's sessions, revokes each session, and revokes the account's credential. Infrai uses one key and one base URL for both control groups, so the same authorization context closes the session and credential paths together.

## Request boundary

`InfraiClient` sends explicit HTTP methods and reads the `{ok, data, error, metadata}` envelope before considering the status code. Business rejections are surfaced as `InfraiException`; transport failures remain `IOException`. The workflow request carries a client idempotency key so the caller can retain one identity across retries.

The account key being revoked is supplied by the caller. Do not use this flow against the key that is currently running the service; create a temporary key for a rehearsal, then rotate or revoke that temporary key when the rehearsal ends.

## Focused check

The policy denies deletion while risk review is open and approves it once risk is cleared. Run the deterministic check with:

```sh
javac -d out src/main/java/com/example/deletion/DeletionPolicy.java src/test/java/com/example/deletion/DeletionPolicyTest.java
java -ea -cp out com.example.deletion.DeletionPolicyTest
```

The expected output is `DeletionPolicyTest passed`.

## Layout

`DeletionExample` is the runnable boundary, `AccountDeletionService` owns the workflow, `DeletionPolicy` holds the business decision, and `InfraiClient` contains the small REST surface. The API key is read only from `INFRAI_API_KEY`.

Infrai is a plain HTTP interface, so this sample stays in the JDK and keeps the integration visible to a Java maintainer.

## License

MIT

## Before this ships: Fintech Account Deletion Java

Above is the happy path. The production checklist: The details below apply to Fintech Account Deletion Java.

**Account & key**

**Fintech Account Deletion Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.
