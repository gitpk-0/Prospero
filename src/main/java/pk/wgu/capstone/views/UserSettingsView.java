package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.entity.User;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

import java.util.Objects;


@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings | Prospero")
@PermitAll // all logged-in users can access this page
public class UserSettingsView extends Div {

    private SecurityService securityService;
    private PfmService service;

    // user info form fields
    private TextField firstName;
    private TextField lastName;
    private EmailField email;
    private Checkbox allowMarketing;

    private Binder<User> userBinder = new Binder<>(User.class);

    private Button updateBtn = new Button("Save Changes");
    private Button cancelBtn = new Button("Cancel");

    private Accordion accordion;

    boolean isDarkTheme = true;

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

        userBinder.forField(firstName)
                .withValidator(Objects::nonNull, "First name is required")
                .bind(User::getFirstName, User::setFirstName);

        userBinder.forField(lastName)
                .withValidator(Objects::nonNull, "Last name is required")
                .bind(User::getLastName, User::setLastName);

        userBinder.forField(email)
                .withValidator(this::emailValidator)
                .bind(User::getEmail, User::setEmail);

        userBinder.forField(allowMarketing)
                .bind(User::isAllowsMarketingEmails, User::setAllowsMarketingEmails);

        userBinder.bindInstanceFields(this);

        cancelBtn.addClickListener(e -> accordion.close());
        updateBtn.addClickListener(e -> {
            // System.out.println(getUser());
            if (validateAndSaveUserInfo()) {
                Notification.show("Information Updated.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                accordion.close();
            }
            // service.updateUserInfo(userBinder.getBean());
        });
    }

    private void setUser(User user) {
        userBinder.setBean(user);
    }

    private User getUser() {
        System.out.println("get user called");
        System.out.println("Is valid userbean: " + userBinder.isValid());
        return userBinder.getBean();
    }

    private ValidationResult emailValidator(String email, ValueContext valueContext) {
        User currentUser = service.findUserById(userId);
        // if email exists and is not the current user
        if (service.userExists(email) && !currentUser.getEmail().equalsIgnoreCase(email)) {
            return ValidationResult.error("An account with this email already exists");
        }
        return ValidationResult.ok();
    }

    private boolean validateAndSaveUserInfo() {
        try {
            if (userBinder.isValid()) {
                User updatedUser = new User();
                userBinder.writeBean(updatedUser); // run validation

                User currentUser = service.findUserById(userId);
                currentUser.setFirstName(updatedUser.getFirstName());
                currentUser.setLastName(updatedUser.getLastName());
                currentUser.setEmail(updatedUser.getEmail());
                currentUser.setAllowsMarketingEmails(updatedUser.isAllowsMarketingEmails());

                service.updateExistingUserInfo(currentUser);
                return true;
            }
        } catch (ValidationException exception) {
            System.out.println("Validation exception: " + exception.getMessage());
            exception.getValidationErrors().forEach(System.out::println);
            return false;
        }
        return false;
    }

    private void initializeFormFields() {
        User currentUser = service.findUserById(userId);
        setUser(currentUser);
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
                "Advanced Settings", createAdvSettingsAccordion());
        advSettings.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        advSettings.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

        AccordionPanel contact = accordion.add(
                "Contact", createContactLayout());
        contact.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        contact.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

        return accordion;
    }

    private Component createContactLayout() {
        return new VerticalLayout(new H4("For any questions, issues, or other inquiries, " +
                "kindly direct your email to prospero.support@pm.me"));
    }

    private Component createChangeAppThemeButton() {
        // light/dark mode toggle button
        Button changeThemeBtn = new Button("Change App Theme", VaadinIcon.MOON.create());
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

        AccordionPanel transactions = new AccordionPanel("Transactions");
        transactions.setContent(createTransactionsSettingsLayout());
        transactions.addThemeVariants(DetailsVariant.FILLED);
        advSettingsAccordion.add(transactions);

        AccordionPanel categories = new AccordionPanel("Categories");
        categories.setContent(new H5("Coming soon!"));
        categories.addThemeVariants(DetailsVariant.FILLED);
        advSettingsAccordion.add(categories);

        AccordionPanel budgets = new AccordionPanel("Budgets");
        budgets.setContent(new H5("Coming soon!"));
        budgets.addThemeVariants(DetailsVariant.FILLED);
        advSettingsAccordion.add(budgets);

        categories.setEnabled(false);
        budgets.setEnabled(false);

        advSettingsAccordion.close();

        return advSettingsAccordion;
    }

    private Component createTransactionsSettingsLayout() {
        Button deleteAllBtn = new Button("Delete All Transactions", VaadinIcon.TRASH.create());
        deleteAllBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteAllBtn.getStyle().set("--lumo-primary-color", "red");

        Integer transactionCount = service.getTransactionCount(userId);

        deleteAllBtn.addClickListener(e -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Warning");
            confirmDialog.setText("By proceeding, you are about to permanently delete all " + transactionCount +
                    " of your transactions. Are you absolutely certain you want to continue?");
            confirmDialog.setConfirmText("Delete " + transactionCount + " Transactions");
            confirmDialog.setConfirmButtonTheme("error primary");
            confirmDialog.setCancelable(true);
            confirmDialog.open();

            confirmDialog.addConfirmListener(confirmE -> {
                service.deleteAllTransactionsForUser(userId);
            });
        });

        return deleteAllBtn;
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
        buttonLayout.addClassNames(LumoUtility.Margin.Vertical.SMALL);
        return buttonLayout;
    }

    private void setTheme(boolean dark) {
        var js = "document.documentElement.setAttribute('theme', $0)";
        getElement().executeJs(js, dark ? Lumo.DARK : Lumo.LIGHT);
    }
}
