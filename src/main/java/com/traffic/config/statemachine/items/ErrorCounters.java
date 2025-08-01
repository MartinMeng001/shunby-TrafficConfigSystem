package com.traffic.config.statemachine.items;

import java.time.LocalDateTime;
import java.time.Duration;

// 错误计数器类
public class ErrorCounters {
    private int counterMismatch = 0;
    private int idLogicErrors = 0;
    private LocalDateTime lastResetTime = LocalDateTime.now();

    public void incrementCounterMismatch() {
        this.counterMismatch++;
    }

    public void incrementIdLogicErrors() {
        this.idLogicErrors++;
    }

    public boolean shouldExitInductiveMode() {
        Duration duration = Duration.between(lastResetTime, LocalDateTime.now());
        return duration.toMinutes() <= 60 &&
                (counterMismatch >= 5 || idLogicErrors >= 10);
    }

    public void reset() {
        this.counterMismatch = 0;
        this.idLogicErrors = 0;
        this.lastResetTime = LocalDateTime.now();
    }

    // Getters
    public int getCounterMismatch() { return counterMismatch; }
    public int getIdLogicErrors() { return idLogicErrors; }
}
