package org.apolenkov.application.service;

import org.apolenkov.application.model.PracticeDirection;
import org.springframework.stereotype.Service;

@Service
public class PracticeSettingsService {
    private int defaultCount = 10;
    private boolean defaultRandomOrder = true;
    private PracticeDirection defaultDirection = PracticeDirection.FRONT_TO_BACK;

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

    public PracticeDirection getDefaultDirection() {
        return defaultDirection;
    }

    public void setDefaultDirection(PracticeDirection defaultDirection) {
        this.defaultDirection = defaultDirection;
    }
}
