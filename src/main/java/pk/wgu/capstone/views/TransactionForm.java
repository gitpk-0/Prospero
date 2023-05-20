package pk.wgu.capstone.views;

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

import java.util.List;
import java.util.Objects;

public class TransactionForm extends FormLayout {

    // data binding
    Binder<Transaction> binder = new BeanValidationBinder<>(Transaction.class);

    // converters
    BigDecimalToDoubleConverter amountConverter = new BigDecimalToDoubleConverter();
    SqlDateToLocalDateConverter dateConverter = new SqlDateToLocalDateConverter();

    // form fields
    DatePicker date = new DatePicker("Date");
    NumberField amount = new NumberField("Amount");
    Div dollarPrefix = new Div();
    TextField description = new TextField("Description");
    ComboBox<Category> category = new ComboBox<>("Category");
    ComboBox<Type> type = new ComboBox<>("Type");

    // buttons
    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button cancel = new Button("Cancel");

    /**
     * Creates a new TransactionForm with the provided categories and types.
     *
     * @param categories The list of available categories.
     * @param types      The list of available transaction types.
     */
    public TransactionForm(List<Category> categories, List<Type> types) {
        addClassName("transaction-form"); // for styling


        dollarPrefix.setText("$");
        amount.setPrefixComponent(dollarPrefix);

        binder.forField(date)
                .withConverter(dateConverter)
                .withValidator(Objects::nonNull, "Date is required")
                .bind(Transaction::getDate, Transaction::setDate);

        binder.forField(amount)
                .withConverter(amountConverter)
                .withValidator(Objects::nonNull, "Amount is required")
                .bind(Transaction::getAmount, Transaction::setAmount);

        binder.forField(category)
                .withValidator(Objects::nonNull, "Category is required")
                .bind(Transaction::getCategory, Transaction::setCategory);

        binder.forField(type)
                .withValidator(Objects::nonNull, "Type is required")
                .withValidator(value -> !(category.getValue().getName().equals("Income") && value == Type.EXPENSE),
                        "Type cannot be EXPENSE if category is Income")
                .bind(Transaction::getType, Transaction::setType);


        binder.bindInstanceFields(this); // bind fields to the data model

        // set items and label generators for category and type fields
        category.setItems(categories);
        category.setItemLabelGenerator(Category::getName);
        type.setItems(types);
        type.setItemLabelGenerator(Type::name);


        category.addValueChangeListener(e -> {
            Category c = e.getValue();
            if (c != null && c.getName().equals("Income")) {
                type.setValue(Type.INCOME);
            }
        });

        type.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() == Type.INCOME) {
                category.setValue(categories.get(0));
            }
        });


        add( // add form fields and button layout to the layout
                date,
                amount,
                description,
                category,
                type,
                createButtonLayout()
        );


    }

    /**
     * Sets the provided Transaction object as the bean for the binder.
     *
     * @param transaction The Transaction object to be set.
     */
    public void setTransaction(Transaction transaction) {
        binder.setBean(transaction);
    }

    /**
     * Creates the button layout for the form.
     *
     * @return The horizontal layout containing the buttons.
     */
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

    /**
     * Validates and saves the form data when the save button is clicked.
     */
    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }

    // Events

    /**
     * Represents an abstract event in the TransactionForm.
     */
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

    /**
     * Represents a Save event in the TransactionForm.
     */
    public static class SaveEvent extends TransactionFormEvent {
        SaveEvent(TransactionForm source, Transaction transaction) {
            super(source, transaction);
        }
    }

    /**
     * Represents a Delete event in the TransactionForm.
     */
    public static class DeleteEvent extends TransactionFormEvent {
        DeleteEvent(TransactionForm source, Transaction transaction) {
            super(source, transaction);
        }
    }

    /**
     * Represents a Close event in the TransactionForm.
     */
    public static class CloseEvent extends TransactionFormEvent {
        CloseEvent(TransactionForm source) {
            super(source, null);
        }

    }

    /**
     * Adds a Save event listener to the TransactionForm.
     *
     * @param listener The listener to be added.
     * @return The registration object for the listener
     */
    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    /**
     * Adds a Delete event listener to the TransactionForm.
     *
     * @param listener The listener to be added.
     * @return The registration object for the listener
     */
    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        return addListener(DeleteEvent.class, listener);
    }

    /**
     * Adds a Close event listener to the TransactionForm.
     *
     * @param listener The listener to be added.
     * @return The registration object for the listener
     */
    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }
}
