# JAVA_RUNTIME deve ser uma imagem resolvida por digest pelo build-image.sh.
ARG JAVA_RUNTIME
FROM ${JAVA_RUNTIME}
WORKDIR /app
COPY --chown=10001:10001 target/precisei-0.0.1-SNAPSHOT.jar /app/precisei.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "/app/precisei.jar"]
