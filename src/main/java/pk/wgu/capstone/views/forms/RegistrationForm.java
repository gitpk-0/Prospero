package pk.wgu.capstone.views.forms;

import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.stream.Stream;

public class RegistrationForm extends FormLayout {

    private H3 title;
    private TextField firstName;
    private TextField lastName;
    private EmailField email;
    private PasswordField password;
    private PasswordField passwordConfirmation;
    private Checkbox allowMarketing;
    private Span errorMessage;
    private Button submit;

    public RegistrationForm() {
        title = new H3("Registration form");
        firstName = new TextField("First name");
        lastName = new TextField("Last name");
        email = new EmailField("Email");
        allowMarketing = new Checkbox("Subscribe to marketing emails?");
        allowMarketing.getStyle().set("margin-top", "12px");
        password = new PasswordField("Password");
        passwordConfirmation = new PasswordField("Confirm password");

        setRequiredIndicatorVisible(firstName, lastName, email, password, passwordConfirmation);

        setMaxWidth("520px");

        setResponsiveSteps(
                new ResponsiveStep("0", 1, ResponsiveStep.LabelsPosition.TOP),
                new ResponsiveStep("500px", 2, ResponsiveStep.LabelsPosition.TOP)
        );

        // components below will use the full width of the form
        setColspan(title, 2);
        setColspan(email, 2);
        setColspan(errorMessage, 2);
        setColspan(submit, 2);

    }

    public PasswordField getPassword() {
        return password;
    }

    public PasswordField getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public Span getErrorMessage() {
        return errorMessage;
    }

    public Button getSubmit() {
        return submit;
    }

    private void setRequiredIndicatorVisible(HasValueAndElement<?, ?>... components) {
        Stream.of(components)
                .forEach(component -> component.setRequiredIndicatorVisible(true));
    }
}
