# 1단계: Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# 라이브러리 캐싱 및 빌드
COPY build.gradle settings.gradle ./
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

COPY . .
RUN ./gradlew clean build -x test

# .war 확장자를 찾아서 app.jar로 이름을 바꿈
RUN mv build/libs/*[!plain].war build/libs/app.jar

# 2단계: Run stage (실제 배포 환경)
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# [핵심 수정] 리눅스 패키지 업데이트 및 ffmpeg 설치
# 설치 후 캐시를 삭제하여 이미지 용량을 최소화
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# 빌드 단계에서 준비한 app.jar 복사
COPY --from=build /app/build/libs/app.jar .

# 영상 저장 폴더 생성
RUN mkdir -p /app/video

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]