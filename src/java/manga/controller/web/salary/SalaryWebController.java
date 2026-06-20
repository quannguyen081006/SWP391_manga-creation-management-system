package manga.controller.web.salary;

import javax.servlet.http.HttpSession;
import manga.common.util.SessionUserUtil;
import manga.model.AuthenticatedUser;
import manga.service.salary.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/main/salary")
public class SalaryWebController {

    @Autowired
    private SalaryService salaryService;

    @RequestMapping(value = "/periods", method = RequestMethod.GET)
    public String periods(HttpSession session, Model model) {
        AuthenticatedUser user = SessionUserUtil.requireUser(session);
        model.addAttribute("periods", salaryService.listMyPeriods(user));
        return "salary/period";
    }

    @RequestMapping(value = "/periods/generate", method = RequestMethod.POST)
    public String generate(HttpSession session, Model model) {
        AuthenticatedUser user = SessionUserUtil.requireUser(session);
        try {
            long periodId = salaryService.autoCreateAndCalculate(user);
            return "redirect:/main/salary/periods/" + periodId;
        } catch (RuntimeException ex) {
            model.addAttribute("periods", salaryService.listMyPeriods(user));
            model.addAttribute("error", ex.getMessage());
            return "salary/period";
        }
    }

    @RequestMapping(value = "/my", method = RequestMethod.GET)
    public String mySalary(HttpSession session, Model model) {
        AuthenticatedUser user = SessionUserUtil.requireUser(session);
        model.addAttribute("records", salaryService.getMySettledSalaryRecords(user));
        return "salary/assistant-salary";
    }

    @RequestMapping(value = "/periods/{id}", method = RequestMethod.GET)
    public String detail(@PathVariable("id") long id, HttpSession session, Model model) {
        AuthenticatedUser user = SessionUserUtil.requireUser(session);
        try {
            salaryService.refreshOpenPeriod(id, user);
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        loadDetail(id, user, model);
        return "salary/detail";
    }

    @RequestMapping(value = "/periods/{id}/calculate", method = RequestMethod.POST)
    public String calculate(@PathVariable("id") long id, HttpSession session, Model model) {
        AuthenticatedUser user = SessionUserUtil.requireUser(session);
        try {
            salaryService.calculate(id, user);
            return "redirect:/main/salary/periods/" + id;
        } catch (RuntimeException ex) {
            loadDetail(id, user, model);
            model.addAttribute("error", ex.getMessage());
            return "salary/detail";
        }
    }

    @RequestMapping(value = "/periods/{id}/settle", method = RequestMethod.POST)
    public String settle(@PathVariable("id") long id, HttpSession session, Model model) {
        AuthenticatedUser user = SessionUserUtil.requireUser(session);
        try {
            salaryService.settle(id, user);
            return "redirect:/main/salary/periods/" + id;
        } catch (RuntimeException ex) {
            loadDetail(id, user, model);
            model.addAttribute("error", ex.getMessage());
            return "salary/detail";
        }
    }

    private void loadDetail(long periodId, AuthenticatedUser user, Model model) {
        model.addAttribute("period", salaryService.getPeriodOwnedByUser(periodId, user));
        model.addAttribute("records", salaryService.getRecords(periodId, user));
    }
}
