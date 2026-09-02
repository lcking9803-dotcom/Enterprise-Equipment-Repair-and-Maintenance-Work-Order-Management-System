package com.lwq.maintenance.domain.enums;

public enum Priority {
    LOW(72), MEDIUM(48), HIGH(24), URGENT(4);

    private final int slaHours;

    Priority(int slaHours) {
        this.slaHours = slaHours;
    }

    public int getSlaHours() {
        return slaHours;
    }
}

