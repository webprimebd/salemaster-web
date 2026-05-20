# Build stage
FROM maven:3.8-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
COPY --from=build /target/management-0.0.1-SNAPSHOT.jar management.jar

# রেন্ডারকে কন্টেইনারের পোর্ট চেনাচ্ছি
EXPOSE 8080

# ডকার শেলকে সক্রিয় করে এনভায়রনমেন্ট ভ্যারিয়েবল এবং রেন্ডারের ডাইনামিক পোর্ট পুশ করার চূড়ান্ত কমান্ড
ENTRYPOINT java -Dspring.datasource.url=${SPRING_DATASOURCE_URL} \
             -Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
             -Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
             -Dspring.jpa.hibernate.ddl-auto=none \
             -Dserver.address=0.0.0.0 \
             -Dserver.port=${PORT} \
             -jar management.jar
