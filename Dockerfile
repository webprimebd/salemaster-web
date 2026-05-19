# Build stage
FROM maven:3.8-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
COPY --from=build /target/*.jar management.jar

# রেন্ডারকে কন্টেইনারের পোর্ট চেনাচ্ছি
EXPOSE 8080

# ডকার শেলকে সক্রিয় করে এনভায়রনমেন্ট ভ্যারিয়েবল সরাসরি স্প্রিং বুটে পুশ করার চূড়ান্ত কমান্ড
ENTRYPOINT java -Dspring.datasource.url=${SPRING_DATASOURCE_URL} \
             -Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
             -Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
             -Dspring.jpa.hibernate.ddl-auto=update \
             -Dserver.address=0.0.0.0 \
             -Dserver.port=8080 \
             -jar management.jar
