package manga.repository.salary;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import manga.model.salary.TaskTypeRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TaskTypeRateRepository {

    @Autowired
    private DataSource dataSource;

    public List<TaskTypeRate> findAll() {
        String sql = "SELECT code, ratePerPage FROM TaskType ORDER BY code ASC";
        List<TaskTypeRate> rows = new ArrayList<TaskTypeRate>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TaskTypeRate row = new TaskTypeRate();
                row.setCode(rs.getString("code"));
                row.setDisplayName(displayNameForCode(row.getCode()));
                row.setRatePerPage(rs.getBigDecimal("ratePerPage"));
                rows.add(row);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot list task type rates", ex);
        }
        return rows;
    }

    public void updateRate(String code, BigDecimal ratePerPage) {
        String sql = "UPDATE TaskType SET ratePerPage = ?, updatedAt = GETDATE() "
                + "WHERE UPPER(code) = UPPER(?)";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, ratePerPage);
            ps.setString(2, code);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Task type not found");
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot update task type rate", ex);
        }
    }

    private String displayNameForCode(String code) {
        if (code == null) {
            return "";
        }
        String normalized = code.trim().toUpperCase();
        if ("SKETCHING".equals(normalized)) {
            return "Sketching";
        }
        if ("INKING".equals(normalized)) {
            return "Inking";
        }
        if ("COLORING".equals(normalized)) {
            return "Coloring";
        }
        if ("SCREENTONE".equals(normalized)) {
            return "Screentone";
        }
        if ("LETTERING".equals(normalized)) {
            return "Lettering";
        }
        if ("MIXED".equals(normalized)) {
            return "Mixed";
        }
        return code;
    }
}
