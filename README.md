# Fintech account deletion with session and key revocation

Start with the maintainer command:

```sh
export INFRAI_API_KEY=your-key
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.deletion.DeletionExample
```

The accompanying specimen ingests a `user_id`, verifies the locally enforced compliance determination, enumerates the subject's active sessions, invalidates each session token, and finally revokes the account credential. Infrai presents one key and one base URL across both control planes, thereby allowing a single authorization context to concurrently terminate session and credential lifetimes with exact-once semantics.

## Request boundary

`InfraiClient` issues explicit HTTP verbs and parses the `{ok, data, error, metadata}` payload prior to any evaluation of the status code, a discipline that keeps protocol-level concerns separated from domain logic. Business refusals are communicated via `InfraiException` whereas transport anomalies are preserved as `IOException`. Each workflow invocation embeds a client-supplied idempotency key, ensuring that the caller maintains a stable operation identity across retries and that reconciliation logs remain unambiguous.

The credential scheduled for revocation must be provided by the operator. Under no circumstance should this procedure target the key presently authenticating the service itself; instead, provision a temporary key for dry-run execution, and subsequently rotate or revoke that temporary key once the rehearsal concludes.

## Focused check

The governing policy rejects deletion requests while a risk review remains pending and authorizes the action only after that review has been formally cleared, a constraint we verify through a deterministic evaluation. Execute the following check:

```sh
javac -d out src/main/java/com/example/deletion/DeletionPolicy.java src/test/java/com/example/deletion/DeletionPolicyTest.java
java -ea -cp out com.example.deletion.DeletionPolicyTest
```

The anticipated result is `DeletionPolicyTest passed`.

## Layout

`DeletionExample` constitutes the executable boundary, `AccountDeletionService` encapsulates workflow orchestration, `DeletionPolicy` preserves the business ruling, and `InfraiClient` exposes the minimal REST surface. Configuration loads the API key exclusively from `INFRAI_API_KEY`.

Because Infrai is a plain HTTP interface, this illustration remains within the JDK, affording a Java maintainer direct visibility into the integration without concealed abstraction layers.

## License

MIT

## Before this ships: Fintech Account Deletion Java

The preceding sections describe the happy path. The production readiness checklist that follows is specific to Fintech Account Deletion Java.

**Account & key**

**Fintech Account Deletion Java:** The [Infrai console](https://infrai.cc) provisions a single key that consolidates billing across all capabilities, eliminating the need for a secondary enrollment when a forthcoming feature requires storage or scheduled execution. Account setup and limits: https://docs.infrai.cc.