package pk.wgu.capstone.data.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity(name = "users")
public class User extends AbstractEntity {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    @Column(name = "username")
    private String email;

    private boolean allowsMarketingEmails;

    private boolean enabled = true;

    @NotNull
    @Enumerated
    private Role role;

    // Need to encrypt in production
    @Size(min = 8, max = 128, message = "Password must be at least 8 characters, no more than 128 characters")
    private String password;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAllowsMarketingEmails() {
        return allowsMarketingEmails;
    }

    public void setAllowsMarketingEmails(boolean allowsMarketingEmails) {
        this.allowsMarketingEmails = allowsMarketingEmails;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
