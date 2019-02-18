package cn.wemarket.wxfront.common.autoconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@ComponentScan
@EnableScheduling
@EnableAsync
@EnableCaching
public class AutoConfiguration {
    @Value("${threadpool.core-pool-size:48}")
    private int corePoolSize;

    @Value("${threadpool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${threadpool.max-pool-size:48}")
    private int maxPoolSize;

    @Value("${threadpool.queue-capacity:300}")
    private int queueCapacity;

    @Value("${threadpool.allow-core-thread-timeout:true}")
    private boolean allowCoreThreadTimeOut;

    @Value("${threadpool.await-termination-seconds:60}")
    private int awaitTerminationSeconds;

    @Value("${threadpool.task.max-pool-size:8}")
    private int taskMaxPoolSize;

    @Value("${threadpool.wait-for-task-to-complete-on-shutdown:true}")
    private boolean waitForTasksToCompleteOnShutdown;

    @Bean(name = "frontTaskExecutor")
    public ThreadPoolTaskExecutor setFrontTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setAllowCoreThreadTimeOut(allowCoreThreadTimeOut);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }

    @Bean("messageSource")
    public ReloadableResourceBundleMessageSource messageSource(){
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }
}
