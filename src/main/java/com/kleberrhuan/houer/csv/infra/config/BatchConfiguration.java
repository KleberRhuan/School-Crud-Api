/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.config;

import com.kleberrhuan.houer.csv.domain.model.CsvSchoolRecord;
import com.kleberrhuan.houer.csv.infra.batch.SchoolItemProcessor;
import com.kleberrhuan.houer.csv.infra.batch.SchoolItemWriter;
import com.kleberrhuan.houer.csv.infra.batch.listener.CsvImportJobListener;
import com.kleberrhuan.houer.csv.infra.batch.listener.CsvImportStepListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfiguration {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final SchoolItemProcessor itemProcessor;
  private final CsvImportJobListener jobListener;
  private final CsvImportStepListener stepListener;

  /** Job principal de importação de escolas. */
  @Bean
  public Job schoolImportJob(Step schoolImportStep) {
    return new JobBuilder("schoolImportJob", jobRepository)
      .incrementer(new RunIdIncrementer())
      .listener(jobListener)
      .start(schoolImportStep)
      .build();
  }

  /** Step de processamento dos dados do CSV. */
  @Bean
  public Step schoolImportStep(
    FlatFileItemReader<CsvSchoolRecord> csvSchoolReader,
    SchoolItemWriter itemWriter
  ) {
    return new StepBuilder("schoolImportStep", jobRepository)
      .<CsvSchoolRecord, CsvSchoolRecord>chunk(2_000, transactionManager)
      .reader(csvSchoolReader)
      .processor(itemProcessor)
      .writer(itemWriter)
      .listener(stepListener)
      .taskExecutor(batchTaskExecutor())
      .build();
  }

  /** Executor de tarefas para processamento paralelo. */
  @Bean
  public TaskExecutor batchTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("batch-school-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
  }
}
