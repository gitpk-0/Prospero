package pk.wgu.capstone.views.forms;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import pk.wgu.capstone.data.converter.BigDecimalToDoubleConverter;
import pk.wgu.capstone.data.converter.SqlDateToLocalDateConverter;
import pk.wgu.capstone.data.entity.Budget;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

import java.math.BigDecimal;
import java.util.Objects;

public class BudgetForm extends FormLayout {

    private SecurityService securityService;
    private PfmService service;

    Binder<Budget> budgetBinder = new BeanValidationBinder<>(Budget.class);

    // converters
    BigDecimalToDoubleConverter amountConverter = new BigDecimalToDoubleConverter();
    SqlDateToLocalDateConverter dateConverter = new SqlDateToLocalDateConverter();

    // form fields
    TextField budgetName = new TextField("Budget Name");
    DatePicker startDatePick = new DatePicker("Start Date");
    DatePicker endDatePick = new DatePicker("End Date");
    TextField description = new TextField("Description");
    NumberField spendingGoal = new NumberField("Spending Goal");
    Div dollarPrefix = new Div();


    // buttons
    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button cancel = new Button("Cancel");

    public BudgetForm(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        // addClassName("Budget-form");
        dollarPrefix.setText("$");
        spendingGoal.setPrefixComponent(dollarPrefix);

        budgetBinder.forField(budgetName)
                .withValidator(Objects::nonNull, "Budget name is required")
                .bind(Budget::getName, Budget::setName);

        budgetBinder.forField(startDatePick)
                .withConverter(dateConverter)
                .withValidator(Objects::nonNull, "State date is required")
                .bind(Budget::getStart, Budget::setStart);

        budgetBinder.forField(endDatePick)
                .withConverter(dateConverter)
                .withValidator(Objects::nonNull, "End date is required")
                .bind(Budget::getEnd, Budget::setEnd);


        budgetBinder.forField(description)
                .withValidator(Objects::nonNull, "Description is required")
                .bind(Budget::getDescription, Budget::setDescription);

        budgetBinder.forField(spendingGoal)
                .withConverter(amountConverter)
                .withValidator(Objects::nonNull, "Spending goal is required")
                .withValidator(amount -> amount.compareTo(BigDecimal.valueOf(0.009)) > 0, "Amount must be at least $0.01")
                .bind(Budget::getSpendingGoal, Budget::setSpendingGoal);

        budgetBinder.bindInstanceFields(this);

        description.setPlaceholder("Enter a description");

        add(
                budgetName,
                startDatePick,
                endDatePick,
                description,
                spendingGoal,
                createButtonLayout()
        );
    }

    // Budget
    public void setBudget(Budget budget) {
        budgetBinder.setBean(budget);
    }

    public Budget getBudget() {
        return budgetBinder.getBean();
    }

    private Component createButtonLayout() {
        // set theme variants for buttons
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // add click listeners to buttons
        save.addClickListener(event -> validateAndSave());
        // delete.addClickListener(event -> fireEvent(new DeleteEvent(this, binder.getBean())));
        delete.addClickListener(event -> confirmDeleteDialog());
        cancel.addClickListener(event -> fireEvent(new BudgetForm.CloseEvent(this)));

        // add keyboard shortcuts to buttons
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        return new HorizontalLayout(save, delete, cancel);
    }

    private void confirmDeleteDialog() {
        // current user's first name
        String firstName = service.findUserById(securityService.getCurrentUserId(service)).getFirstName();

        ConfirmDialog confirmDelete = new ConfirmDialog();
        confirmDelete.setHeader("Delete Budget");
        confirmDelete.setText(firstName + ", are you sure you want to permanently delete this budget?");
        confirmDelete.setCancelable(true);
        confirmDelete.setConfirmText("Delete");
        confirmDelete.setConfirmButtonTheme("error primary");
        confirmDelete.addConfirmListener(e -> fireEvent(new BudgetForm.DeleteEvent(this, budgetBinder.getBean())));
        confirmDelete.open();
    }

    private void validateAndSave() {
        if (budgetBinder.isValid()) {
            Budget budget = budgetBinder.getBean();
            budget.setUserId(securityService.getCurrentUserId(service));
            fireEvent(new BudgetForm.SaveEvent(this, budget));
        }
    }

    // Events
    public static abstract class BudgetFormEvent extends ComponentEvent<BudgetForm> {

        private Budget budget;

        protected BudgetFormEvent(BudgetForm source, Budget Budget) {
            super(source, false);
            this.budget = Budget;
        }

        public Budget getBudget() {
            return budget;
        }
    }


    public static class SaveEvent extends BudgetForm.BudgetFormEvent {
        SaveEvent(BudgetForm source, Budget Budget) {
            super(source, Budget);
        }
    }


    public static class DeleteEvent extends BudgetForm.BudgetFormEvent {
        DeleteEvent(BudgetForm source, Budget Budget) {
            super(source, Budget);
        }
    }


    public static class CloseEvent extends BudgetForm.BudgetFormEvent {
        CloseEvent(BudgetForm source) {
            super(source, null);
        }

    }


    public Registration addSaveListener(ComponentEventListener<BudgetForm.SaveEvent> listener) {
        return addListener(BudgetForm.SaveEvent.class, listener);
    }


    public Registration addDeleteListener(ComponentEventListener<BudgetForm.DeleteEvent> listener) {
        return addListener(BudgetForm.DeleteEvent.class, listener);
    }


    public Registration addCloseListener(ComponentEventListener<BudgetForm.CloseEvent> listener) {
        return addListener(BudgetForm.CloseEvent.class, listener);
    }
}
