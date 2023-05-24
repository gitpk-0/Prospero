package pk.wgu.capstone.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.HttpServletRequest;

@Route("login")
@PageTitle("Login | Prospero")
public class LoginView extends VerticalLayout implements BeforeEnterListener {

    LoginI18n i18n = LoginI18n.createDefault();
    LoginForm loginForm = new LoginForm();

    public LoginView() {
        LoginI18n.Form i18nForm = i18n.getForm();
        i18n.setForm(i18nForm);

        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("");
        i18nErrorMessage.setMessage("Invalid credentials");
        // i18nErrorMessage.setMessage("Check that you have entered the correct username and password and try again.");
        i18n.setErrorMessage(i18nErrorMessage);
        i18n.setAdditionalInformation("If you are experiencing issues " +
                "logging into your account, please contact prospero.support@pm.me");

        loginForm.setI18n(i18n);
        loginForm.getStyle().set("text-align", "center");



        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm.setAction("login");

        Image logo = new Image("icons/icon.png", "Icon");
        logo.addClassName("logo-image-login");

        HttpServletRequest request = (HttpServletRequest) VaadinService.getCurrentRequest();
        String url = request.getRequestURI();

        if (url.equals("/")) {
            loginForm.setError(true);
        }

        System.out.println("Servlet path: " + request.getServletPath());
        System.out.println("Query string: " + request.getQueryString());
        System.out.println("Path info: " + request.getPathInfo());
        System.out.println("Here that is: " + request.getRequestURL().toString());
        System.out.println("Here it is: " + url);


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
        System.out.println("this was called 11111111");
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("err")) {
            Notification error = new Notification("Invalid credentials");
            error.setOpened(true);
            loginForm.setError(true);
        }
    }
}

