package pk.wgu.capstone.views.forms;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import pk.wgu.capstone.data.converter.BigDecimalToDoubleConverter;
import pk.wgu.capstone.data.converter.SqlDateToLocalDateConverter;
import pk.wgu.capstone.data.entity.Category;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransactionForm extends FormLayout {

    private SecurityService securityService;
    private PfmService service;

    // data binding
    Binder<Transaction> transactionBinder = new BeanValidationBinder<>(Transaction.class);

    // converters
    BigDecimalToDoubleConverter amountConverter = new BigDecimalToDoubleConverter();
    SqlDateToLocalDateConverter dateConverter = new SqlDateToLocalDateConverter();

    // form fields
    DatePicker datePick = new DatePicker("Date");
    NumberField amount = new NumberField("Amount");
    Div dollarPrefix = new Div();
    Select<Type> typeSelect = new Select<>();
    Select<Category> categorySelect = new Select<>();
    TextField description = new TextField("Description");
    public Button createNewCategoryBtn = new Button("Create new");

    // buttons
    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button cancel = new Button("Cancel");

    public TransactionForm(SecurityService securityService,
                           PfmService service,
                           List<Category> categories,
                           List<Type> types) {
        this.securityService = securityService;
        this.service = service;

        // show numerical keyboard when amount field is focused on mobile devices
        amount.getElement().getNode().runWhenAttached(ui ->
                ui.getPage().executeJs("$0.focusElement().type=$1", amount, "number"));

        typeSelect.setHelperText("Type");
        typeSelect.getElement().getThemeList().add(SelectVariant.LUMO_HELPER_ABOVE_FIELD.getVariantName());

        categorySelect.setHelperText("Category");
        categorySelect.getElement().getThemeList().add(SelectVariant.LUMO_HELPER_ABOVE_FIELD.getVariantName());

        addClassName("transaction-form"); // for styling
        dollarPrefix.setText("$");
        amount.setPrefixComponent(dollarPrefix);
        createNewCategoryBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createNewCategoryBtn.getStyle().set("--lumo-primary-color", "green");

        transactionBinder.forField(datePick)
                .withConverter(dateConverter)
                .withValidator(Objects::nonNull, "Date is required")
                .bind(Transaction::getDate, Transaction::setDate);

        transactionBinder.forField(amount)
                .withConverter(amountConverter)
                .withValidator(Objects::nonNull, "Amount is required")
                .withValidator(amount -> amount.compareTo(BigDecimal.valueOf(0.009)) > 0, "Amount must be at least $0.01")
                .bind(Transaction::getAmount, Transaction::setAmount);

        transactionBinder.forField(typeSelect)
                .withValidator(Objects::nonNull, "Type is required")
                .bind(Transaction::getType, Transaction::setType);

        transactionBinder.forField(categorySelect)
                .withValidator(Objects::nonNull, "Category is required")
                .bind(Transaction::getCategory, Transaction::setCategory);

        transactionBinder.bindInstanceFields(this); // bind fields to the data model

        description.setPlaceholder("Enter a description");
        // set items and label generators for category and type fields
        categorySelect.setItems(categories);
        // categorySelect.setAllowCustomValue(true);
        categorySelect.setItemLabelGenerator(Category::getName);
        categorySelect.setPlaceholder("Select a category");
        typeSelect.setItems(types);
        typeSelect.setItemLabelGenerator(Type::name);
        typeSelect.setPlaceholder("Select a transaction type");

        // Only allow category selection after transaction type has been selected
        categorySelect.setEnabled(false);
        typeSelect.addValueChangeListener(e -> {
            filterCategoriesByType(categories, e);
        });

        add( // add form fields and button layout to the layout
                datePick,
                amount,
                typeSelect,
                categoryFieldsLayout(),
                description,
                createButtonLayout()
        );
    }


    private void filterCategoriesByType(List<Category> categories, AbstractField.ComponentValueChangeEvent<Select<Type>, Type> e) {
        Type selectedType = e.getValue();
        if (selectedType != null) {
            if (selectedType.equals(Type.INCOME)) {
                categorySelect.setItems(categories
                        .stream().filter(c -> c.getType() == Type.INCOME).collect(Collectors.toList()));
                categorySelect.setEnabled(true);
            } else if (selectedType.equals(Type.EXPENSE)) {
                categorySelect.setItems(categories
                        .stream().filter(c -> c.getType() == Type.EXPENSE).collect(Collectors.toList()));
                categorySelect.setEnabled(true);
            } else {
                categorySelect.setEnabled(false);
            }
        }
    }

    // Transaction
    public void setTransaction(Transaction transaction) {
        transactionBinder.setBean(transaction);
    }

    public Transaction getTransaction() {
        return transactionBinder.getBean();
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
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        // add keyboard shortcuts to buttons
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        return new HorizontalLayout(save, delete, cancel);
    }

    private void confirmDeleteDialog() {
        // current user's first name
        String firstName = service.findUserById(securityService.getCurrentUserId(service)).getFirstName();

        ConfirmDialog confirmDelete = new ConfirmDialog();
        confirmDelete.setHeader("Delete Transaction");
        confirmDelete.setText(firstName + ", are you sure you want to permanently delete this transaction?");
        confirmDelete.setCancelable(true);
        confirmDelete.setConfirmText("Delete");
        confirmDelete.setConfirmButtonTheme("error primary");
        confirmDelete.addConfirmListener(e -> fireEvent(new DeleteEvent(this, transactionBinder.getBean())));
        confirmDelete.open();
    }

    private void validateAndSave() {
        if (transactionBinder.isValid()) {
            Transaction transaction = transactionBinder.getBean();
            transaction.setUserId(securityService.getCurrentUserId(service));
            fireEvent(new SaveEvent(this, transaction));
        }
    }

    private Component categoryFieldsLayout() {
        HorizontalLayout layout = new HorizontalLayout(categorySelect, createNewCategoryBtn);
        layout.setAlignItems(FlexComponent.Alignment.END);
        return layout;
    }

    // Events
    public static abstract class TransactionFormEvent extends ComponentEvent<TransactionForm> {

        private Transaction transaction;

        protected TransactionFormEvent(TransactionForm source, Transaction transaction) {
            super(source, false);
            this.transaction = transaction;
        }

        public Transaction getTransaction() {
            return transaction;
        }
    }


    public static class SaveEvent extends TransactionFormEvent {
        SaveEvent(TransactionForm source, Transaction transaction) {
            super(source, transaction);
        }
    }


    public static class DeleteEvent extends TransactionFormEvent {
        DeleteEvent(TransactionForm source, Transaction transaction) {
            super(source, transaction);
        }
    }


    public static class CloseEvent extends TransactionFormEvent {
        CloseEvent(TransactionForm source) {
            super(source, null);
        }

    }


    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }


    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        return addListener(DeleteEvent.class, listener);
    }


    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }
}