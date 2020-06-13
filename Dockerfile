# First stage: JDK 11 with modules required for Spring Boot
FROM maven:3.6.3-jdk-11 as packager

# source JDK distribution names
# update from https://jdk.java.net/java-se-ri/11
#ENV JDK_VERSION="11.0.1"
#ENV JDK_URL="https://download.java.net/java/GA/jdk11/13/GPL/openjdk-${JDK_VERSION}_linux-x64_bin.tar.gz"
#ENV JDK_HASH="7a6bb980b9c91c478421f865087ad2d69086a0583aeeb9e69204785e8e97dcfd"
#ENV JDK_HASH_FILE="${JDK_ARJ_FILE}.sha2"
#ENV JDK_ARJ_FILE="openjdk-${JDK_VERSION}.tar.gz"
# target JDK installation names
ENV OPT="/opt"
#ENV JKD_DIR_NAME="jdk-${JDK_VERSION}"
#ENV JAVA_HOME="${OPT}/${JKD_DIR_NAME}"
ENV JAVA_MINIMAL="${OPT}/java-minimal"

# downloaded JDK to the local file
#ADD "$JDK_URL" "$JDK_ARJ_FILE"

# verify downloaded file hashsum
#RUN { \
#        echo "Verify downloaded JDK file $JDK_ARJ_FILE:" && \
#        echo "$JDK_HASH $JDK_ARJ_FILE" > "$JDK_HASH_FILE" && \
#        sha256sum -c "$JDK_HASH_FILE" ; \
#    }

# extract JDK and add to PATH
#RUN { \
#        echo "Unpack downloaded JDK to ${JAVA_HOME}/:" && \
#        mkdir -p "$OPT" && \
#        tar xf "$JDK_ARJ_FILE" -C "$OPT" ; \
#   }
#ENV PATH="$PATH:$JAVA_HOME/bin"

RUN { \
        java --version ; \
        echo "jlink version:" && \
        jlink --version ; \
    }

WORKDIR /app
COPY pom.xml .
COPY src src
COPY frontend frontend
COPY package.json package.json
COPY webpack.config.js webpack.config.js

RUN mvn package -Pproduction

# build modules distribution
RUN jlink --verbose --add-modules java.base,java.logging,java.naming,java.desktop,java.management,java.security.jgss,java.instrument --no-header-files --no-man-pages --output "$JAVA_MINIMAL"

# Second stage, add only our minimal "JRE" distr and our app
FROM debian:stretch-slim

ENV JAVA_HOME=/opt/java-minimal
ENV PATH="$PATH:$JAVA_HOME/bin"

COPY --from=packager "$JAVA_HOME" "$JAVA_HOME"
COPY --from=packager "/app/target/dixit-0.7-spring-boot.jar" "/app.jar"

EXPOSE 80
CMD [ "-jar", "/app.jar" ]
ENTRYPOINT [ "java" ]