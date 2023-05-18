package pk.wgu.capstone.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
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

    private void configureForm() {
        form = new TransactionForm(service.)
    }

    private void configureGrid() {

    }
}
