package pk.wgu.capstone.data.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

@Entity(name = "budgets")
public class Budget extends AbstractEntity {

    @NotEmpty
    private String name;

    @NotNull
    private Date startDate;

    @NotNull
    private Date endDate;

    @NotNull
    private LocalDateTime dateCreated;

    @NotNull
    private BigDecimal spendingGoal;

    @NotEmpty
    private String description;

    @NotNull
    private Long userId;

    public Budget() {
    }

    public Budget(String name, Date start, Date end, BigDecimal spendingGoal,
                  String description, Long userId, LocalDateTime dateCreated) {
        this.name = name;
        this.startDate = start;
        this.endDate = end;
        this.spendingGoal = spendingGoal;
        this.description = description;
        this.userId = userId;
        this.dateCreated = dateCreated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStart() {
        return startDate;
    }

    public void setStart(Date start) {
        this.startDate = start;
    }

    public Date getEnd() {
        return endDate;
    }

    public void setEnd(Date end) {
        this.endDate = end;
    }

    public BigDecimal getSpendingGoal() {
        return spendingGoal;
    }

    public void setSpendingGoal(BigDecimal spendingGoal) {
        this.spendingGoal = spendingGoal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long usedId) {
        this.userId = usedId;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
}
