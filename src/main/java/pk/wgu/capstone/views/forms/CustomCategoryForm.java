package pk.wgu.capstone.views.forms;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import pk.wgu.capstone.data.entity.Category;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

import java.util.List;
import java.util.Objects;

public class CustomCategoryForm extends FormLayout {

    private SecurityService securityService;
    private PfmService service;

    // data binding
    Binder<Category> binder = new BeanValidationBinder<>(Category.class);

    // form fields
    TextField categoryName = new TextField("Category Name");
    ComboBox<Type> typeSelect = new ComboBox<>("Type");

    // buttons
    Button save = new Button("Save");
    Button cancel = new Button("Cancel");

    public CustomCategoryForm(SecurityService securityService, PfmService service, List<Type> types) {
        this.securityService = securityService;
        this.service = service;
        this.typeSelect = typeSelect;

        addClassName("custom-category-form");

        binder.forField(typeSelect)
                .withValidator(Objects::nonNull, "Type is required")
                .bind(Category::getType, Category::setType);

        binder.forField(categoryName)
                .withValidator(Objects::nonNull, "Category name is required")
                .bind(Category::getName, Category::setName);

        binder.bindInstanceFields(this); // bind fields to the data model

        categoryName.setPlaceholder("Enter a new category name");
        typeSelect.setItems(types);
        typeSelect.setItemLabelGenerator(Type::name);
        typeSelect.setPlaceholder("Select a transaction type");

        add (
                new H4("Create a new transaction category"),
                typeSelect,
                categoryName,
                createButtonLayout()
        );
    }

    private Component createButtonLayout() {
        // set theme variants for buttons
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // add click listeners to buttons
        save.addClickListener(event -> validateAndSave());
        cancel.addClickListener(event -> fireEvent(new CustomCategoryForm.CloseEvent(this)));

        // add keyboard shortcuts to buttons
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        return new HorizontalLayout(save, cancel);
    }

    public void setCategory(Category category) {
        binder.setBean(category);
    }

    public Category getCategory() {
        return binder.getBean();
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            Category category = binder.getBean();
            // category.setUserId(securityService.getCurrentUserId(service));
            fireEvent(new SaveEvent(this, category));
        }
    }

    public static abstract class CustomCategoryFormEvent extends ComponentEvent<CustomCategoryForm> {
        private Category category;

        protected CustomCategoryFormEvent(CustomCategoryForm source, Category category) {
            super(source, false);
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }
    }

    public static class SaveEvent extends CustomCategoryFormEvent {
        SaveEvent(CustomCategoryForm source, Category category) {
            super(source, category);
        }
    }

    public static class CloseEvent extends  CustomCategoryFormEvent {
        CloseEvent(CustomCategoryForm source) {
            super(source, null);
        }
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }
}
