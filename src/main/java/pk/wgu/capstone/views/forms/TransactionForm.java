package pk.wgu.capstone.views.forms;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
    Binder<Transaction> binder = new BeanValidationBinder<>(Transaction.class);

    // converters
    BigDecimalToDoubleConverter amountConverter = new BigDecimalToDoubleConverter();
    SqlDateToLocalDateConverter dateConverter = new SqlDateToLocalDateConverter();

    // form fields
    DatePicker datePick = new DatePicker("Date");
    NumberField amount = new NumberField("Amount");
    Div dollarPrefix = new Div();
    ComboBox<Type> typeSelect = new ComboBox<>("Type");
    ComboBox<Category> categorySelect = new ComboBox<>("Category");
    TextField description = new TextField("Description");

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

        addClassName("transaction-form"); // for styling
        dollarPrefix.setText("$");
        amount.setPrefixComponent(dollarPrefix);

        binder.forField(datePick)
                .withConverter(dateConverter)
                .withValidator(Objects::nonNull, "Date is required")
                .bind(Transaction::getDate, Transaction::setDate);

        binder.forField(amount)
                .withConverter(amountConverter)
                .withValidator(Objects::nonNull, "Amount is required")
                .withValidator(amount -> amount.compareTo(BigDecimal.valueOf(0.009)) > 0, "Amount must be at least $0.01")
                .bind(Transaction::getAmount, Transaction::setAmount);

        binder.forField(categorySelect)
                .withValidator(Objects::nonNull, "Category is required")
                .bind(Transaction::getCategory, Transaction::setCategory);

        binder.forField(typeSelect)
                .withValidator(Objects::nonNull, "Type is required")
                .bind(Transaction::getType, Transaction::setType);


        binder.bindInstanceFields(this); // bind fields to the data model


        description.setPlaceholder("Enter a description");
        // set items and label generators for category and type fields
        categorySelect.setItems(categories);
        categorySelect.setAllowCustomValue(true);
        categorySelect.setItemLabelGenerator(Category::getName);
        categorySelect.setPlaceholder("Select or create a category");
        typeSelect.setItems(types);
        typeSelect.setItemLabelGenerator(Type::name);
        typeSelect.setPlaceholder("Select a transaction type");


        // Only allow category selection after transaction type has been selected
        categorySelect.setEnabled(false);
        typeSelect.addValueChangeListener(e -> {
            Type selectedType = e.getValue();
            System.out.println("Selected type: " + selectedType.toString());
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
        });


        add( // add form fields and button layout to the layout
                datePick,
                amount,
                typeSelect,
                categorySelect,
                description,
                createButtonLayout()
        );


    }


    public void setTransaction(Transaction transaction) {
        binder.setBean(transaction);
    }

    public Transaction getTransaction() {
        return binder.getBean();
    }

    private Component createButtonLayout() {
        // set theme variants for buttons
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // add click listeners to buttons
        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, binder.getBean())));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        // add keyboard shortcuts to buttons
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        return new HorizontalLayout(save, delete, cancel);
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            Transaction transaction = binder.getBean();
            transaction.setUserId(securityService.getCurrentUserId(service));
            fireEvent(new SaveEvent(this, transaction));
        }
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