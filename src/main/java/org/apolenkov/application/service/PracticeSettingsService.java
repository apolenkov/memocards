package org.apolenkov.application.service;

import org.springframework.stereotype.Service;

@Service
public class PracticeSettingsService {
    private int defaultCount = 10;
    private boolean defaultRandomOrder = true;
    private String defaultDirection = "front_to_back"; // or back_to_front

    public int getDefaultCount() {
        return defaultCount;
    }

    public void setDefaultCount(int defaultCount) {
        this.defaultCount = Math.max(1, defaultCount);
    }

    public boolean isDefaultRandomOrder() {
        return defaultRandomOrder;
    }

    public void setDefaultRandomOrder(boolean defaultRandomOrder) {
        this.defaultRandomOrder = defaultRandomOrder;
    }

    public String getDefaultDirection() {
        return defaultDirection;
    }

    public void setDefaultDirection(String defaultDirection) {
        this.defaultDirection = defaultDirection;
    }
}
