package tir.alex.doors.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@ConfigurationProperties(prefix = "sms")
public class SmsConfig {
    private Set<String> testNumbers;

    public Set<String> getTestNumbers() {
        return testNumbers;
    }

    public void setTestNumbers(Set<String> testNumbers) {
        this.testNumbers = testNumbers;
    }
}
