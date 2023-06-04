package pk.wgu.capstone.views.budget;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.views.MainLayout;

@PageTitle("Budget List")
@Route(value = "budget-list", layout = MainLayout.class)
@PermitAll
@CssImport(value = "./styles/budget-list-view.css")
public class BudgetListView extends Main implements HasComponents, HasStyle {

    private OrderedList budgetContainer;

    public BudgetListView() {
        generateUI();

        budgetContainer.add(new BudgetListViewCard("First Budget", 0.25));
        budgetContainer.add(new BudgetListViewCard("Full Budget", 1.0));
        budgetContainer.add(new BudgetListViewCard("Second Budget", 0.5));
        budgetContainer.add(new BudgetListViewCard("Big Budget", 0.75));
        budgetContainer.add(new BudgetListViewCard("Mid Budget", 0.85));
        budgetContainer.add(new BudgetListViewCard("Little Budget", 0.99));


    }

    private void generateUI() {

        addClassName("budget-list-view");
        addClassNames(LumoUtility.MaxWidth.SCREEN_LARGE,
                LumoUtility.Margin.Horizontal.AUTO,
                LumoUtility.Padding.Bottom.LARGE,
                LumoUtility.Padding.Horizontal.LARGE);

        HorizontalLayout container = new HorizontalLayout();
        container.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.BETWEEN);

        VerticalLayout headerContainer = new VerticalLayout();
        H2 header = new H2("Budgets");
        header.addClassNames(LumoUtility.Margin.Bottom.NONE,
                LumoUtility.Margin.Top.XLARGE, LumoUtility.FontSize.XXXLARGE);

        Paragraph description = new Paragraph("Reach your goals");
        description.addClassNames(LumoUtility.Margin.Bottom.XLARGE,
                LumoUtility.Margin.Top.NONE, LumoUtility.TextColor.SECONDARY);

        headerContainer.add(header, description);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Newest to oldest", "Oldest to newest");
        sortBy.setValue("Newest to oldest");

        budgetContainer = new OrderedList();
        budgetContainer.addClassNames(LumoUtility.Gap.MEDIUM,
                LumoUtility.Display.GRID, LumoUtility.ListStyleType.NONE,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE);

        container.add(headerContainer, sortBy);
        add(container, budgetContainer);
    }
}
