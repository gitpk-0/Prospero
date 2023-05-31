package pk.wgu.capstone.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

@Entity(name = "categories")
public class Category extends AbstractEntity {

    @NotBlank
    private String name;

    @NotNull
    @Enumerated
    private Type type;

    @NotBlank
    private String userIdsCsv;

    @NotNull
    private Boolean isDefault;

    public Category() {
    }

    public Category(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getUserIdsCsv() {
        return userIdsCsv;
    }

    public void setUserIdsCsv(String userIdsCsv) {
        this.userIdsCsv = userIdsCsv;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean hasUserId(Long userIdToFind) {
        List<Long> userIds = Arrays.stream(this.userIdsCsv.split(","))
                .map(Long::valueOf)
                .toList();

        return userIds.contains(userIdToFind);
    }

    public boolean isDefaultCategory() {
        return this.isDefault;
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", userIds=" + userIdsCsv +
                ", isDefault=" + isDefault +
                '}';
    }
}
