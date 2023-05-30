package pk.wgu.capstone.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity(name = "categories")
public class Category extends AbstractEntity {

    @NotBlank
    private String name;

    @NotNull
    @Enumerated
    private Type type;

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
}
