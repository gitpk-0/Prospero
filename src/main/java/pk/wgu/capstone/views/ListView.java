package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import pk.wgu.capstone.data.entity.Category;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;
import pk.wgu.capstone.views.forms.CustomCategoryForm;
import pk.wgu.capstone.views.forms.TransactionForm;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@SpringComponent
@Scope("prototype")
@PermitAll // all logged-in users can access this page
@Route(value = "", layout = MainLayout.class)
@PageTitle("Home | Prospero")
public class ListView extends VerticalLayout {

    private SecurityService securityService;
    private PfmService service;
    TransactionForm transactionForm;
    CustomCategoryForm newCategoryForm;

    Grid<Transaction> grid = new Grid<>(Transaction.class);

    TextField filterText = new TextField();

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy");

    public ListView(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        addClassName("list-view");
        setSizeFull(); // makes this view the same size as the entire browser window

        configureGrid();
        sortGrid();
        configureCategoryForm();
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

    // Page content - grid and forms
    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, transactionForm, newCategoryForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, transactionForm);
        content.setFlexGrow(1, newCategoryForm);
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

        grid.asSingleSelect().addValueChangeListener(e -> {
            // prevent both forms from being opened simultaneously
            if (!newCategoryForm.isVisible()) {
                editTransaction(e.getValue());
            }
        });
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
        transactionForm = new TransactionForm(
                securityService,
                service,
                service.findAllCategories(),
                service.findAllTypes());
        transactionForm.setWidth("25em");


        transactionForm.createNewCategoryBtn.addClickListener(e -> {
            closeTransactionEditor();
            editCategory();
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

    private void editTransaction(Transaction transaction) {
        if (transaction == null) {
            closeTransactionEditor();
        } else {
            transactionForm.setTransaction(transaction);
            transactionForm.setVisible(true);
            addClassName("editing");
        }
    }


    // New Category Form
    private void configureCategoryForm() {
        newCategoryForm = new CustomCategoryForm(
                securityService,
                service,
                service.findAllTypes());

        newCategoryForm.setWidth("30em");
        newCategoryForm.setVisible(false);

        newCategoryForm.addSaveListener(this::saveCategory);
        newCategoryForm.addCloseListener(e -> closeCategoryEditor());
    }

    private void editCategory() {
        newCategoryForm.setCategory(new Category());
        newCategoryForm.setVisible(true);
    }

    private void saveCategory(CustomCategoryForm.SaveEvent saveEvent) {
        service.addNewCategory(saveEvent.getCategory());
        closeCategoryEditor();
        UI.getCurrent().getPage().reload(); //
    }

    private void closeCategoryEditor() {
        newCategoryForm.setVisible(false);
        updateList();
    }
}
