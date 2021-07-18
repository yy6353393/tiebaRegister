package top.decade.boot.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import top.decade.boot.domain.BootProperties;

import java.time.LocalDateTime;

@Configuration
@EnableScheduling
@PropertySource("classpath:/application.properties")
public class SaticScheduleTask {
    @Autowired
    BootProperties bootProperties;

    @Scheduled(cron = "${tieba.cron}")
    private void run() {
        System.err.println("执行静态定时任务时间: " + LocalDateTime.now());
        System.err.println("Bduss: " + bootProperties.getBduss());
        Register.app(bootProperties.getBduss());
    }
}

