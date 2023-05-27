package pk.wgu.capstone.data.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;

@Entity(name = "categories")
public class Category extends AbstractEntity {

    @NotBlank
    private String name;

    public Category() {}

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
