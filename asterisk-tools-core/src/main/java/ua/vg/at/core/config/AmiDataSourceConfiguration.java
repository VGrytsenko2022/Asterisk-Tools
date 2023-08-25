package ua.vg.at.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmiDataSourceConfiguration {
    @Value("${ami.ip}")
    private String amipi;

    @Value("${ami.username}")
    private String username;

    @Value("${ami.password}")
    private String password;

    public String getAmipi() {
        return amipi;
    }

    public void setAmipi(String amipi) {
        this.amipi = amipi;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
