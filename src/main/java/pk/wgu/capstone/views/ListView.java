package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
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
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;
import pk.wgu.capstone.views.forms.TransactionForm;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@SpringComponent
@Scope("prototype")
@PageTitle("Home | Prospero")
@Route(value = "", layout = MainLayout.class)
@PermitAll // all logged-in users can access this page
public class ListView extends VerticalLayout {

    private SecurityService securityService;
    private PfmService service;
    TransactionForm form;

    Grid<Transaction> grid = new Grid<>(Transaction.class);

    TextField filterText = new TextField();

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy");
    private boolean isEditorOpen = false;

    public ListView(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        addClassName("list-view");
        setSizeFull(); // makes this view the same size as the entire browser window

        configureGrid();
        sortGrid();
        configureForm();
        addClassName("name");

        add(
                getToolbar(),
                getContent()
        );

        updateList();
        closeEditor();
    }

    private void sortGrid() {
        GridSortOrder<Transaction> sortOrder = new GridSortOrder<>(grid.getColumnByKey("date"), SortDirection.ASCENDING);
        grid.sort(List.of(sortOrder));
    }

    private void configureGrid() {
        grid.removeAllColumns();
        grid.addClassName("transaction-grid");
        grid.setSizeFull();

        grid.addColumn(t -> t.getDate().toLocalDate().format(dateFormatter)).setKey("date").setHeader("Date")
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getDate).reversed());

        NumberRenderer<Transaction> amountRenderer = new NumberRenderer<>(Transaction::getAmount, "$ %.2f");
        grid.addColumn(amountRenderer).setHeader("Amount")
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getAmount));

        grid.addColumn(Transaction::getDescription).setHeader("Description")
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getDescription));

        grid.addColumn(transaction -> transaction.getCategory().getName()).setHeader("Category")
                .setSortable(true)
                .setComparator(Comparator.comparing(transaction -> transaction.getCategory().getName()));

        grid.addColumn(transaction -> {
                    String label = String.valueOf(transaction.getType());
                    return label.charAt(0) + label.substring(1).toLowerCase();
                }).setHeader("Type")
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.getColumns().forEach(col -> col.setAutoWidth(true));


        grid.asSingleSelect().addValueChangeListener(e -> {
            if (!isEditorOpen) {
                editTransaction(e.getValue());
            }
        });

    }

    private void configureForm() {
        form = new TransactionForm(
                securityService,
                service,
                service.findAllCategories(),
                service.findAllTypes());
        form.setWidth("25em");

        form.addSaveListener(this::saveTransaction);
        form.addDeleteListener(this::deleteTransaction);
        form.addCloseListener(e -> closeEditor());
    }

    private void saveTransaction(TransactionForm.SaveEvent saveEvent) {
        service.saveTransaction(saveEvent.getTransaction());
        updateList();
        closeEditor();
    }

    private void deleteTransaction(TransactionForm.DeleteEvent deleteEvent) {
        service.deleteTransaction(deleteEvent.getTransaction());
        updateList();
        closeEditor();
    }

    private void closeEditor() {
        grid.deselectAll();
        form.setTransaction(null);
        removeClassName("editing");
        isEditorOpen = false;
        form.setVisible(false);
    }

    private void updateList() {
        Long userId = securityService.getCurrentUserId(service);
        grid.setItems(service.findAllTransactions(userId, filterText.getValue()));
    }

    private void addTransaction() {
        grid.asSingleSelect().clear(); // unselect transaction if one is selected
        editTransaction(new Transaction());
    }

    private void editTransaction(Transaction transaction) {
        if (transaction == null) {
            closeEditor();
        } else {
            form.setTransaction(transaction);
            form.setVisible(true);
            addClassName("editing");
            isEditorOpen = true;
        }
    }

    private Component getToolbar() {
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

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassName("content");
        content.setSizeFull();

        return content;
    }
}
