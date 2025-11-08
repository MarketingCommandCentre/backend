package com.ibrasoft.commandcentre.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class CycleService {
    
    @Value("${app.cycle.latest-start-date}")
    private String latestCycleStartDate;
    
    private static final int CYCLE_LENGTH_WEEKS = 2;
    private static final int CYCLE_LENGTH_DAYS = CYCLE_LENGTH_WEEKS * 7;
    
    @Getter
    public static class CycleInfo {
        private final int cycleNumber;
        private final LocalDate developmentStart;
        private final LocalDate developmentEnd;
        private final LocalDate postingStart;
        private final LocalDate postingEnd;
        
        public CycleInfo(int cycleNumber, LocalDate developmentStart) {
            this.cycleNumber = cycleNumber;
            this.developmentStart = developmentStart;
            this.developmentEnd = developmentStart.plusDays(CYCLE_LENGTH_DAYS - 1);
            this.postingStart = this.developmentEnd.plusDays(1);
            this.postingEnd = this.postingStart.plusDays(CYCLE_LENGTH_DAYS - 1);
        }
    }
    
    /**
     * Get the current development cycle (requests being created now)
     */
    public CycleInfo getCurrentDevelopmentCycle() {
        LocalDate today = LocalDate.now();
        return getCycleForDevelopmentDate(today);
    }
    
    /**
     * Get the current posting cycle (requests being posted now)
     */
    public CycleInfo getCurrentPostingCycle() {
        LocalDate today = LocalDate.now();
        return getCycleForPostingDate(today);
    }
    
    /**
     * Get the cycle info for a specific posting date
     */
    public CycleInfo getCycleForPostingDate(LocalDate postingDate) {
        LocalDate referenceStart = LocalDate.parse(latestCycleStartDate);
        
        // Calculate how many days between reference and posting date
        long daysBetween = ChronoUnit.DAYS.between(referenceStart, postingDate);
        
        // Posting happens 2 weeks after development start
        // So subtract 14 days to get the development start
        long daysFromReferenceToDevelopmentStart = daysBetween - CYCLE_LENGTH_DAYS;
        
        // Calculate which cycle this is
        int cyclesSinceReference = (int) Math.floor((double) daysFromReferenceToDevelopmentStart / CYCLE_LENGTH_DAYS);
        
        // Calculate the development start date for this cycle
        LocalDate developmentStart = referenceStart.plusDays(cyclesSinceReference * CYCLE_LENGTH_DAYS);
        
        // Cycle number (starting from 1 for the reference date)
        int cycleNumber = cyclesSinceReference + 1;
        
        return new CycleInfo(cycleNumber, developmentStart);
    }
    
    /**
     * Get the cycle info for a specific development date
     */
    public CycleInfo getCycleForDevelopmentDate(LocalDate developmentDate) {
        LocalDate referenceStart = LocalDate.parse(latestCycleStartDate);
        
        // Calculate how many days between reference and development date
        long daysBetween = ChronoUnit.DAYS.between(referenceStart, developmentDate);
        
        // Calculate which cycle this is
        int cyclesSinceReference = (int) Math.floor((double) daysBetween / CYCLE_LENGTH_DAYS);
        
        // Calculate the development start date for this cycle
        LocalDate developmentStart = referenceStart.plusDays(cyclesSinceReference * CYCLE_LENGTH_DAYS);
        
        // Cycle number (starting from 1 for the reference date)
        int cycleNumber = cyclesSinceReference + 1;
        
        return new CycleInfo(cycleNumber, developmentStart);
    }
    
    /**
     * Check if a posting date falls within the current posting cycle
     */
    public boolean isInCurrentPostingCycle(LocalDate postingDate) {
        CycleInfo currentPostingCycle = getCurrentPostingCycle();
        return !postingDate.isBefore(currentPostingCycle.getPostingStart()) 
            && !postingDate.isAfter(currentPostingCycle.getPostingEnd());
    }
    
    /**
     * Check if a posting date falls within the current development cycle's posting period
     */
    public boolean isInCurrentDevelopmentCyclePostingPeriod(LocalDate postingDate) {
        CycleInfo currentDevelopmentCycle = getCurrentDevelopmentCycle();
        return !postingDate.isBefore(currentDevelopmentCycle.getPostingStart()) 
            && !postingDate.isAfter(currentDevelopmentCycle.getPostingEnd());
    }
}
