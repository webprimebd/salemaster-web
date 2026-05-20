# Build stage
FROM maven:3.8-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
COPY --from=build /target/management-0.0.1-SNAPSHOT.jar management.jar

# রেন্ডারকে কন্টেইনারের পোর্ট চেনাচ্ছি
EXPOSE 8080

# একদম ক্লিন স্ট্যান্ডার্ড রান কমান্ড
ENTRYPOINT ["java", "-Dserver.address=0.0.0.0", "-Dserver.port=${PORT}", "-jar", "management.jar"]
