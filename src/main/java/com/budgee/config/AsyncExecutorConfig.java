package com.budgee.config;

import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@Slf4j(topic = "ASYNC-EXECUTOR-CONFIG")
public class AsyncExecutorConfig implements AsyncConfigurer {

    @NonFinal
    @Value("${budgee.executor.mail.max-size}")
    int EXECUTOR_MAIL_MAX_SIZE;

    @NonFinal
    @Value("${budgee.executor.mail.core-size}")
    int EXECUTOR_MAIL_CORE_SIZE;

    // -----------------------------------------
    // MAIL EXECUTOR
    // -----------------------------------------
    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        log.info("[mailExecutor] create mailExecutor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(EXECUTOR_MAIL_CORE_SIZE);
        executor.setMaxPoolSize(EXECUTOR_MAIL_MAX_SIZE);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("MailExecutor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return AsyncConfigurer.super.getAsyncUncaughtExceptionHandler();
    }
}
