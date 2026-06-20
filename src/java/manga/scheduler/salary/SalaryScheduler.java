package manga.scheduler.salary;

import manga.service.salary.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SalaryScheduler {

    @Autowired
    private SalaryService salaryService;

    @Scheduled(cron = "0 0 0 5 * *")
    public void autoMonthly() {
        salaryService.autoCreateAndCalculate();
    }
}
