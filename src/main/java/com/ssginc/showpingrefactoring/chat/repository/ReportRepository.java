package com.ssginc.showpingrefactoring.chat.repository;

//import com.ssginc.showpingrefactoring.chat.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
}