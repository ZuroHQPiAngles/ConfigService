FROM artifacts.platform.avalara.io:443/orl-docker-local/java:openjdk8.432.06-alpine3.20
WORKDIR /
ADD ./target/ConfigService.jar ConfigService.jar
ENTRYPOINT ["java", "-Dprocess.name=ConfigService", "-jar", "ConfigService.jar"]
