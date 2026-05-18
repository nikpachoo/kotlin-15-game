# Kodee vs Friction Server

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need
  to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                                                   | Description                                                                        |
|------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [Authentication](https://start.ktor.io/p/auth)                         | Provides extension point for handling the Authorization header                     |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Exposed](https://start.ktor.io/p/exposed)                             | Adds Exposed database to your application                                          |
| [Postgres](https://start.ktor.io/p/postgres)                           | Adds Postgres database to your application                                         |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                                    | Description                                                          |
|-----------------------------------------|----------------------------------------------------------------------|
| `./gradlew test`                        | Run the tests                                                        |
| `./gradlew build`                       | Build everything                                                     |
| `./gradlew buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew run`                         | Run the server                                                       |
| `./gradlew runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## Deploying to Google Cloud

The server runs on **Cloud Run** backed by **Cloud SQL for PostgreSQL**. Two scripts in `scripts/` handle everything; you don't need to memorise any `gcloud` commands.

### Prerequisites

- A Google Cloud project with billing enabled.
- The [`gcloud` CLI](https://cloud.google.com/sdk/docs/install) installed and authenticated (`gcloud auth login`).

### First-time setup

```bash
cp scripts/gcp.env.example scripts/gcp.env
# edit scripts/gcp.env — at minimum set PROJECT and REGION
./scripts/setup.sh
```

`setup.sh` enables the required APIs, creates the Artifact Registry repo, the Cloud SQL instance + database + user, and stores `db-password` / `auth-password` in Secret Manager. It prompts for the two passwords (or reads them from `gcp.env` if you set them there). Every step is idempotent — re-running creates only what's missing. To rotate the DB or auth password, re-run with `ROTATE_PASSWORDS=yes ./scripts/setup.sh`.

`scripts/gcp.env` is gitignored — don't commit it once it has real values.

### Database roles

Cloud SQL is configured with two PostgreSQL roles:

- **`postgres`**: Cloud SQL admin. Used only for schema migrations, break-glass debugging, and provisioning other roles. Its password is **not** kept in Secret Manager.
- **`highscore_server_app`**: the role the Cloud Run service connects as (`DB_USER` in `scripts/gcp.env`). Has only the minimum privileges the app needs: `CONNECT` on the `highscore` database, `USAGE` on schema `public`, `SELECT/INSERT/UPDATE/DELETE` on `public.users`, and `USAGE/SELECT` on `public.users_id_seq`. Cannot create tables, alter schema, manage roles, or touch any other database. Its password lives in Secret Manager (`db-password` by default, see gcp.env.example).

#### One-time provisioning of the app role

`setup.sh` provisions the instance, database, and secrets, but the restricted app role has to be created manually, `gcloud sql users create` would grant `cloudsqlsuperuser`, defeating the lockdown.

After `setup.sh` has run, generate the app password and stage it in Secret Manager:

```bash
APP_DB_PASSWORD="$(openssl rand -base64 36 | tr -d '/+=' | head -c 40)"
printf '%s' "$APP_DB_PASSWORD" \
  | gcloud secrets versions add db-password \
      --project=<PROJECT> --data-file=-
```

Then open a psql shell as the admin and run the SQL block below (substituting `<APP_DB_PASSWORD>`):

```bash
gcloud sql connect <INSTANCE> \
  --project=<PROJECT> --user=postgres --database=highscore
```

```sql
CREATE ROLE highscore_server_app WITH LOGIN PASSWORD '<APP_DB_PASSWORD>' NOINHERIT;

REVOKE ALL ON SCHEMA public FROM PUBLIC;

REVOKE ALL ON DATABASE highscore FROM highscore_server_app;
GRANT  CONNECT ON DATABASE highscore TO highscore_server_app;

GRANT USAGE ON SCHEMA public TO highscore_server_app;

-- Bootstrap the schema. Either by connecting the server through the `postgres` user first, and letting Exposed set up the tables,
-- or doing this manually (WARNING: might become outdated!)
CREATE TABLE IF NOT EXISTS public.users (
  id    serial       PRIMARY KEY,
  name  varchar(50)  NOT NULL,
  score integer      NOT NULL,
  email varchar(255) NULL
);
ALTER TABLE    public.users        OWNER TO postgres;
ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE    public.users        TO highscore_server_app;
GRANT USAGE, SELECT                  ON SEQUENCE public.users_id_seq TO highscore_server_app;
```

Then deploy. `scripts/gcp.env` should have `DB_USER=highscore_server_app`.

#### Rotating the app password

```bash
NEW_APP_PASSWORD="$(openssl rand -base64 36 | tr -d '/+=' | head -c 40)"
printf '%s' "$NEW_APP_PASSWORD" \
  | gcloud secrets versions add db-password \
      --project=<PROJECT> --data-file=-

gcloud sql connect <INSTANCE> --project=<PROJECT> --user=postgres --database=highscore
# inside psql:
#   ALTER ROLE highscore_server_app WITH PASSWORD '<NEW_APP_PASSWORD>';

./scripts/deploy.sh
# After verifying the new revision works, disable the previous secret version:
#   gcloud secrets versions list  db-password --project=<PROJECT>
#   gcloud secrets versions disable <OLD_VERSION> --secret=db-password --project=<PROJECT>
```

Schema changes (new columns, new tables) must also be applied as `postgres`, since `highscore_server_app` has no DDL privileges. The app's `SchemaUtils.create` will skip any table that already exists, so additive changes applied through psql are picked up automatically on next deploy.

### Deploying

```bash
./scripts/deploy.sh
```

This builds the image via Cloud Build (no local Docker needed), pushes it to Artifact Registry, deploys a new Cloud Run revision wired to Cloud SQL, and prints the service URL. Run it any time you want to ship code changes.

Verify the deploy:

```bash
curl -u "admin:<AUTH_PASSWORD>" "$(gcloud run services describe highscore-server --region=<REGION> --format='value(status.url)')/"
# Expected: Hello World!
```

The Cloud Run service is deployed with `--allow-unauthenticated` — the app's HTTP basic auth is what actually gates every endpoint, so make sure `AUTH_PASSWORD` is strong.

