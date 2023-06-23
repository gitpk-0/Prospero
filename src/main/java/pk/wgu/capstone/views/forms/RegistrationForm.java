package pk.wgu.capstone.views.forms;

import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import pk.wgu.capstone.views.LoginView;

import java.util.stream.Stream;

@CssImport(value = "./themes/prospero/views/registration-form.css", themeFor = "")
public class RegistrationForm extends FormLayout implements RouterLayout {

    private Image logo;
    private H1 appname;
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
        logo = new Image("icons/icon.png", "Icon");
        logo.addClassName("logo-image-login");
        appname = new H1("Sign up");
        appname.addClassName("app-name-reg");
        title = new H3("Create a new Prospero account");
        title.addClassName("title");

        VerticalLayout formHeader = new VerticalLayout(logo, appname, title);
        formHeader.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        formHeader.setWidthFull();

        firstName = new TextField("First name");
        lastName = new TextField("Last name");
        email = new EmailField("Email");

        allowMarketing = new Checkbox("Subscribe to marketing emails?");
        allowMarketing.getStyle().set("margin-top", "12px").set("margin-bottom", "12px");

        password = new PasswordField("Password");
        passwordConfirmation = new PasswordField("Confirm password");

        errorMessage = new Span();

        submit = new Button("Sign up");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Div toLogin = new Div();
        toLogin.setClassName("to-login");
        toLogin.setText("Already have an account? ");

        Button toLoginBtn = new Button("Login");
        toLoginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        RouterLink toLoginView = new RouterLink(LoginView.class);
        toLoginView.add(toLoginBtn);
        toLogin.add(toLoginView);

        setRequiredIndicatorVisible(firstName, lastName, email, password, passwordConfirmation);

        add(formHeader, firstName, lastName, email, password, passwordConfirmation,
                allowMarketing, errorMessage, submit, toLogin);

        setMaxWidth("500px");

        setResponsiveSteps(
                new ResponsiveStep("0", 1, ResponsiveStep.LabelsPosition.TOP),
                new ResponsiveStep("490px", 2, ResponsiveStep.LabelsPosition.TOP)
        );

        // components below will use the full width of the form
        setColspan(formHeader, 2);
        setColspan(email, 2);
        setColspan(allowMarketing, 2);
        setColspan(errorMessage, 2);
        setColspan(submit, 2);
        setColspan(toLogin, 2);
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

    public void setEmail(String email) {
        this.email.setValue(email);
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