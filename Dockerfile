# 1단계: Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
# 빌드 속도 향상을 위해 라이브러리 캐싱 활용
COPY build.gradle settings.gradle ./
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

COPY . .
RUN ./gradlew clean build -x test

# 2단계: Run stage
FROM openjdk:17-slim
WORKDIR /app
# 빌드된 jar 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# EC2 내부 비디오 저장 경로 생성
RUN mkdir -p /app/video

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]