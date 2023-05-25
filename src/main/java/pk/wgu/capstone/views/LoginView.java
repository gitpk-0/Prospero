package pk.wgu.capstone.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | Prospero")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    LoginI18n i18n = LoginI18n.createDefault();
    LoginForm loginForm = new LoginForm();

    public LoginView() {
        LoginI18n.Form i18nForm = i18n.getForm();
        i18n.setForm(i18nForm);

        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("Invalid credentials");
        i18nErrorMessage.setMessage("Check that you have entered the correct username and password and try again.");
        i18n.setErrorMessage(i18nErrorMessage);
        i18n.setAdditionalInformation("If you are experiencing issues " +
                "logging into your account, please contact prospero.support@pm.me");

        loginForm.setI18n(i18n);
        // loginForm.getStyle().set("text-align", "center");



        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm.setAction("login");

        Image logo = new Image("icons/icon.png", "Icon");
        logo.addClassName("logo-image-login");

        add(
                logo,
                new H1("Prospero"),
                new H3("Your Path to Prosperity"),
                loginForm,
                new RouterLink("Create an account", RegistrationView.class)
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        System.out.println("BEFORE-EVENT-METHOD-CALLED!");

        // inform the user about an authentication error
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}

