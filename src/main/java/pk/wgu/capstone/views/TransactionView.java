package pk.wgu.capstone.views;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import pk.wgu.capstone.data.entity.Category;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;
import pk.wgu.capstone.views.forms.TransactionForm;

import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SpringComponent
@Scope("prototype")
@PermitAll // all logged-in users can access this page
@Route(value = "transactions", layout = MainLayout.class)
@PageTitle("Transactions | Prospero")
@CssImport(value = "./themes/prospero/views/transaction-view.css")
public class TransactionView extends Div {

    private SecurityService securityService;
    private PfmService service;

    TransactionForm transactionForm;
    Dialog dialog;

    Grid<Transaction> grid = new Grid<>(Transaction.class);
    Binder<Category> categoryBinder;

    Component filterDiv;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy");

    // Filters
    private final TextField description = new TextField("Description");
    private final DatePicker startDate = new DatePicker("Transaction Date");
    private final DatePicker endDate = new DatePicker();
    private final Select<String> categorySelect = new Select<>();
    private final RadioButtonGroup<String> types = new RadioButtonGroup<>("Type");

    public TransactionView(SecurityService securityService, PfmService service) {
        addClassNames("transaction-view");
        this.securityService = securityService;
        this.service = service;

        categorySelect.setLabel("Category");

        setSizeFull(); // makes this view the same size as the entire browser window
        checkForMessage();

        configureGrid();
        sortGrid();
        configureForm();

        filterDiv = createFilterLayout();

        add(
                createMobileFilters(),
                filterDiv,
                getContent()
        );

        updateList(false);
        closeTransactionEditor();
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

    private void updateList(boolean filterList) {
        Long userId = securityService.getCurrentUserId(service);

        if (filterList) {
            // filter transactions grid based on the provided filter values
            List<Transaction> filteredTransactions = service.getFilteredTransactions(
                    userId,
                    description.getValue() == null ? null : description.getValue().trim(),
                    startDate.getValue() == null ? null : Date.valueOf(startDate.getValue()),
                    endDate.getValue() == null ? null : Date.valueOf(endDate.getValue()),
                    categorySelect.getValue() == null ? null : categorySelect.getValue(),
                    types.getValue() == null ? null : types.getValue()
            );
            grid.setItems(filteredTransactions);
        } else {
            // return all transactions for the current user
            grid.setItems(service.findAllTransactions(userId));
            resetFilterFields();
        }
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
        updateList(false);
        closeTransactionEditor();
    }

    private void deleteTransaction(TransactionForm.DeleteEvent deleteEvent) {
        service.deleteTransaction(deleteEvent.getTransaction());
        updateList(false);
        closeTransactionEditor();
    }

    private void closeTransactionEditor() {
        grid.deselectAll();
        transactionForm.setTransaction(null);
        removeClassName("editing");
        transactionForm.setVisible(false);
    }

    private void addTransaction(String userTimeZone) {
        grid.asSingleSelect().clear(); // unselect transaction if one is selected
        Transaction newTransaction = new Transaction();

        // Adjust default date based on user's time zone
        ZonedDateTime userDateTime = ZonedDateTime.now(ZoneId.of(userTimeZone));
        LocalDate userLocalDate = userDateTime.toLocalDate();
        newTransaction.setDate(Date.valueOf(userLocalDate));

        editTransaction(newTransaction);
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
        setCategory(new Category());

        ComboBox<Type> typeSelect = new ComboBox<>("Transaction Type");
        categoryBinder.forField(typeSelect)
                .withValidator(Objects::nonNull, "Type is required")
                .bind(Category::getType, Category::setType);

        TextField categoryName = new TextField("New Category Name");
        categoryBinder.forField(categoryName)
                .withValidator(Objects::nonNull, "Category name is required")
                .bind(Category::getName, Category::setName);

        categoryBinder.bindInstanceFields(dialog); // bind fields to the data model

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
                SaveEvent saveEvent = new TransactionView.SaveEvent(this, newCategory);
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

        if (newCategoryName.isEmpty() || newCategoryName.isBlank()) {
            return false; // show error
        }

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

    private void saveCategory(TransactionView.SaveEvent saveEvent) {
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
        UI.getCurrent().getPage().reload();
    }

    public static abstract class TransactionViewEvent extends ComponentEvent<TransactionView> {
        private Category category;

        protected TransactionViewEvent(TransactionView source, Category category) {
            super(source, false);
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }
    }

    public static class SaveEvent extends TransactionViewEvent {
        SaveEvent(TransactionView source, Category category) {
            super(source, category);
        }
    }

    public static class CloseEvent extends TransactionViewEvent {
        CloseEvent(TransactionView source) {
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
        ConfirmDialog dialog = new ConfirmDialog();

        Icon errorIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE_O);
        errorIcon.setColor("#ff7745");
        errorIcon.setSize("2.5em");

        H2 headerMessage = new H2("Creation failed");
        headerMessage.getStyle().set("font-family", "system-ui").set("font-weight", "900");

        HorizontalLayout headerLayout = new HorizontalLayout(errorIcon, headerMessage);
        headerLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        dialog.setHeader(headerLayout);
        dialog.setText(new Html(
                "<h5>There was an error creating the category you entered. Please ensure that the category you " +
                        "provided does not already exist. <br/><br/>" +
                        "If the problem persists, please contact: " +
                        "<b><a href=\"mailto:prospero.support@pm.me\">prospero.support@pm.me</a></b></h5>"));

        dialog.setConfirmText("OK");
        dialog.open();
    }

    private void checkForMessage() {
        String message = (String) VaadinSession.getCurrent().getAttribute("createCategorySuccess");
        if (message != null) {
            showSuccess();
            VaadinSession.getCurrent().setAttribute("createCategorySuccess", null);
        }
    }

    private Component createFilterLayout() {
        startDate.addValueChangeListener(e -> {
            if (startDate.getValue() != null) {
                endDate.setMin(startDate.getValue().plusDays(1));
            }
        });

        Div filterDiv = new Div();
        filterDiv.setWidthFull();
        filterDiv.addClassName("filter-layout");
        filterDiv.addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                LumoUtility.BoxSizing.BORDER);

        categorySelect.setItems(getCategoryNames(null));

        types.setItems("Income", "Expense");
        types.addClassName("double-width");

        types.addValueChangeListener(e -> {
            if (types.getValue() != null) {
                categorySelect.clear();
                if (Objects.equals(types.getValue(), "Income")) {
                    categorySelect.setItems(getCategoryNames("Income"));
                } else if (Objects.equals(types.getValue(), "Expense")) {
                    categorySelect.setItems(getCategoryNames("Expense"));
                }
            }
        });

        // Action buttons
        Button resetBtn = new Button("Reset");
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> resetFilterFields());

        Button searchBtn = new Button("Search");
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> updateList(true));

        Button addTransactionBtn = new Button("Add Transaction");
        addTransactionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTransactionBtn.getStyle().set("--lumo-primary-color", "green");
        addTransactionBtn.addClickListener(e -> {
            UI.getCurrent()
                    .getPage()
                    .executeJs("var userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone; return userTimeZone;")
                    .then(result -> {
                        String userTimeZone = result.asString();
                        if (userTimeZone == null || userTimeZone.isEmpty()) {
                            userTimeZone = "America/New_York";
                        }
                        System.out.println("User time zone: " + userTimeZone);
                        addTransaction(userTimeZone);
                    });
        });

        Div actions = new Div(resetBtn, searchBtn, addTransactionBtn);
        actions.addClassName(LumoUtility.Gap.SMALL);
        actions.addClassName("actions");

        filterDiv.add(description, createDateRangeFilter(), categorySelect, types, actions);
        return filterDiv;
    }

    private List<String> getCategoryNames(String filterByType) {
        Long userId = securityService.getCurrentUserId(service);
        List<Category> allUserCategories = service.findAllCategories().stream()
                .filter(c -> c.isDefaultCategory() || c.hasUserId(userId))
                .filter(c -> filterByType == null || c.getType().toString().equalsIgnoreCase(filterByType))
                .distinct()
                .toList();

        return allUserCategories.stream().map(Category::getName).distinct().toList();
    }

    private void resetFilterFields() {
        description.clear();
        startDate.clear();
        endDate.clear();
        categorySelect.clear();
        types.clear();
        categorySelect.setItems(getCategoryNames(null));
    }

    private Component createDateRangeFilter() {
        startDate.setPlaceholder("From");

        endDate.setPlaceholder("To");

        FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
        dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
        dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

        return dateRangeComponent;
    }


    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filterDiv.getClassNames().contains("visible")) {
                filterDiv.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filterDiv.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }
}