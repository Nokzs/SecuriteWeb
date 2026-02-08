package com.example.gatewaybff.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class LogoutProperties {
    private String param = "app"; 
    private Redirect redirect = new Redirect();

    public static class Redirect {
        private String defaultValue; 
        private Map<String, String> apps = new HashMap<>(); 

        // Getters et Setters
        public String getDefault() {
            return defaultValue;
        }

        public void setDefault(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Map<String, String> getApps() {
            return apps;
        }

        public void setApps(Map<String, String> apps) {
            this.apps = apps;
        }
    }

    // Getters et Setters pour AppProperties
    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Redirect getRedirect() {
        return redirect;
    }

    public void setRedirect(Redirect redirect) {
        this.redirect = redirect;
    }
}
