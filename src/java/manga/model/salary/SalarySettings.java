package manga.model.salary;

import java.math.BigDecimal;

public class SalarySettings {
    private int kpiBonusThreshold;
    private BigDecimal bonusPercent;
    private BigDecimal penaltyPerLateTask;
    private int rejectionPenaltyThreshold;
    private BigDecimal penaltyPerRejectedTask;
    private int kpiOnTimeWeight;
    private int kpiQualityWeight;

    public int getKpiBonusThreshold() {
        return kpiBonusThreshold;
    }

    public void setKpiBonusThreshold(int kpiBonusThreshold) {
        this.kpiBonusThreshold = kpiBonusThreshold;
    }

    public BigDecimal getBonusPercent() {
        return bonusPercent;
    }

    public void setBonusPercent(BigDecimal bonusPercent) {
        this.bonusPercent = bonusPercent;
    }

    public BigDecimal getPenaltyPerLateTask() {
        return penaltyPerLateTask;
    }

    public void setPenaltyPerLateTask(BigDecimal penaltyPerLateTask) {
        this.penaltyPerLateTask = penaltyPerLateTask;
    }

    public int getRejectionPenaltyThreshold() {
        return rejectionPenaltyThreshold;
    }

    public void setRejectionPenaltyThreshold(int rejectionPenaltyThreshold) {
        this.rejectionPenaltyThreshold = rejectionPenaltyThreshold;
    }

    public BigDecimal getPenaltyPerRejectedTask() {
        return penaltyPerRejectedTask;
    }

    public void setPenaltyPerRejectedTask(BigDecimal penaltyPerRejectedTask) {
        this.penaltyPerRejectedTask = penaltyPerRejectedTask;
    }

    public int getKpiOnTimeWeight() {
        return kpiOnTimeWeight;
    }

    public void setKpiOnTimeWeight(int kpiOnTimeWeight) {
        this.kpiOnTimeWeight = kpiOnTimeWeight;
    }

    public int getKpiQualityWeight() {
        return kpiQualityWeight;
    }

    public void setKpiQualityWeight(int kpiQualityWeight) {
        this.kpiQualityWeight = kpiQualityWeight;
    }
}
