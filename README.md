# ShowPing (Refactoring) 📦

## 1. Overview (프로젝트 개요)

ShowPing은 **실시간 라이브 커머스 플랫폼**으로 오프라인 쇼핑몰의 생생한 경험을 온라인으로 옮겨와 라이브 스트리밍의 상호 작용성을 더한 서비스 입니다.  
이 플랫폼은 **실시간 상품 소개, 채팅 및 댓글을 통한 즉각적인 소통, 구매 연동 기능**을 결합하여 고객이 더 빠른 구매 결정을 내리고 판매자는 시청자 참여를 극대화할 수 있도록 돕습니다.

---

## 2. Objective (기획 의도)

- **실시간 상품 소개 제공**을 통해 고객 신뢰와 몰입도를 높인다.
- **채팅 및 댓글 기반 피드백**으로 고객 만족도를 향상시킨다.
- **구매 연동 기능**을 통해 상품 탐색과 구매 사이의 단계를 단축한다.
- **전자제품 카테고리**를 시작으로 수요가 높고 단가가 큰 상품군을 확대한다.
- **오프라인 쇼핑 경험 + 실시간 소통**을 결합하여 **차세대 라이브 커머스 생태계**를 구현한다.

---

## 3. Development Period (개발 기간)
- 2025.04 ~

---

## 4. Roles (역할)

| 이름 | 역할 |
|------|------|
| [김대철](https://github.com/dckat)  | 프로젝트 형상관리, 라이브 스트리밍 기능, VOD 기능 |
| [김주일](https://github.com/juil1-kim)   | 스케줄러, 채팅 기능, 신고 관리 기능 |
| [김창훈](https://github.com/C-H-Kim) | 팀장, 서버 인프라 구축, 라이브 스트리밍 기능 |
| [박헌우](https://github.com/heonwoo1315)  | 문서화 작업, 회원 기능, 보안 기능 |
| [조민호](https://github.com/heonwoo1315)  | ERD 관리, 상품 기능, 장바구니 기능, 결제 기능 |

---

## 5. Tech Stack (기술 스택)

### 🖥 Backend
<p>
  <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white">
  <img src="https://img.shields.io/badge/JPA-Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
</p>

### 💬 Messaging & Streaming
<p>
  <img src="https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white">
  <img src="https://img.shields.io/badge/Apache%20Zookeeper-FF6F00?style=for-the-badge&logo=apache&logoColor=white">
  <img src="https://img.shields.io/badge/STOMP-WebSocket-FF5722?style=for-the-badge&logo=socketdotio&logoColor=white">
  <img src="https://img.shields.io/badge/Kurento-Media%20Server-5BA745?style=for-the-badge&logo=webrtc&logoColor=white">
</p>

### 💾 Database
<p>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
</p>

### 💻 Frontend
<p>
  <img src="https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=white">
  <img src="https://img.shields.io/badge/css3-1572B6?style=for-the-badge&logo=css3&logoColor=white">
  <img src="https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white">
</p>

### ☁ Infra & DevOps
<p>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white">
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">
  <img src="https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazonaws&logoColor=white">
  <img src="https://img.shields.io/badge/NCP-03C75A?style=for-the-badge&logo=naver&logoColor=white">
</p>

### 🛠 Tools & Collaboration
<p>
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black">
  <img src="https://img.shields.io/badge/JMeter-D22128?style=for-the-badge&logo=apachejmeter&logoColor=white">
  <img src="https://img.shields.io/badge/nGrinder-000000?style=for-the-badge&logo=apache&logoColor=white">
  <img src="https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white">
  <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white">
  <img src="https://img.shields.io/badge/Trello-0052CC?style=for-the-badge&logo=trello&logoColor=white">
</p>

---

## 6. Project Architecture (프로젝트 아키텍처 구조)

### 6 - 1. Service Infrastru
<img width="547" height="406" alt="시스템 아키텍처_라이브_4팀(채팅 기반 라이브 커머스 서비스)" src="https://github.com/user-attachments/assets/5ad938ed-9e31-478c-a681-b963e832bce3" />

- **Ubuntu Private 서버** 위에서 Docker Compose로 전체 서비스 운영  
- **Nginx**: HTTPS Termination + Reverse Proxy  
- **Spring Boot (Tomcat)**: 핵심 비즈니스 로직 (WebFlux, Batch 등)  
- **Kurento Media Server**: WebRTC 기반 실시간 스트리밍 처리  
- **AWS RDS (MySQL)**: 관계형 데이터 저장소  
- **NCP Object Storage**: 방송 영상 저장소

---

### 6 - 2. Detailed System Architecture (상세 시스템 아키텍처)
<img width="4052" height="1802" alt="시스템 아키텍처_전체_4팀(채팅 기반 라이브 스트리밍 서비스)" src="https://github.com/user-attachments/assets/699728c5-c1c9-435c-a263-c46b531d1ae1" />

- **Spring Boot 내부 구성**: Spring Security, JWT, STOMP(WebSocket), WebFlux, Batch, Gradle  
- **Redis**: Refresh Token 저장소  
- **Kafka + ZooKeeper**: 채팅 및 메시징 분산 처리  
- **MongoDB**: 채팅 로그 및 금칙어 저장  
- **외부 연동 서비스**:  
  - AWS RDS (MySQL)  
  - NCP (Clova, Object Storage)  
  - Google Authenticator (TOTP 2FA)  
  - PortOne (결제 모듈)  
- **CI/CD**: GitHub Actions 기반 자동 배포  
- **협업 툴 & 성능 테스트 툴**: Slack, Figma, Trello, JMeter, nGrinder 등

---

### 6 - 3 Chat & Messaging Flow (채팅 및 메시징 구조)
<img width="670" height="479" alt="시스템 아키텍처_채팅_4팀(채팅 기반 라이브 커머스 서비스)" src="https://github.com/user-attachments/assets/2c7fde15-2b73-49d3-82ca-6993482d7df6" />

- **Client → Nginx → Spring (JWT 기반 인증)** 요청 흐름  
- **STOMP/WebSocket**: 클라이언트와 서버 간 실시간 통신  
- **Kafka + ZooKeeper**: 채팅 메시지 Producer/Consumer 구조  
- **MongoDB**: 금칙어·채팅 로그 저장  
- **MySQL**: 회원/상품/권한 관리  
- **JWT + Spring Security**: 사용자 인증/인가 처리 

---

### 6 - 4 4️⃣ CI/CD Pipeline (배포 파이프라인)
<img width="508" height="194" alt="시스템_아키텍처_CICD_4팀(채팅 기반 라이브 커머스 서비스)" src="https://github.com/user-attachments/assets/f8114518-bc8a-452e-a6ed-7ff3e329d73c" />

- **개발자 → GitHub**: 코드 Push  
- **GitHub Actions**: CI/CD Workflow 실행  
- **SCP 방식 배포**: Build된 `.war` 파일을 Ubuntu 서버의 Tomcat 컨테이너에 전달  
- **자동 반영**: 컨테이너 내부에서 서비스 구동  

---

## 7. ERD & Database Design (ERD 및 데이터베이스 설계)


<img width="1450" height="742" alt="ERD_4팀(채팅 기반 라이브 커머스 서비스)" src="https://github.com/user-attachments/assets/913547c4-b2f5-4165-90bb-27776f6bcaf6" />

---

## 8. Git Convention & Collaboration (깃 컨벤션 & 협업)

### 🔹 Git Branch Naming Rules (브랜치 네이밍 규칙)
- **Issue 기반 브랜치 생성**: 새로운 기능 추가나 버그 수정을 시작할 때 먼저 GitHub Issue를 작성하고, 해당 이슈 번호를 브랜치명에 반영
  - 예시: `fix/csrf/70` → CSRF 버그 수정 (이슈 번호: #70)
- 브랜치 네이밍 규칙:
  - `feat/기능명/이슈번호` → 새로운 기능 개발
  - `fix/버그명/이슈번호` → 버그 수정
  - `refactor/모듈명/이슈번호` → 리팩토링 작업

### 🔹 Pull Request & Code Review (풀 리퀘스트 & 코드 리뷰)
- 기능 구현 후 **Pull Request(PR)** 생성
- 팀원들이 PR 코드를 검토하며:
  - 코드 블록을 **드래그하여 코멘트** 작성
  - 의문점/개선점을 질의응답 형식으로 공유
- **최소 2명 이상의 승인**을 받아야 Merge 가능
- Merge는 **PR 작성자가 직접 수행**

### 🔹 Knowledge Sharing (지식 공유)
- 개발 중 **도메인 지식, 트러블슈팅 경험** 등을 발견하면 GitHub Issue로 정리 및 공유
- 이슈는 단순 Task 관리뿐만 아니라 **지식 공유 플랫폼** 역할도 수행

---

### 🤝 Collaboration Process (협업 프로세스)

### 📌 During Bootcamp (Original Project)
- **기간**: 2개월
- **Daily Report (GitHub Issue 활용)**  
  - 오늘 할 일(To-do)  
  - 주요 이슈 공유  
  - 도메인 지식 키워드 정리  
  - Future Task (내일 할 일) 체크리스트
- **Daily Meeting**  
  - 아침마다 전날 작업 공유 + 개발 중 발생한 이슈 공유
- **Domain Ownership**  
  - 예: JWT 기반 로그인/인증/인가 도메인 담당 → 러닝커브 극복
- **Documentation**  
  - WBS, Notion, Swagger 등으로 명세 및 진행사항 관리
- **Communication**  
  - 필요 시 Discord 비공개 회의 진행
- **성과**  
  - 해당 프로젝트로 **최우수상 수상**

### 📌 After Bootcamp (Refactoring Project)
- **리포지토리 분리** 후 리팩토링 진행
- 새로운 기능 추가 및 버그 수정
- 파일 구조를 **DDD 기반**으로 재구성
- GitHub Issue & PR을 중심으로 협업 강화

---

## 9. Key Features (주요 기능)

- **Authentication & Authorization (인증/인가)**
  - JWT 기반 로그인/회원가입 (Access + Refresh Token, Redis 저장)
  - TOTP 기반 2단계 인증 (Google Authenticator 연동)
  - CSRF 토큰 기반 보안 구조 적용

- **Live Streaming (라이브 방송)**
  - WebRTC & Kurento 기반 실시간 방송 송출
  - RTMP/HLS 지원 → 다양한 기기 호환성 확보
  - 방송 중 실시간 채팅 및 상품 연동

- **Chat System (채팅 시스템)**
  - STOMP(WebSocket) 기반 양방향 메시징
  - Kafka 분산 처리로 안정성 및 확장성 확보
  - MongoDB 저장 및 금칙어 필터링

- **VOD Service (다시보기 서비스)**
  - 방송 종료 후 영상 NCP Object Storage에 저장
  - HLS 인코딩 및 썸네일 생성
  - VOD 다시보기 제공

- **Watch History (시청 내역)**
  - Cursor 기반 페이지네이션 → 대용량 데이터에서도 빠른 조회
  - 기간 필터 (7일, 1개월, 3개월, 6개월, 사용자 지정 범위)
  - 추천 서비스 확장 가능성

- **Cart & Payment (장바구니 및 결제)**
  - 장바구니 CRUD (추가/삭제/수량 변경)
  - PortOne 결제 모듈 연동
  - CSRF 보안 적용된 REST API

- **Admin & API Docs (관리자 및 문서화)**
  - 관리자 방송 관리 기능 (등록/삭제)
  - Swagger 기반 API 문서 자동 제공

---

## 10. Service Screenshots (서비스 화면 캡처)

### 🔹 Main Page (메인 페이지)

- 사용자가 접속했을 때 처음 보게 되는 메인 화면입니다.
<p align="center">
  <img width="1918" height="850" alt="image" src="https://github.com/user-attachments/assets/33a0920e-0665-4f0f-bdb7-b25b1d7e6fcc" />
</p>

### 🔹 Login & Signup (로그인 & 회원가입)

- 사용자 인증을 위한 로그인 및 회원가입 페이지입니다.

- 로그인 화면
<p align="center">
  <img width="1658" height="843" alt="image" src="https://github.com/user-attachments/assets/b185e0c6-a4ed-438a-9914-f643226790dc" />
</p>

- 회원가입 화면
<p align="center">
  <img width="1660" height="868" alt="image" src="https://github.com/user-attachments/assets/cdc0ce2f-680b-4b3a-9648-4c52745bac01" />
</p>

### 🔹 Live Streaming (라이브 방송)

- 실시간 방송 화면과 채팅창을 함께 볼 수 있는 화면입니다.

- 관리자 페이지
<p align="center">
  <img width="950" height="498" alt="image" src="https://github.com/user-attachments/assets/72c5748e-74cc-408f-9665-108fb48c0bdb" />
</p>

- 유저 페이지
<p align="center">
  <img width="957" height="431" alt="image" src="https://github.com/user-attachments/assets/65773446-a515-4fc6-b67f-13bcf93f2889" />
</p>

### 🔹 Watch History (시청 내역)

- 방송 종료 후 다시보기 페이지입니다.
<p align="center">
  <img width="957" height="463" alt="image" src="https://github.com/user-attachments/assets/f7d85b6d-f039-456c-9d81-f8683171193b" />

  <img width="958" height="465" alt="image" src="https://github.com/user-attachments/assets/0f25ef67-36dd-49bd-9171-a7d1eef59505" />
</p>

### 🔹 Cart & Payment (장바구니 및 결제)

- 장바구니 와 결제 진행 화면입니다.
<p align="center">
  <img width="958" height="456" alt="image" src="https://github.com/user-attachments/assets/5b48ea0c-7ea0-4a9c-bc20-6d8ddb73fd1f" />
  <img width="958" height="464" alt="image" src="https://github.com/user-attachments/assets/f7535f26-0a3b-4476-9ce7-cc32e9af3bad" />
  <img width="957" height="463" alt="image" src="https://github.com/user-attachments/assets/be4db94b-fcd7-4c6f-bcf6-aa26fcdd755d" />
  <img width="321" height="320" alt="image" src="https://github.com/user-attachments/assets/3151faa3-9182-4882-b2d9-c561c1ed297e" />
  <img width="325" height="323" alt="image" src="https://github.com/user-attachments/assets/8f55b437-f26d-449d-bba9-038e6bf30cd2" />
</p>

### 🔹 Admin Dashboard (관리자 페이지)

- 방송을 등록/삭제할 수 있는 관리자 화면입니다.  
<p align="center">
  <img width="958" height="468" alt="image" src="https://github.com/user-attachments/assets/fc66d2ad-8a29-4577-93f5-f7d87452dfe0" />
</p>

### 🔹 Report Management (신고 관리 페이지)

- 사용자 신고 내역을 관리하고 처리할 수 있는 관리자 화면입니다.
<p align="center">
  <img width="956" height="448" alt="image" src="https://github.com/user-attachments/assets/45919db3-0d3d-4bdc-9531-d415141804e2" />
  <img width="955" height="449" alt="image" src="https://github.com/user-attachments/assets/7b3672f0-169d-4d01-98a1-f77a6a421397" />
  <img width="958" height="419" alt="image" src="https://github.com/user-attachments/assets/e38b8218-aca8-4813-a503-ffc81eae208f" />
</p>

--- 

## 11. Demo Video (시연 영상)
[전체 시연](https://drive.google.com/file/d/1qRfUPvz5sc0aGOEi5frr-GHMHEO6eOMj/view?usp=drive_link)
