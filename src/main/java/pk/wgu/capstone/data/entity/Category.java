package pk.wgu.capstone.data.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity(name = "categories")
public class Category extends AbstractEntity {

    @NotBlank
    private String name;

    @NotNull
    @Enumerated
    private Type type;

    @ElementCollection
    private List<Long> userIds;

    @NotNull
    private Boolean isDefault;

    public Category() {}

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

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", userIds=" + userIds +
                ", isDefault=" + isDefault +
                '}';
    }
}
