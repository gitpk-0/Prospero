package pk.wgu.capstone.data.entity;


import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class User extends AbstractEntity {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    private boolean allowsMarketingEmails;

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
}
