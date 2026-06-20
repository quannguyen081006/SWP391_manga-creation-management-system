package manga.service.salary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.logging.Logger;
import manga.common.exception.BusinessRuleException;
import manga.common.util.SessionUserUtil;
import manga.model.salary.AssistantSalaryRecord;
import manga.model.AuthenticatedUser;
import manga.model.salary.SalarySettings;
import manga.repository.salary.AssistantSalaryRecordRepository;
import manga.repository.salary.SalaryPeriodRepository;
import manga.repository.chaptertask.PageTaskRepository;
import manga.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalaryService {

    private static final Logger LOGGER = Logger.getLogger(SalaryService.class.getName());

    @Autowired
    private SalaryPeriodRepository salaryPeriodRepository;

    @Autowired
    private AssistantSalaryRecordRepository assistantSalaryRecordRepository;

    @Autowired
    private SalarySettingsService salarySettingsService;

    @Autowired
    private PageTaskRepository pageTaskRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Map<String, Object>> listMyPeriods(AuthenticatedUser user) {
        requireMangaka(user, "Only MANGAKA can view salary periods");
        return salaryPeriodRepository.listPeriodsByMangaka(user.getId());
    }

    public Map<String, Object> getPeriodOwnedByUser(long periodId, AuthenticatedUser user) {
        Map<String, Object> period = salaryPeriodRepository.findById(periodId);
        if (period == null) {
            throw new BusinessRuleException("Salary period not found");
        }
        Number ownerId = (Number) period.get("mangakaId");
        if (user == null || !user.hasRole("MANGAKA")
                || ownerId == null || ownerId.longValue() != user.getId()) {
            throw new BusinessRuleException("You do not own this salary period");
        }
        return period;
    }

    @Transactional
    public void calculate(long periodId, AuthenticatedUser user) {
        Map<String, Object> period = getPeriodOwnedByUser(periodId, user);
        if (!"OPEN".equals(period.get("status"))) {
            throw new BusinessRuleException("Only OPEN period can be (re)calculated");
        }
        calculateForPeriod(periodId, user.getId());
    }

    private void calculateForPeriod(long periodId, long mangakaId) {
        SalarySettings settings = salarySettingsService.getSettings();
        List<AssistantSalaryRecord> rows = assistantSalaryRecordRepository.calculatePreview(
                periodId, mangakaId,
                settings.getKpiOnTimeWeight(), settings.getKpiQualityWeight());
        for (AssistantSalaryRecord row : rows) {
            BigDecimal suggestedBonus = calculateSuggestedBonus(
                    row.getKpiScore(), row.getGrossSalary(), settings);
            int lateTaskCount = assistantSalaryRecordRepository.countLateTasks(
                    periodId, row.getAssistantId());
            BigDecimal suggestedDeduction = settings.getPenaltyPerLateTask()
                    .multiply(new BigDecimal(lateTaskCount));
            int heavyRejectedCount = assistantSalaryRecordRepository.countHeavyRejectedTasks(
                    periodId, row.getAssistantId(),
                    settings.getRejectionPenaltyThreshold());
            BigDecimal rejectPenalty = settings.getPenaltyPerRejectedTask()
                    .multiply(new BigDecimal(heavyRejectedCount));
            suggestedDeduction = suggestedDeduction.add(rejectPenalty);
            row.setHeavyRejectedTaskCount(heavyRejectedCount);
            row.setSuggestedBonus(suggestedBonus);
            row.setSuggestedDeduction(suggestedDeduction);
            row.setBonus(suggestedBonus);
            row.setDeduction(suggestedDeduction);
        }
        assistantSalaryRecordRepository.upsertCalculated(periodId, rows);
    }

    @Transactional
    public void settle(long periodId, AuthenticatedUser user) {
        Map<String, Object> period = getPeriodOwnedByUser(periodId, user);
        if (!"OPEN".equals(period.get("status"))) {
            throw new BusinessRuleException("Period already settled");
        }
        if (!assistantSalaryRecordRepository.existsForPeriod(periodId)) {
            throw new BusinessRuleException("Calculate salary at least once before settling");
        }
        calculateForPeriod(periodId, user.getId());
        List<Long> assistantIds = assistantSalaryRecordRepository.findAssistantIdsByPeriod(periodId);
        salaryPeriodRepository.markSettled(periodId);
        assistantSalaryRecordRepository.markPeriodTasksSalaried(periodId);
        String periodName = String.valueOf(period.get("name"));
        for (Long assistantId : assistantIds) {
            notificationService.notifyUser(
                    assistantId.longValue(),
                    "SALARY_SETTLED",
                    "K\u1ef3 l\u01b0\u01a1ng \"" + periodName
                            + "\" \u0111\u00e3 \u0111\u01b0\u1ee3c t\u1ea5t to\u00e1n. "
                            + "Xem chi ti\u1ebft t\u1ea1i trang L\u01b0\u01a1ng c\u1ee7a b\u1ea1n.",
                    periodId,
                    "SALARY_PERIOD");
        }
    }

    public List<Map<String, Object>> getRecords(long periodId, AuthenticatedUser user) {
        getPeriodOwnedByUser(periodId, user);
        SalarySettings settings = salarySettingsService.getSettings();
        List<Map<String, Object>> rows = assistantSalaryRecordRepository.findByPeriodId(periodId);
        for (Map<String, Object> row : rows) {
            long assistantId = ((Number) row.get("assistantId")).longValue();
            BigDecimal kpiScore = (BigDecimal) row.get("kpiScore");
            BigDecimal grossSalary = (BigDecimal) row.get("grossSalary");
            int lateTaskCount = assistantSalaryRecordRepository.countLateTasks(
                    periodId, assistantId);
            int heavyRejectedCount = assistantSalaryRecordRepository.countHeavyRejectedTasks(
                    periodId, assistantId, settings.getRejectionPenaltyThreshold());
            BigDecimal suggestedDeduction = settings.getPenaltyPerLateTask()
                    .multiply(new BigDecimal(lateTaskCount))
                    .add(settings.getPenaltyPerRejectedTask()
                            .multiply(new BigDecimal(heavyRejectedCount)));
            row.put("suggestedBonus", calculateSuggestedBonus(kpiScore, grossSalary, settings));
            row.put("suggestedDeduction", suggestedDeduction);
            row.put("lateTaskCount", lateTaskCount);
            row.put("heavyRejectedTaskCount", heavyRejectedCount);
            row.put("lateDeduction", settings.getPenaltyPerLateTask()
                    .multiply(new BigDecimal(lateTaskCount)));
            row.put("rejectionDeduction", settings.getPenaltyPerRejectedTask()
                    .multiply(new BigDecimal(heavyRejectedCount)));
            row.put("tasks", loadTaskBreakdown(periodId, assistantId, settings));
        }
        return rows;
    }

    public List<AssistantSalaryRecord> getMySettledSalaryRecords(AuthenticatedUser user) {
        try {
            SessionUserUtil.requireRole(user, "ASSISTANT", "Only ASSISTANT can view this salary page");
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException(ex.getMessage());
        }
        List<AssistantSalaryRecord> rows =
                assistantSalaryRecordRepository.findSettledByAssistant(user.getId());
        for (AssistantSalaryRecord row : rows) {
            row.setTasks(loadTaskBreakdown(
                    row.getPeriodId(), user.getId(), salarySettingsService.getSettings()));
        }
        return rows;
    }

    private List<Map<String, Object>> loadTaskBreakdown(long periodId,
            long assistantId, SalarySettings settings) {
        List<Map<String, Object>> tasks =
                pageTaskRepository.findApprovedTasksForSalary(periodId, assistantId);
        for (Map<String, Object> task : tasks) {
            List<String> reasons = new ArrayList<String>();
            BigDecimal deduction = BigDecimal.ZERO;
            if (!Boolean.TRUE.equals(task.get("onTime"))) {
                int daysLate = ((Number) task.get("daysLate")).intValue();
                deduction = deduction.add(settings.getPenaltyPerLateTask());
                reasons.add("Overdue by " + daysLate + " day"
                        + (daysLate == 1 ? "" : "s"));
            }
            int rejectionCount = ((Number) task.get("rejectionCount")).intValue();
            if (rejectionCount >= settings.getRejectionPenaltyThreshold()) {
                deduction = deduction.add(settings.getPenaltyPerRejectedTask());
                reasons.add("Rejected " + rejectionCount + " times"
                        + " (threshold: "
                        + settings.getRejectionPenaltyThreshold() + ")");
            }
            task.put("deductionReasons", reasons);
            task.put("deductionAmount", deduction);
        }
        return tasks;
    }

    public void autoCreateAndCalculate() {
        List<Long> mangakaIds = salaryPeriodRepository.findMangakasWithUnsalariedApprovedTasks();
        if (mangakaIds.isEmpty()) {
            LOGGER.warning("Salary generation skipped: no approved unsalaried tasks");
            return;
        }
        for (Long mangakaId : mangakaIds) {
            autoCreateAndCalculateForMangaka(mangakaId.longValue(), false);
        }
    }

    public long autoCreateAndCalculate(AuthenticatedUser user) {
        requireMangaka(user, "Only MANGAKA can generate a salary period");
        return autoCreateAndCalculateForMangaka(user.getId(), true);
    }

    private long autoCreateAndCalculateForMangaka(long mangakaId, boolean failWhenSkipped) {
        Long openPeriodId = salaryPeriodRepository.findOpenPeriodId(mangakaId);
        if (openPeriodId != null) {
            calculateForPeriod(openPeriodId.longValue(), mangakaId);
            return openPeriodId.longValue();
        }
        String periodName = currentAutoPeriodName();
        if (!salaryPeriodRepository.hasUnsalariedApprovedTasks(mangakaId)) {
            String message = "No approved unsalaried tasks are available";
            if (failWhenSkipped) {
                throw new BusinessRuleException(message);
            }
            LOGGER.warning(message + " for Mangaka #" + mangakaId);
            return 0L;
        }
        long periodId = salaryPeriodRepository.createPeriod(mangakaId, periodName);
        calculateForPeriod(periodId, mangakaId);
        return periodId;
    }

    private String currentAutoPeriodName() {
        LocalDate today = LocalDate.now();
        String month = today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return "Auto - " + month + " " + today.getYear();
    }

    public void refreshOpenPeriod(long periodId, AuthenticatedUser user) {
        Map<String, Object> period = getPeriodOwnedByUser(periodId, user);
        if ("OPEN".equals(period.get("status"))) {
            calculate(periodId, user);
        }
    }

    private BigDecimal calculateSuggestedBonus(BigDecimal kpiScore,
            BigDecimal grossSalary, SalarySettings settings) {
        if (kpiScore == null || grossSalary == null
                || kpiScore.compareTo(new BigDecimal(settings.getKpiBonusThreshold())) < 0) {
            return BigDecimal.ZERO;
        }
        return grossSalary.multiply(settings.getBonusPercent())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private void requireMangaka(AuthenticatedUser user, String message) {
        try {
            SessionUserUtil.requireRole(user, "MANGAKA", message);
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException(ex.getMessage());
        }
    }
}
