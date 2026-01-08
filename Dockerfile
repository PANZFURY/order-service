FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

EXPOSE 8081

CMD ["java", "-jar", "target/orderservice-0.0.1-SNAPSHOT.jar"]
