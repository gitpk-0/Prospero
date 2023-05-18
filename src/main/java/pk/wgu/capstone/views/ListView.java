package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.context.annotation.Scope;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.service.PfmService;

@SpringComponent
@Scope("prototype")
@PageTitle("Home | Prospero")
@Route(value = "", layout = MainLayout.class)
public class ListView extends VerticalLayout {

    Grid<Transaction> grid = new Grid<>(Transaction.class);
    TextField filterText = new TextField();
    TransactionForm form;
    private PfmService service;

    public ListView() {
        addClassName("list-view");
        setSizeFull(); // makes this view the same size as the entire browser window

        configureGrid();
        configureForm();
        addClassName("name");

        add(
                getToolbar(),
                getContent()
        );

        updateList();
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassName("transaction-grid");
        grid.setSizeFull();
        grid.setColumns("Date", "Amount", "Description");
        grid.addColumn(transaction -> transaction.getCategory().getName()).setHeader("Category");
        grid.addColumn(Transaction::getType).setHeader("Type");
    }

    private void configureForm() {
        form = new TransactionForm(service.findAllCategories(), service.findAllTypes());
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
        form.setTransaction(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void updateList() {
        grid.setItems(service.findAllTransactions(filterText.getValue()));
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
