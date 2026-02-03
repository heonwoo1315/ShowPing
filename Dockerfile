# 1단계: Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# 라이브러리 캐싱
COPY build.gradle settings.gradle ./
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

COPY . .
RUN ./gradlew clean build -x test

# [수정] .war 확장자를 찾아서 app.jar로 이름을 바꿉니다. (실행 가능한 war는 jar처럼 실행 가능합니다)
RUN mv build/libs/*[!plain].war build/libs/app.jar

# 2단계: Run stage
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# 빌드 단계에서 준비한 app.jar 복사
COPY --from=build /app/build/libs/app.jar .

RUN mkdir -p /app/video
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]