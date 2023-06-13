package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.entity.User;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;


@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings | Prospero")
@PermitAll // all logged-in users can access this page
public class UserSettingsView extends Div {

    private SecurityService securityService;
    private PfmService service;

    private TextField firstName;
    private TextField lastName;
    private EmailField email;
    private Checkbox allowMarketing;

    private Button updateBtn = new Button("Save Changes");
    private Button cancelBtn = new Button("Cancel");

    private Accordion accordion;

    boolean isDarkTheme = true;

    private Binder<User> userBinder = new Binder<>(User.class);

    private Long userId;

    public UserSettingsView(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        userId = securityService.getCurrentUserId(service);

        addClassName("settings-view");
        addClassNames(LumoUtility.MaxWidth.SCREEN_LARGE,
                LumoUtility.Margin.Horizontal.AUTO,
                LumoUtility.Padding.LARGE);

        initializeFormFields();

        add(createTitle());
        add(createMainAccordion());
        add(createButtonLayout());

        accordion.close();

        userBinder.bindInstanceFields(this);

        cancelBtn.addClickListener(e -> accordion.close());
        updateBtn.addClickListener(e -> {
            // service.updateUserInfo(userBinder.getBean());
            Notification.show("Information Updated.");
            accordion.close();
        });
    }

    private void initializeFormFields() {
        User currentUser = service.findUserById(userId);
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        email = new EmailField("Email");
        allowMarketing = new Checkbox("Subscribe to marketing emails?");
        allowMarketing.getStyle().set("margin-top", "12px").set("margin-bottom", "12px");

        firstName.setValue(currentUser.getFirstName());
        lastName.setValue(currentUser.getLastName());
        email.setValue(currentUser.getEmail());
        allowMarketing.setValue(currentUser.isAllowsMarketingEmails());
    }

    private Component createTitle() {
        return new H3("Settings");
    }

    private Component createMainAccordion() {
        accordion = new Accordion();
        accordion.addClassName("main-accordion");

        AccordionPanel userInfo = accordion.add("User Information", createUserInfoFormLayout());
        userInfo.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        userInfo.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

        AccordionPanel appTheme = accordion.add("App Theme", createChangeAppThemeButton());
        appTheme.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        appTheme.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

        AccordionPanel advSettings = accordion.add(
                "Advanced Settings", new VerticalLayout(createAdvSettingsAccordion()));
        advSettings.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        appTheme.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

        return accordion;
    }

    private Component createChangeAppThemeButton() {
        // light/dark mode toggle button
        Button changeThemeBtn = new Button("Change App Theme", VaadinIcon.MOON.create());
        // changeThemeBtn.addClassName("theme-btn");
        changeThemeBtn.getElement().setAttribute("title", "Change current theme");
        changeThemeBtn.addClickListener(e -> {
            if (isDarkTheme) {
                setTheme(false);
                isDarkTheme = false;
            } else {
                setTheme(true);
                isDarkTheme = true;
            }
        });

        return changeThemeBtn;
    }

    private Component createAdvSettingsAccordion() {
        Accordion advSettingsAccordion = new Accordion();

        advSettingsAccordion.add("Categories", new VerticalLayout(new H3("Manage your transaction categories")));
        advSettingsAccordion.add("Budgets", new VerticalLayout(new H3("Manage your budgets")));
        advSettingsAccordion.add("Transactions", new VerticalLayout(new H3("Coming Soon!")));

        return advSettingsAccordion;
    }

    private Component createUserInfoFormLayout() {
        FormLayout userInfoForm = new FormLayout();
        firstName.setErrorMessage("First name cannot be blank");
        lastName.setErrorMessage("Last name cannot be blank");
        email.setErrorMessage("Please enter a valid email address");
        userInfoForm.add(firstName, lastName, email, allowMarketing, createButtonLayout());
        return userInfoForm;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassNames("button-layout");
        updateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(updateBtn);
        buttonLayout.add(cancelBtn);
        buttonLayout.addClassNames(LumoUtility.Margin.Vertical.AUTO);
        return buttonLayout;
    }

    private void setTheme(boolean dark) {
        var js = "document.documentElement.setAttribute('theme', $0)";
        getElement().executeJs(js, dark ? Lumo.DARK : Lumo.LIGHT);
    }
}
