from openjdk:21-jdk-slim
WORKDIR /app
ADD target/blog-0.0.1-SNAPSHOT.jar /app/app.jar
expose 8080
ENTRYPOINT ["java", "-jar", "app.jar"]