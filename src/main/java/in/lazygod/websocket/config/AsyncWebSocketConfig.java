package in.lazygod.websocket.config;

import in.lazygod.websocket.model.SessionWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncWebSocketConfig {

    @Bean("wsExecutor")
    public Executor wsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ws-");
        executor.initialize();
        SessionWrapper.setExecutor(executor);
        return executor;
    }
}
