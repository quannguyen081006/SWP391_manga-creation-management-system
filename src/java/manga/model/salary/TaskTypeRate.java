package manga.model.salary;

import java.math.BigDecimal;

public class TaskTypeRate {
    private String code;
    private String displayName;
    private BigDecimal ratePerPage;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BigDecimal getRatePerPage() {
        return ratePerPage;
    }

    public void setRatePerPage(BigDecimal ratePerPage) {
        this.ratePerPage = ratePerPage;
    }
}
