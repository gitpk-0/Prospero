package pk.wgu.capstone.views.forms;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import pk.wgu.capstone.data.converter.BigDecimalToDoubleConverter;
import pk.wgu.capstone.data.converter.SqlDateToLocalDateConverter;
import pk.wgu.capstone.data.entity.Budget;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;
import pk.wgu.capstone.views.budget.BudgetListView;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;

public class BudgetForm extends FormLayout {

    private SecurityService securityService;
    private PfmService service;
    private BudgetListView budgetListView;

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

        dollarPrefix.setText("$");
        spendingGoal.setPrefixComponent(dollarPrefix);
        endDatePick.setHelperText("Must be after start date");

        budgetBinder.forField(budgetName)
                .withValidator(Objects::nonNull, "Budget name is required")
                .bind(Budget::getName, Budget::setName);

        budgetBinder.forField(startDatePick)
                .withConverter(dateConverter)
                .withValidator(Objects::nonNull, "State date is required")
                .withValidator(start -> {
                    LocalDate startDate = startDatePick.getValue();
                    LocalDate endDate = endDatePick.getValue();
                    return startDate == null || endDate == null || startDate.isBefore(endDate);
                }, "Start date must be before the end date")
                .bind(Budget::getStart, Budget::setStart);

        budgetBinder.forField(endDatePick)
                .withConverter(dateConverter)
                .withValidator(Objects::nonNull, "End date is required")
                .withValidator(end -> {
                    LocalDate startDate = startDatePick.getValue();
                    LocalDate endDate = endDatePick.getValue();
                    return startDate == null || endDate == null || endDate.isAfter(startDate);
                }, "End date must be after the start date")
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

        // setDatePickerValues();
        startDatePick.addValueChangeListener(e -> {
            if (startDatePick.getValue() != null) {
                endDatePick.setMin(startDatePick.getValue().plusDays(1));
            }
        });

        description.setPlaceholder("Enter a description");

        add(
                budgetName,
                startDatePick,
                endDatePick,
                description,
                spendingGoal
                // createButtonLayout()
        );
    }

    // Budget
    public void setBudget(Budget budget) {
        budgetBinder.setBean(budget);
    }

    public Budget getBudget() {
        return budgetBinder.getBean();
    }

    public void setDatePickerValues() {
        startDatePick.setValue(LocalDate.now());
        endDatePick.setValue(LocalDate.now().plusWeeks(1));
        endDatePick.setMin(startDatePick.getValue().plusDays(1));
    }

    public Component editBudgetButtonLayout(Dialog dialog) {
        // set theme variants for buttons
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // add click listeners to buttons
        save.addClickListener(event -> {
            if (validateAndSave()) {
                dialog.close();
            }
        });

        delete.addClickListener(event -> {
            dialog.close();
            confirmDeleteDialog(dialog);
        });

        cancel.addClickListener(event -> {
            dialog.close();
        });

        // add keyboard shortcuts to buttons
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        return new HorizontalLayout(save, delete, cancel);
    }

    public Component createNewBudgetButtonLayout(Dialog dialog) {
        // set theme variants for buttons
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // add click listeners to buttons
        save.addClickListener(event -> {
            if (validateAndSave()) {
                dialog.close();
            }
        });

        cancel.addClickListener(event -> {
            dialog.close();
        });

        // add keyboard shortcuts to buttons
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        return new HorizontalLayout(save, cancel);
    }

    private void confirmDeleteDialog(Dialog dialog) {
        // current user's first name
        String firstName = service.findUserById(securityService.getCurrentUserId(service)).getFirstName();

        ConfirmDialog confirmDelete = new ConfirmDialog();
        confirmDelete.setHeader("Delete Budget");
        confirmDelete.setText(firstName + ", are you sure you want to permanently delete this budget?");
        confirmDelete.setCancelable(true);
        confirmDelete.setConfirmText("Delete");
        confirmDelete.setConfirmButtonTheme("error primary");
        confirmDelete.addConfirmListener(e -> {
            deleteBudget(new DeleteEvent(this, budgetBinder.getBean()));
            dialog.close();
        });
        confirmDelete.open();
    }

    public boolean validateAndSave() {
        if (budgetBinder.isValid()) {
            Budget budget = budgetBinder.getBean();
            budget.setUserId(securityService.getCurrentUserId(service));
            saveBudget(new BudgetForm.SaveEvent(this, budget));
            fireEvent(new BudgetForm.CloseEvent(this));
            budgetListView.closeDialog();
            return true;
        } else {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Error creating budget");
            confirmDialog.setText("Please make sure all fields are filled out properly.");
            confirmDialog.setConfirmText("OK");
            confirmDialog.open();
        }
        return false;
    }

    private void saveBudget(SaveEvent saveEvent) {
        Budget budget = saveEvent.getBudget();
        if (budget.getDateCreated() == null) {
            budget.setDateCreated(Date.valueOf(LocalDate.now()));
        }
        service.saveBudget(budget);
        budgetListView.updateBudgetList();
    }

    private void deleteBudget(BudgetForm.DeleteEvent deleteEvent) {
        service.deleteBudget(deleteEvent.getBudget());
        budgetListView.updateBudgetList();
    }

    public void setBudgetListView(BudgetListView budgetListView) {
        this.budgetListView = budgetListView;
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

}
