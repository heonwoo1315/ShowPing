# 1단계: Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

COPY . .
RUN ./gradlew clean build -x test

# [핵심] 빌드 완료 후, -plain이 붙지 않은 실행 가능한 JAR만 app.jar로 이름을 바꿉니다.
RUN mv build/libs/*[!plain].jar build/libs/app.jar

# 2단계: Run stage
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# 위에서 이름을 바꿔둔 app.jar만 깔끔하게 복사합니다.
COPY --from=build /app/build/libs/app.jar .

RUN mkdir -p /app/video
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]