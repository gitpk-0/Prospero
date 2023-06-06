package pk.wgu.capstone.views.budget;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.entity.Budget;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.entity.report.CategoryTotal;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;
import pk.wgu.capstone.views.MainLayout;
import pk.wgu.capstone.views.forms.BudgetForm;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@PageTitle("Budget List")
@Route(value = "budget-list", layout = MainLayout.class)
@PermitAll
// @CssImport(value = "./styles/budget-list-view.css")
@CssImport(value = "./themes/prospero/views/budget-chart.css", themeFor = "vaadin-chart")
@CssImport(value = "./themes/prospero/views/budget-list-view.css")
public class BudgetListView extends Main implements HasComponents, HasStyle {

    private OrderedList budgetContainer;
    private SecurityService securityService;
    private PfmService service;
    private VerticalLayout dialogLayout;
    private Dialog dialog;

    BudgetForm budgetForm;

    public BudgetListView(SecurityService securityService, PfmService service) {
        addClassName("budget-list-view");
        this.securityService = securityService;
        this.service = service;

        generateUI();
        configureBudgetCards();

        // budgetContainer.add(new BudgetListViewCard("First Budget", 0.25, "Complete"));
        // budgetContainer.add(new BudgetListViewCard("Second Budget", 0.5, "In Progress"));
        // budgetContainer.add(new BudgetListViewCard("Big Budget", 0.75, "In Progress"));
        // budgetContainer.add(new BudgetListViewCard("Full Budget", 1.25, "Not Started"));
        // budgetContainer.add(new BudgetListViewCard("Mid Budget", 0.85, "In Progress"));
        // budgetContainer.add(new BudgetListViewCard("Little Budget", 0.99, "Complete"));
        // budgetContainer.add(new BudgetListViewCard("Little Budget", 1.0, "Complete"));
        // budgetContainer.add(new BudgetListViewCard("Little Budget", 0.745, "Complete"));


    }

    private void generateUI() {

        addClassName("budget-list-view");
        addClassNames(LumoUtility.MaxWidth.SCREEN_LARGE,
                LumoUtility.Margin.Horizontal.AUTO,
                LumoUtility.Padding.Bottom.LARGE,
                LumoUtility.Padding.Horizontal.LARGE);

        VerticalLayout headerContainer = new VerticalLayout();
        headerContainer.addClassNames(LumoUtility.AlignItems.CENTER);
        H2 header = new H2("Budgets");
        header.addClassNames(LumoUtility.Margin.Bottom.NONE,
                LumoUtility.Margin.Top.NONE, LumoUtility.FontSize.XXLARGE);

        Paragraph description = new Paragraph("Reach your goals");
        description.addClassNames(LumoUtility.Margin.Bottom.NONE,
                LumoUtility.Margin.Top.NONE, LumoUtility.TextColor.SECONDARY);

        headerContainer.add(header, description);

        HorizontalLayout optionsContainer = new HorizontalLayout();
        optionsContainer.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        optionsContainer.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Newest to oldest", "Oldest to newest");
        sortBy.setValue("Newest to oldest");

        Button createBudgetBtn = new Button("Create Budget");
        createBudgetBtn.addClickListener(e -> {
            addBudget();
        });

        optionsContainer.add(sortBy, createBudgetBtn);

        budgetContainer = new OrderedList();
        budgetContainer.addClassNames(LumoUtility.Gap.MEDIUM,
                LumoUtility.Display.GRID, LumoUtility.ListStyleType.NONE,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE);

        add(
                headerContainer,
                optionsContainer,
                budgetContainer);
    }

    private void configureBudgetCards() {
        budgetContainer.removeAll();
        Long userId = securityService.getCurrentUserId(service);

        List<Budget> budgets = service.getBudgetsByUserId(userId);
        budgets.stream().forEach(budget -> {
            BudgetListViewCard card = new BudgetListViewCard(
                    budget.getName(),
                    calcBudgetProgress(budget, userId),
                    calcBudgetStatus(budget)
            );
            card.addClickListener(e -> {
                editBudget(budget);
            });
            budgetContainer.add(card);
        });

    }

    private String calcBudgetStatus(Budget budget) {
        Date start = budget.getStart();
        Date end = budget.getEnd();
        Date currentDate = new Date(LocalDate.now().toEpochDay());

        if (currentDate.before(start)) {
            return "Not Started";
        } else if (currentDate.after(end)) {
            return "Completed";
        } else {
            return "In Progress";
        }
    }

    private Double calcBudgetProgress(Budget budget, Long userId) {
        Date start = budget.getStart();
        Date end = budget.getEnd();
        System.out.println("Start: " + start);
        System.out.println("end: " + end);
        System.out.println("userId: " + userId);
        System.out.println("GetSumTransactionsInDateRange: " + service.getSumTransactionsInDateRange(start, end, userId));
        Double spendingTotal = Double.valueOf(String.valueOf(
                service.getSumTransactionsInDateRange(start, end, userId)));
        Double spendingGoal = Double.valueOf(String.valueOf(budget.getSpendingGoal()));

        return spendingTotal / spendingGoal;
    }

    // Budget Form
    private void configureForm() {
        Long userId = securityService.getCurrentUserId(service);
        budgetForm = new BudgetForm(securityService, service);
        budgetForm.setWidth("25em");

        budgetForm.addSaveListener(this::saveBudget);
        budgetForm.addDeleteListener(this::deleteBudget);
        budgetForm.addCloseListener(e -> closeBudgetEditor());
    }

    private void saveBudget(BudgetForm.SaveEvent saveEvent) {
        service.saveBudget(saveEvent.getBudget());
        configureBudgetCards();
        closeBudgetEditor();
    }

    private void deleteBudget(BudgetForm.DeleteEvent deleteEvent) {
        service.deleteBudget(deleteEvent.getBudget());
        configureBudgetCards();
        closeBudgetEditor();
    }

    private void closeBudgetEditor() {
        budgetForm.setBudget(null);
        removeClassName("editing");
        budgetForm.setVisible(false);
        dialogLayout = null;
    }

    private void addBudget() {
        editBudget(new Budget());
    }

    public void editBudget(Budget budget) {
        if (budget == null) {
            closeBudgetEditor();
        } else {
            if (dialogLayout == null) {
                configureForm();
                // dialogLayout = new VerticalLayout(budgetForm);
                dialogLayout = new VerticalLayout(getBudgetChart(budget));
                // dialogLayout.getStyle().set("width" , "40rem").set("height", "40rem");
                dialogLayout.addClassName("budget-dialog");
                dialog = new Dialog(dialogLayout);
            }
            Button cancelButton = new Button("Cancel", e -> {
                dialog.close();
                closeBudgetEditor();
            });
            dialog.add(cancelButton);
            budgetForm.setBudget(budget);
            dialog.open();
        }
    }

    private Component getBudgetChart(Budget budget) {
        Long userId = securityService.getCurrentUserId(service);

        Chart pieChart = new Chart(ChartType.PIE);
        pieChart.setThemeName("classic");
        pieChart.setClassName("expense-pie-chart");
        Configuration config = pieChart.getConfiguration();
        DataSeries dataSeries = new DataSeries();
        dataSeries.setName("Expenses");

        List<Object[]> expenseResults =
                service.sumTransactionsInDateRangeByCategory(userId, Type.EXPENSE, budget.getStart(), budget.getEnd());

        if (!expenseResults.isEmpty()) {
            List<CategoryTotal> expenseData = expenseResults
                    .stream()
                    .map(row -> new CategoryTotal((String) row[0], (BigDecimal) row[1])).toList();

            expenseData.stream().forEach(dataItem -> {
                DataSeriesItem expenseItem = new DataSeriesItem(dataItem.getCategoryName(), dataItem.getTotalAmount());
                expenseItem.setClassName("expense-item-slice");
                dataSeries.add(expenseItem);
                DataLabels totalLabel = new DataLabels(true);
                totalLabel.setShape(Shape.CALLOUT);
                totalLabel.setY(-10);
                totalLabel.setFormatter("function() { return this.point.name }");
                totalLabel.setInside(true);
                expenseItem.setDataLabels(totalLabel);
            });

            Tooltip tooltip = new Tooltip();
            tooltip.setFormatter("function() {" +
                    "    return '<br/><b>' + this.point.name + '</b><br/>' +" +
                    "        '$' + Highcharts.numberFormat(this.point.y, 2, '.', ',') + '<br/><br/>'}");


            tooltip.setEnabled(true);
            config.setTooltip(tooltip);

            config.getChart().setStyledMode(true);
            config.getLegend().setEnabled(false);
            config.setSeries(dataSeries);

            pieChart.addClassName("budget-pie-chart");

            return pieChart;
        } else {
            return new HorizontalLayout(new Text("Problem w chart"));
        }

    }
}
