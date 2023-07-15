package com.prography1.eruna.batch.scheduler;


import com.prography1.eruna.config.FCMConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;


@SpringBatchTest
@SpringJUnitConfig(TestBatchConfig.class)
@Import({CustomConfig.class, FCMConfig.class})
@EnableJpaRepositories("com.prography1.eruna.domain.repository")
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BatchJobLaunchTests {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;


    @BeforeEach
    public void setup(@Autowired Job job) {
        this.jobLauncherTestUtils.setJobRepository(jobRepository);
        this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
        this.jobLauncherTestUtils.setJob(job); // this is optional if the job is unique
        this.jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    public void testMyJob() throws Exception {
        // given
        JobParameters jobParameters = this.jobLauncherTestUtils.getUniqueJobParameters();


        // when
        JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);

        // then
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }

}
