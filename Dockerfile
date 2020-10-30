FROM openjdk:8-alpine

COPY target/uberjar/dataseq-core.jar /dataseq-core/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/dataseq-core/app.jar"]
