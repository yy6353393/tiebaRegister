package top.decade.boot.domain;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tieba")
public class BootProperties {
    private String bduss;
    private String cron;

    public String getBduss() {
        return bduss;
    }

    public String getCron() {
        return cron;
    }

    public void setBduss(String bduss) {
        this.bduss = bduss;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }
}
