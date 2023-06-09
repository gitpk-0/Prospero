package pk.wgu.capstone.views;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.StyleSheet;
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
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.criteria.*;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.domain.Specification;
import pk.wgu.capstone.data.entity.Category;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;
import pk.wgu.capstone.views.forms.TransactionForm;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SpringComponent
@Scope("prototype")
@PermitAll // all logged-in users can access this page
@Route(value = "", layout = MainLayout.class)
@PageTitle("Home | Prospero")
@StyleSheet(value = "./themes/prospero/views/transaction-view.css")
public class TransactionView extends Div {

    private SecurityService securityService;
    private PfmService service;
    TransactionForm transactionForm;
    Dialog dialog;

    private Filters filters;

    Grid<Transaction> grid = new Grid<>(Transaction.class);
    Binder<Category> categoryBinder;

    TextField filterText = new TextField();

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy");

    public TransactionView(SecurityService securityService, PfmService service) {
        addClassNames("transaction-view");
        this.securityService = securityService;
        this.service = service;

        filters = new Filters(this::refreshGrid, securityService, service);


        setSizeFull(); // makes this view the same size as the entire browser window
        checkForMessage();

        configureGrid();
        sortGrid();
        configureForm();
        // addClassName("name");

        add(
                // getSearchbar(),
                createMobileFilters(),
                filters,
                getContent()
        );

        updateList();
        closeTransactionEditor();
    }

    // Search bar
    private Component getSearchbar() {
        filterText.setPlaceholder("Filter by description...");
        filterText.addClassName("filter-text");
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

    public static abstract class ListViewEvent extends ComponentEvent<TransactionView> {
        private Category category;

        protected ListViewEvent(TransactionView source, Category category) {
            super(source, false);
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }
    }

    public static class SaveEvent extends TransactionView.ListViewEvent {
        SaveEvent(TransactionView source, Category category) {
            super(source, category);
        }
    }

    public static class CloseEvent extends TransactionView.ListViewEvent {
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

    // Filters
    public static class Filters extends Div implements Specification<Transaction> {

        private final TextField description = new TextField("Name");
        private final DatePicker startDate = new DatePicker("Transaction Date");
        private final DatePicker endDate = new DatePicker();
        private final MultiSelectComboBox<String> categories = new MultiSelectComboBox<>("Category");
        private final CheckboxGroup<String> types = new CheckboxGroup<>("Type");

        private SecurityService securityService;
        private PfmService service;

        public Filters(Runnable onSearch, SecurityService securityService, PfmService service) {
            this.securityService = securityService;
            this.service = service;

            setWidthFull();
            // addClassName("transaction-view");
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            description.setPlaceholder("Description");

            List<String> categoryNames = service.findAllCategories()
                    .stream().map(Category::getName).collect(Collectors.toList());
            categories.setItems(categoryNames);

            types.setItems("Income", "Expense");
            types.addClassName("double-width");

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                description.clear();
                startDate.clear();
                endDate.clear();
                categories.clear();
                types.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(description, categories, types, actions);
        }

        @Override
        public Predicate toPredicate(Root<Transaction> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (!description.isEmpty()) {
                String lowerCaseFilter = description.getValue().toLowerCase();
                Predicate descriptionMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                        lowerCaseFilter + "%");
                predicates.add(criteriaBuilder.or(descriptionMatch));
            }
            if (startDate.getValue() != null) {
                String databaseColumn = "dateOfBirth";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(databaseColumn),
                        criteriaBuilder.literal(startDate.getValue())));
            }
            if (endDate.getValue() != null) {
                String databaseColumn = "dateOfBirth";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.literal(endDate.getValue()),
                        root.get(databaseColumn)));
            }
            if (!categories.isEmpty()) {
                String databaseColumn = "occupation";
                List<Predicate> categoryPredicates = new ArrayList<>();
                for (String category : categories.getValue()) {
                    categoryPredicates
                            .add(criteriaBuilder.equal(criteriaBuilder.literal(category), root.get(databaseColumn)));
                }
                predicates.add(criteriaBuilder.or(categoryPredicates.toArray(Predicate[]::new)));
            }
            if (!types.isEmpty()) {
                String databaseColumn = "type";
                List<Predicate> typePredicates = new ArrayList<>();
                for (String type : types.getValue()) {
                    typePredicates.add(criteriaBuilder.equal(criteriaBuilder.literal(type), root.get(databaseColumn)));
                }
                predicates.add(criteriaBuilder.or(typePredicates.toArray(Predicate[]::new)));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }

        private Component createDateRangeFilter() {
            startDate.setPlaceholder("From");

            endDate.setPlaceholder("To");

            // For screen readers
            setAriaLabel(startDate, "From date");
            setAriaLabel(endDate, "To date");

            FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

            return dateRangeComponent;
        }

        private void setAriaLabel(DatePicker datePicker, String label) {
            datePicker.getElement().executeJs("const input = this.inputElement;" //
                    + "input.setAttribute('aria-label', $0);" //
                    + "input.removeAttribute('aria-labelledby');", label);
        }

        private String ignoreCharacters(String characters, String in) {
            String result = in;
            for (int i = 0; i < characters.length(); i++) {
                result = result.replace("" + characters.charAt(i), "");
            }
            return result;
        }

        private Expression<String> ignoreCharacters(String characters, CriteriaBuilder criteriaBuilder,
                                                    Expression<String> inExpression) {
            Expression<String> expression = inExpression;
            for (int i = 0; i < characters.length(); i++) {
                expression = criteriaBuilder.function("replace", String.class, expression,
                        criteriaBuilder.literal(characters.charAt(i)), criteriaBuilder.literal(""));
            }
            return expression;
        }
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
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }


    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }
}