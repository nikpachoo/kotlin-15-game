FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 9001
CMD ["sh", "-c", "java -jar app.jar -P:postgres.url=$POSTGRES_URL -P:postgres.user=$POSTGRES_USER -P:postgres.password=$POSTGRES_PASSWORD"]
