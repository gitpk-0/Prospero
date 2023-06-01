package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import pk.wgu.capstone.data.entity.Category;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;
import pk.wgu.capstone.views.forms.TransactionForm;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SpringComponent
@Scope("prototype")
@PermitAll // all logged-in users can access this page
@Route(value = "", layout = MainLayout.class)
@PageTitle("Home | Prospero")
public class ListView extends VerticalLayout {

    private SecurityService securityService;
    private PfmService service;
    TransactionForm transactionForm;
    Dialog dialog;

    Grid<Transaction> grid = new Grid<>(Transaction.class);
    Binder<Category> categoryBinder;

    TextField filterText = new TextField();

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy");

    public ListView(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        addClassName("list-view");
        setSizeFull(); // makes this view the same size as the entire browser window
        checkForMessage();

        configureGrid();
        sortGrid();
        configureForm();
        addClassName("name");

        add(
                getSearchbar(),
                getContent()
        );

        updateList();
        closeTransactionEditor();
    }

    // Search bar
    private Component getSearchbar() {
        filterText.setPlaceholder("Filter by description...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY); // prevents the database from being hit with every keystroke
        filterText.addValueChangeListener(e -> updateList());

        Button addTransactionButton = new Button("Add Transaction");
        addTransactionButton.addClickListener(e -> addTransaction());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addTransactionButton);
        toolbar.addClassName("toolbar");

        return toolbar;
    }

    // Page content - grid and transaction form
    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, transactionForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, transactionForm);
        content.addClassName("content");
        content.setSizeFull();

        return content;
    }

    // Grid
    private void configureGrid() {
        grid.removeAllColumns();
        grid.addClassName("transaction-grid");
        grid.setSizeFull();

        grid.addColumn(transaction -> transaction.getDate().toLocalDate().format(dateFormatter)).setKey("date")
                .setHeader("Date").setSortable(true).setComparator(Comparator.comparing(Transaction::getDate).reversed());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        NumberRenderer<Transaction> amountRenderer = new NumberRenderer<>(Transaction::getAmount, currencyFormat);
        grid.addColumn(amountRenderer).setHeader("Amount")
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getAmount));

        grid.addColumn(transaction -> {
                    String label = String.valueOf(transaction.getType());
                    return label.charAt(0) + label.substring(1).toLowerCase();
                }).setHeader("Type")
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));


        grid.addColumn(transaction -> transaction.getCategory().getName()).setHeader("Category")
                .setSortable(true)
                .setComparator(Comparator.comparing(transaction -> transaction.getCategory().getName()));

        grid.addColumn(Transaction::getDescription).setHeader("Description")
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getDescription));

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(e -> editTransaction(e.getValue()));
    }

    private void updateList() {
        Long userId = securityService.getCurrentUserId(service);
        grid.setItems(service.findAllTransactions(userId, filterText.getValue()));
    }

    private void sortGrid() {
        GridSortOrder<Transaction> sortOrder = new GridSortOrder<>(grid.getColumnByKey("date"), SortDirection.ASCENDING);
        grid.sort(List.of(sortOrder));
    }

    // Transaction Form
    private void configureForm() {
        Long userId = securityService.getCurrentUserId(service);
        List<Category> categories = service.findAllCategories().stream()
                .filter(c -> c.isDefaultCategory() || c.hasUserId(userId))
                .toList();
        transactionForm = new TransactionForm(
                securityService,
                service,
                categories,
                service.findAllTypes());
        transactionForm.setWidth("25em");

        transactionForm.createNewCategoryBtn.addClickListener(e -> {
            closeTransactionEditor();
            openNewCategoryDialog();
        });

        transactionForm.addSaveListener(this::saveTransaction);
        transactionForm.addDeleteListener(this::deleteTransaction);
        transactionForm.addCloseListener(e -> closeTransactionEditor());
    }

    private void saveTransaction(TransactionForm.SaveEvent saveEvent) {
        service.saveTransaction(saveEvent.getTransaction());
        updateList();
        closeTransactionEditor();
    }

    private void deleteTransaction(TransactionForm.DeleteEvent deleteEvent) {
        service.deleteTransaction(deleteEvent.getTransaction());
        updateList();
        closeTransactionEditor();
    }

    private void closeTransactionEditor() {
        grid.deselectAll();
        transactionForm.setTransaction(null);
        removeClassName("editing");
        transactionForm.setVisible(false);
    }

    private void addTransaction() {
        grid.asSingleSelect().clear(); // unselect transaction if one is selected
        editTransaction(new Transaction());
    }

    public void editTransaction(Transaction transaction) {
        if (transaction == null) {
            closeTransactionEditor();
        } else {
            transactionForm.setTransaction(transaction);
            transactionForm.setVisible(true);
            addClassName("editing");
        }
    }

    // New Category Dialog
    private void openNewCategoryDialog() {
        dialog = new Dialog();
        dialog.setHeaderTitle("Create a new Transaction Category");
        dialog.setDraggable(true);

        // if user clicks outside the dialog box, dialog is closed
        dialog.setModal(true); // blocks the user from interacting with the rest of the UI
        // dialog.setResizable(true);

        categoryBinder = new BeanValidationBinder<>(Category.class);

        ComboBox<Type> typeSelect = new ComboBox<>("Transaction Type");
        categoryBinder.forField(typeSelect)
                .withValidator(Objects::nonNull, "Type is required")
                .bind(Category::getType, Category::setType);

        TextField categoryName = new TextField("New Category Name");
        categoryBinder.forField(categoryName)
                .withValidator(Objects::nonNull, "Category name is required")
                .bind(Category::getName, Category::setName);
        this.setCategory(new Category());
        categoryBinder.bindInstanceFields(this); // bind fields to the data model

        typeSelect.setItems(service.findAllTypes());
        typeSelect.setItemLabelGenerator(Type::name);
        typeSelect.setPlaceholder("Select a transaction type");
        categoryName.setPlaceholder("Enter a new category name");

        VerticalLayout dialogLayout = new VerticalLayout(typeSelect,
                categoryName);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        dialog.add(dialogLayout);

        // buttons
        Button saveButton = new Button("Save", e -> validateAndSave(dialog));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> dialog.close());

        // add keyboard shortcuts to buttons
        saveButton.addClickShortcut(Key.ENTER);
        cancelButton.addClickShortcut(Key.ESCAPE);

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        dialog.open();
    }

    public void setCategory(Category category) {
        categoryBinder.setBean(category);
    }

    public Category getCategory() {
        return categoryBinder.getBean();
    }

    private void validateAndSave(Dialog dialog) {
        if (categoryBinder.isValid()) {
            Category newCategory = categoryBinder.getBean();
            if (isValidNewCategory(newCategory)) {
                newCategory.setDefault(false);
                SaveEvent saveEvent = new ListView.SaveEvent(this, newCategory);
                fireEvent(saveEvent);
                saveCategory(saveEvent);
                dialog.close();
                showSuccess();
                reloadPage();
            } else {
                System.out.println("Problem creating new category");
                showFailure();
            }
        }
    }

    private boolean isValidNewCategory(Category newCategory) {
        Long userId = securityService.getCurrentUserId(service);
        String newCategoryName = newCategory.getName().toLowerCase();

        // new category validation
        List<Category> allCategories = service.findAllCategories();
        for (Category c : allCategories) {
            String cName = c.getName().toLowerCase();
            boolean sameNameAndType = cName.equals(newCategoryName) && c.getType().equals(newCategory.getType());

            // if default category with the same name and type already exists
            if (c.getDefault().equals(true) && sameNameAndType) {
                return false; // show error
            }

            // if new category with same name and type already exists with current user
            if (sameNameAndType && c.hasUserId(userId)) {
                return false; // show error
            }
        }
        return true;
    }

    private void saveCategory(ListView.SaveEvent saveEvent) {
        Category newCategory = saveEvent.getCategory();
        String newCategoryName = newCategory.getName();

        String userIdStr = securityService.getCurrentUserId(service) + ",";
        Category existingCategory = service.findCategoryByName(newCategoryName);

        if (existingCategory == null) {
            newCategory.setUserIdsCsv(userIdStr);
            service.addNewCategory(newCategory);
        } else {
            Long existingCategoryId = existingCategory.getId();
            service.updateCustomCategoryUserIds(existingCategoryId, userIdStr);
        }
    }

    private void reloadPage() {
        VaadinSession.getCurrent().setAttribute("createCategorySuccess", "Category successfully created!");
        UI.getCurrent().access(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            UI.getCurrent().getPage().reload();
        });
    }

    public static abstract class ListViewEvent extends ComponentEvent<ListView> {
        private Category category;

        protected ListViewEvent(ListView source, Category category) {
            super(source, false);
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }
    }

    public static class SaveEvent extends ListView.ListViewEvent {
        SaveEvent(ListView source, Category category) {
            super(source, category);
        }
    }

    public static class CloseEvent extends ListView.ListViewEvent {
        CloseEvent(ListView source) {
            super(source, null);
        }
    }

    private void showSuccess() {
        Notification notification =
                Notification.show("Category successfully created!");

        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setDuration(3000);
        notification.setPosition(Notification.Position.MIDDLE);
    }

    private void showFailure() {
        Notification notification =
                Notification.show("There was an error creating the category you entered. " +
                        "Please ensure that the category you provided does not already exist.");

        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setDuration(2500);
        notification.setPosition(Notification.Position.MIDDLE);
    }

    private void checkForMessage() {
        String message = (String) VaadinSession.getCurrent().getAttribute("createCategorySuccess");
        if (message != null) {
            showSuccess();
            VaadinSession.getCurrent().setAttribute("createCategorySuccess", null);
        }
    }
}
