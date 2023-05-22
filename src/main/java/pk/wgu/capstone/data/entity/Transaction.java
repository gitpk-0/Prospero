package pk.wgu.capstone.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.sql.Date;

@Entity
public class Transaction extends AbstractEntity {

    @NotNull
    private Date date;

    @NotNull
    private BigDecimal amount;

    @NotEmpty
    private String description;

    @ManyToOne
    @NotNull
    private Category category;

    @NotNull
    @Enumerated
    private Type type;

    private Long userId;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "date=" + date +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", category=" + category.getName() +
                ", type=" + type +
                ", userId=" + userId +
                '}';
    }
}
