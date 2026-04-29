# koita-server

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

