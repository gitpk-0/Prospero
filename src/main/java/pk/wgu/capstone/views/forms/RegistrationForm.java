package pk.wgu.capstone.views.forms;

import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.stream.Stream;

@CssImport(value = "./styles/registration-form.css", themeFor = "")
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
        title.setClassName("form-field");
        firstName = new TextField("First name");
        firstName.setClassName("form-field");
        lastName = new TextField("Last name");
        lastName.setClassName("form-field");
        email = new EmailField("Email");
        email.setClassName("form-field");

        allowMarketing = new Checkbox("Subscribe to marketing emails?");
        allowMarketing.getStyle().set("margin-top", "12px");

        password = new PasswordField("Password");
        passwordConfirmation = new PasswordField("Confirm password");

        errorMessage = new Span();

        submit = new Button("Start your journey!");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        setRequiredIndicatorVisible(firstName, lastName, email, password, passwordConfirmation);

        add(title, firstName, lastName, email, password, passwordConfirmation,
                allowMarketing, errorMessage, submit);

        setMaxWidth("500px");

        setResponsiveSteps(
                new ResponsiveStep("0", 1, ResponsiveStep.LabelsPosition.TOP),
                new ResponsiveStep("490px", 2, ResponsiveStep.LabelsPosition.TOP)
        );


        // components below will use the full width of the form
        setColspan(title, 2);
        setColspan(email, 2);
        setColspan(allowMarketing, 2);
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

    public EmailField getEmail() {
        return email;
    }

    public Button getSubmit() {
        return submit;
    }

    public Checkbox getAllowMarketing() {
        return allowMarketing;
    }

    private void setRequiredIndicatorVisible(HasValueAndElement<?, ?>... components) {
        Stream.of(components)
                .forEach(component -> component.setRequiredIndicatorVisible(true));
    }
}