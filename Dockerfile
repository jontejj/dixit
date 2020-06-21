# First stage: JDK 11 with modules required for Spring Boot
FROM maven:3.6.3-jdk-11 as packager
ENV JAVA_MINIMAL="/opt/java-minimal"

WORKDIR /app
COPY pom.xml .
COPY src src
COPY frontend frontend
COPY package.json package.json
COPY webpack.config.js webpack.config.js
RUN mvn package -Pproduction,integration --no-transfer-progress

# build modules distribution
RUN jlink --verbose --add-modules java.base,java.logging,java.naming,java.desktop,java.management,java.security.jgss,java.instrument --no-header-files --no-man-pages --output "$JAVA_MINIMAL"

# Second stage, add only our minimal "JRE" distr and our app, alpine:3.12.0
FROM debian:stretch-slim

ENV JAVA_HOME="/opt/java-minimal"
ENV PATH="$JAVA_HOME/bin:$PATH"

COPY --from=packager "$JAVA_HOME" "$JAVA_HOME"
COPY --from=packager "/app/target/dixit-0.7-spring-boot.jar" "/app.jar"

EXPOSE 80
CMD [ "-jar", "/app.jar" ]
ENTRYPOINT [ "java" ]