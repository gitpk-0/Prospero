package pk.wgu.capstone.data.binder;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import pk.wgu.capstone.data.entity.*;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.views.LoginView;
import pk.wgu.capstone.views.forms.RegistrationForm;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegistrationFormBinder {

    private RegistrationForm registrationForm;
    private PfmService service;
    private PasswordEncoder passwordEncoder;

    private boolean enablePasswordValidation;

    public RegistrationFormBinder(RegistrationForm registrationForm,
                                  PfmService service,
                                  PasswordEncoder passwordEncoder) {
        this.registrationForm = registrationForm;
        this.service = service;
        this.passwordEncoder = passwordEncoder;
    }

    // Add the data binding and validation logic to the registration form
    public void addBindingAndValidation() {
        BeanValidationBinder<User> binder = new BeanValidationBinder<>(User.class);
        binder.bindInstanceFields(registrationForm);

        // custom validation for password fields
        binder.forField(registrationForm.getPassword())
                .withValidator(this::passwordValidator).bind("password");

        binder.forField(registrationForm.getEmail())
                .withValidator(this::emailValidator).bind("email");

        registrationForm.getPasswordConfirmation().addValueChangeListener(e -> {
            // user has changed the second password field, validate and show errors
            enablePasswordValidation = true;
            binder.validate();
        });

        binder.setStatusLabel(registrationForm.getErrorMessage());

        registrationForm.getSubmit().addClickListener(e -> {
            try {
                User userBean = new User(); // new bean to store user info into
                userBean.setRole(Role.USER);

                binder.writeBean(userBean); // run validation and write the values to the bean
                userBean.setPassword(passwordEncoder.encode(userBean.getPassword()));
                userBean.setAllowsMarketingEmails(registrationForm.getAllowMarketing().getValue());

                service.addNewUser(userBean);// add the new user to the database

                generateSampleTransactions(userBean);
                generateSampleBudget(userBean);

                showSuccess(userBean); // success message
            } catch (ValidationException exception) {
                System.out.println("Validation exception: " + exception.getMessage());
                exception.getValidationErrors().forEach(System.out::println);
            }
        });
    }

    private ValidationResult emailValidator(String email, ValueContext valueContext) {
        if (service.userExists(email)) {
            return ValidationResult.error("An account with this email already exists");
        }
        return ValidationResult.ok();
    }

    // Validates that the password is at least 8 characters in length and both values match
    private ValidationResult passwordValidator(String password1, ValueContext valueContext) {


        // password length checks
        if (password1 == null || password1.length() < 8) {
            return ValidationResult.error("Password should be at least 8 characters long");
        }

        if (password1 == null || password1.length() > 128) {
            return ValidationResult.error("Password should be less than 129 characters in length");
        }

        // password complexity check (at least one uppercase letter, one lowercase letter, and one digit)
        if (!password1.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
            return ValidationResult.error("Password should contain at least one uppercase letter, " +
                    "one lowercase letter, and one digit");
        }

        if (!enablePasswordValidation) {
            enablePasswordValidation = true;
            return ValidationResult.ok();
        }

        String password2 = registrationForm.getPasswordConfirmation().getValue();


        if (password1 != null && password1.equals(password2)) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("Passwords do not match");


    }

    // Called when form has been submitted successfully
    private void showSuccess(User userBean) {
        Notification notification =
                Notification.show("User " + userBean.getEmail() + " successfully created. Welcome "
                        + userBean.getFirstName());

        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Redirect the new user to the Login page
        UI.getCurrent().navigate(LoginView.class);
    }

    private void generateSampleBudget(User newUser) {
        Budget sampleBudget = new Budget(
                "Example Budget",
                Date.valueOf(LocalDate.now().minusMonths(3)),
                Date.valueOf(LocalDate.now().plusDays(3)),
                BigDecimal.valueOf(350.00),
                "See how Prospero budgets work with this example. " +
                        "Customize or delete this budget to fit your own financial plans.",
                newUser.getId(),
                LocalDateTime.now());

        service.saveBudget(sampleBudget);
    }

    private void generateSampleTransactions(User newUser) {
        Transaction income = new Transaction(
                Date.valueOf(LocalDate.now().minusMonths(2).minusDays(1)),
                BigDecimal.valueOf(1000.00),
                "Sample Income Transaction",
                service.findCategoryByName("Bonus"),
                Type.INCOME,
                newUser.getId());
        service.saveTransaction(income);

        Transaction expense1 = new Transaction(
                Date.valueOf(LocalDate.now().minusMonths(2).minusDays(2)),
                BigDecimal.valueOf(95.00),
                "Sample Expense Transaction",
                service.findCategoryByName("Food"),
                Type.EXPENSE,
                newUser.getId());
        service.saveTransaction(expense1);

        Transaction expense2 = new Transaction(
                Date.valueOf(LocalDate.now().minusMonths(1).minusDays(3)),
                BigDecimal.valueOf(75.00),
                "Sample Expense Transaction",
                service.findCategoryByName("Utilities"),
                Type.EXPENSE,
                newUser.getId());
        service.saveTransaction(expense2);

        Transaction expense3 = new Transaction(
                Date.valueOf(LocalDate.now().minusDays(4)),
                BigDecimal.valueOf(30.00),
                "Sample Expense Transaction",
                service.findCategoryByName("Entertainment"),
                Type.EXPENSE,
                newUser.getId());
        service.saveTransaction(expense3);
    }
}
