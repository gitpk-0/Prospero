package pk.wgu.capstone.data.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.sql.Date;

@Entity(name = "budgets")
public class Budget extends AbstractEntity {

    @NotEmpty
    private String name;

    @NotNull
    private Date start;

    @NotNull
    private Date end;

    @NotNull
    private Date dateCreated;

    @NotNull
    private BigDecimal spendingGoal;

    @NotEmpty
    private String description;

    private Long userId;

    public Budget() {
    }

    public Budget(String name, Date start, Date end, BigDecimal spendingGoal,
                  String description, Long userId) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.spendingGoal = spendingGoal;
        this.description = description;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
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

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
