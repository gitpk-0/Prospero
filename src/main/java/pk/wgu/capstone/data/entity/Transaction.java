package pk.wgu.capstone.data.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
public class Transaction extends AbstractEntity{

    @NotEmpty
    private LocalDate date;

    @NotEmpty
    private Double amount;

    @NotEmpty
    private String description;

    @NotNull
    private Category category;

    @NotEmpty
    private Type type;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
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

    @Override
    public String toString() {
        return "Transaction{" +
                "date=" + date +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", type=" + type +
                '}';
    }
}
