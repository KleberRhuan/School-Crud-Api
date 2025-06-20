/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig {

  @Bean(name = "mailTaskExecutor")
  public TaskExecutor mailTaskExecutor() {
    var executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(8);
    executor.setMaxPoolSize(24);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("MailExecutor‑");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.setRejectedExecutionHandler(
      new ThreadPoolExecutor.CallerRunsPolicy()
    );

    executor.initialize();
    return executor;
  }
}
