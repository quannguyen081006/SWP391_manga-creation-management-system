package manga.service.salary;

import java.math.BigDecimal;
import java.util.List;
import manga.common.exception.BusinessRuleException;
import manga.common.util.SessionUserUtil;
import manga.model.AuthenticatedUser;
import manga.model.salary.TaskTypeRate;
import manga.repository.salary.TaskTypeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskTypeRateService {

    @Autowired
    private TaskTypeRateRepository taskTypeRateRepository;

    public List<TaskTypeRate> listAll(AuthenticatedUser user) {
        requireAdmin(user);
        return taskTypeRateRepository.findAll();
    }

    public void updateRate(String code, BigDecimal ratePerPage, AuthenticatedUser user) {
        requireAdmin(user);
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessRuleException("Task type code is required");
        }
        if (ratePerPage == null || ratePerPage.signum() <= 0) {
            throw new BusinessRuleException("Rate per page must be greater than 0");
        }
        taskTypeRateRepository.updateRate(code.trim(), ratePerPage);
    }

    private void requireAdmin(AuthenticatedUser user) {
        try {
            SessionUserUtil.requireRole(user, "ADMIN", "Only ADMIN can update task type rates");
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException(ex.getMessage());
        }
    }
}
