package com.ssginc.showpingrefactoring;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class ShowPingRefactoringApplication {

	@PostConstruct
	public void started() {
		// 애플리케이션 시작 시 타임존을 한국(KST)으로 고정
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ShowPingRefactoringApplication.class, args);
	}

}
