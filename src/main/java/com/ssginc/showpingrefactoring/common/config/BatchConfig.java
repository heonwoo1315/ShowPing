package com.ssginc.showpingrefactoring.common.config;

import com.ssginc.showpingrefactoring.domain.stream.service.HlsService;
import com.ssginc.showpingrefactoring.domain.stream.service.SubtitleService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author dckat
 * 배치 작업 구성하는 클래스
 * <p>
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    /**
     * 지정된 JobRepository와 Step을 사용하여 HLS 저장 작업(Job)을 생성하는 메서드
     * @param jobRepository  JobRepository 객체
     * @param createHlsStep    HLS 저장 Step
     * @return 생성된 HLS 저장 Job
     */
    @Bean
    public Job createHlsJob(JobRepository jobRepository, Step createHlsStep) {
        return new JobBuilder("createHlsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(createHlsStep)
                .build();
    }

    /**
     * 지정된 JobRepository, PlatformTransactionManager, 그리고 HlsService를 사용하여
     * HLS 저장 Step을 생성하는 메서드
     * @param jobRepository  JobRepository 객체
     * @param tx             PlatformTransactionManager 객체
     * @param hlsService     HlsService 객체
     * @return 생성된 HLS 저장 Step
     */
    @Bean
    public Step createHlsStep(JobRepository jobRepository,
                            PlatformTransactionManager tx,
                            HlsService hlsService) {
        return new StepBuilder("createHlsStep", jobRepository)
                .tasklet((contrib, ctx) -> {
                    String title = ctx.getStepContext()
                            .getStepExecution()
                            .getJobParameters()
                            .getString("title");
                    hlsService.createHLS(title);
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    /**
     * 지정된 JobRepository와 Step을 사용하여 자막 생성 작업(Job)을 생성하는 메서드
     * @param jobRepository       JobRepository 객체
     * @param createSubtitleStep  자막 생성 Step
     * @return 생성된 자막 생성 Job
     */
    @Bean
    public Job createSubtitleJob(JobRepository jobRepository, Step createSubtitleStep) {
        return new JobBuilder("createSubtitleJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(createSubtitleStep)
                .build();
    }

    /**
     * 지정된 JobRepository, PlatformTransactionManager, 그리고 SubtitleService를 사용하여
     * 자막 생성 Step을 생성하는 메서드
     * @param jobRepository    JobRepository 객체
     * @param tx               PlatformTransactionManager 객체
     * @param subtitleService  SubtitleService 객체
     * @return 생성된 자막 생성 Step
     */
    @Bean
    public Step createSubtitleStep(JobRepository jobRepository,
                                   PlatformTransactionManager tx,
                                   SubtitleService subtitleService) {
        return new StepBuilder("createSubtitleStep", jobRepository)
                .tasklet((contrib, ctx) -> {
                    String title = ctx.getStepContext()
                            .getStepExecution()
                            .getJobParameters()
                            .getString("title");
                    subtitleService.createSubtitle(title);
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

}
