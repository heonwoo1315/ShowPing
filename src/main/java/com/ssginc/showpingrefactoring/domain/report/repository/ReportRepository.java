package com.ssginc.showpingrefactoring.domain.report.repository;

import com.ssginc.showpingrefactoring.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
}