package manga.service.salary;

import java.math.BigDecimal;
import manga.model.salary.SalarySettings;
import manga.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SalarySettingsService {

    public static final int DEFAULT_KPI_BONUS_THRESHOLD = 90;
    public static final BigDecimal DEFAULT_BONUS_PERCENT = new BigDecimal("5");
    public static final BigDecimal DEFAULT_PENALTY_PER_LATE_TASK = new BigDecimal("50000");

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    public SalarySettings getSettings() {
        SalarySettings settings = new SalarySettings();
        settings.setKpiBonusThreshold(systemSettingRepository.getInt(
                SystemSettingRepository.SALARY_KPI_BONUS_THRESHOLD,
                DEFAULT_KPI_BONUS_THRESHOLD));
        settings.setBonusPercent(systemSettingRepository.getDecimal(
                SystemSettingRepository.SALARY_BONUS_PERCENT,
                DEFAULT_BONUS_PERCENT));
        settings.setPenaltyPerLateTask(systemSettingRepository.getDecimal(
                SystemSettingRepository.SALARY_PENALTY_PER_LATE_TASK,
                DEFAULT_PENALTY_PER_LATE_TASK));
        return settings;
    }

    public void updateSettings(int kpiBonusThreshold, BigDecimal bonusPercent,
            BigDecimal penaltyPerLateTask) {
        if (kpiBonusThreshold < 0 || kpiBonusThreshold > 100) {
            throw new IllegalArgumentException("KPI bonus threshold must be between 0 and 100");
        }
        if (bonusPercent == null || bonusPercent.signum() < 0
                || bonusPercent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Bonus percent must be between 0 and 100");
        }
        if (penaltyPerLateTask == null || penaltyPerLateTask.signum() < 0) {
            throw new IllegalArgumentException("Penalty per late task cannot be negative");
        }
        systemSettingRepository.setSalarySettings(
                kpiBonusThreshold, bonusPercent, penaltyPerLateTask);
    }
}
