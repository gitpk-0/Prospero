package pk.wgu.capstone.data.binder;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import pk.wgu.capstone.data.entity.User;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.views.LoginView;
import pk.wgu.capstone.views.forms.RegistrationForm;

public class RegistrationFormBinder {

    private RegistrationForm registrationForm;
    private PfmService service;

    private boolean enablePasswordValidation;

    public RegistrationFormBinder(RegistrationForm registrationForm, PfmService service) {
        this.registrationForm = registrationForm;
        this.service = service;
    }

    /**
     * Add the data binding and validation logic to the registration form
     */
    public void addBindingAndValidation() {
        System.out.println("BINDING AND VALIDATION ADDED -- 1");
        BeanValidationBinder<User> binder = new BeanValidationBinder<>(User.class);
        binder.bindInstanceFields(registrationForm);

        // custom validation for password fields
        binder.forField(registrationForm.getPassword())
                .withValidator(this::passwordValidator).bind("password");

        registrationForm.getPasswordConfirmation().addValueChangeListener(e -> {
            // user has changed the second password field, validate and show errors
            enablePasswordValidation = true;
            binder.validate();
        });

        binder.setStatusLabel(registrationForm.getErrorMessage());

        registrationForm.getSubmit().addClickListener(e -> {
            try {
                System.out.println("BINDING AND VALIDATION CLICK LISTENER FIRED");
                User userBean = new User(); // new bean to store user info into

                binder.writeBean(userBean); // run validation and write the values to the bean

                service.addNewUser(userBean);// add the new user to the database

                showSuccess(userBean); // success message
            } catch (ValidationException exception) {
                System.out.println("Validation exception: " + exception.getMessage());
            }
        });

        System.out.println("BINDING AND VALIDATION ADDED -- 2");
    }

    /**
     * Validates that the password is at least 8 characters in length and both values match
     *
     * @param password1    The password entered into the first password field
     * @param valueContext The value context
     * @return The validation result
     */
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

    /**
     * Called when form has been submitted successfully
     *
     * @param userBean The new user
     */
    private void showSuccess(User userBean) {
        Notification notification =
                Notification.show("User " + userBean.getEmail() + " successfully created. Welcome "
                        + userBean.getFirstName());

        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Redirect the new user to the Login page
        UI.getCurrent().navigate(LoginView.class);
    }
}
