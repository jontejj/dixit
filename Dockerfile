FROM alpine:3.10 AS build

ENV JAVA_MINIMAL="/opt/java-minimal"
ENV JAVA_HOME /opt/jdk
ENV PATH $JAVA_HOME/bin:$PATH
ENV DOWNLOADED_JDK_PATH="$JAVA_HOME/openjdk.tar.gz"

RUN mkdir -p $JAVA_HOME
RUN apk add 'curl<7.70.0-r2'
#Use project Portola (https://openjdk.java.net/projects/portola/)
RUN curl https://download.java.net/java/early_access/alpine/10/binaries/openjdk-15-ea+10_linux-x64-musl_bin.tar.gz -o $DOWNLOADED_JDK_PATH
RUN echo "15a5e8002e24ed129b82bfe55ffe4bdbf3cfd0a7e5ad3399879cdd44175bfd06  $DOWNLOADED_JDK_PATH" | sha256sum -c
RUN tar --extract --file $DOWNLOADED_JDK_PATH --directory "$JAVA_HOME" --strip-components 1; \
    rm $DOWNLOADED_JDK_PATH;
    
RUN apk add 'maven<3.6.3-r0' 'nodejs<12.18.0-r2' 'npm<12.18.0-r2'

WORKDIR /app
COPY pom.xml .
#Download dependencies to improve caching
RUN mvn dependency:go-offline --no-transfer-progress

COPY package.json package.json
COPY webpack.config.js webpack.config.js
#Download dependencies to improve caching
RUN npm install

COPY frontend frontend
COPY src src
RUN mvn package -Pproduction,integration --no-transfer-progress

# build modules distribution
RUN jlink --compress=2 --verbose --add-modules java.base,java.logging,java.naming,java.desktop,java.management,java.security.jgss,java.instrument --no-header-files --no-man-pages --output "$JAVA_MINIMAL"

FROM alpine:3.10

ENV JAVA_MINIMAL="/opt/java-minimal"
ENV JAVA_HOME /opt/jdk

COPY --from=build  $JAVA_MINIMAL $JAVA_HOME
ENV PATH=/opt/jdk/bin:$PATH
COPY --from=build "/app/target/dixit-0.7-spring-boot.jar" "/app.jar"

EXPOSE 80
CMD ["-showversion", "-jar", "/app.jar" ]

ENTRYPOINT ["java"]