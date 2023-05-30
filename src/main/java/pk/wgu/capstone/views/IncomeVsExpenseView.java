package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.entity.report.CategoryTotal;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;

@Route(value = "income-vs-expense", layout = MainLayout.class)
@PageTitle("I & E | Prospero")
@PermitAll
@CssImport(value = "./styles/income-vs-expense-view-styles.css", themeFor = "vaadin-chart")
public class IncomeVsExpenseView extends VerticalLayout {

    private SecurityService securityService;
    private PfmService service;
    private Long userId;

    Grid<CategoryTotal> incomeGrid = new Grid<>(CategoryTotal.class);
    Grid<CategoryTotal> expenseGrid = new Grid<>(CategoryTotal.class);

    public IncomeVsExpenseView(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;
        this.userId = securityService.getCurrentUserId(service);

        addClassName("income-vs-expense-view");
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        configureGrids();

        add(
                // getTransactionStats(),
                getTransactionsChart(),
                getIncomeAndExpenseGridContent()
        );

        updateGrids();
    }

    private void configureGrids() {
        // INCOME GRID
        incomeGrid.removeAllColumns();

        incomeGrid.addColumn(CategoryTotal::getCategoryName).setHeader(
                        new Html("<div style='font-size: 1.2rem; font-weight:900'>Category</div>"))
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getCategoryName));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        NumberRenderer<CategoryTotal> amountRenderer = new NumberRenderer<>(CategoryTotal::getTotalAmount, currencyFormat);

        incomeGrid.addColumn(amountRenderer).setHeader(
                        new Html("<div style='font-size: 1.2rem; font-weight:900'>Amount</div>"))
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getTotalAmount));

        incomeGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        incomeGrid.asSingleSelect();
        incomeGrid.getStyle().set("margin-left", "8rem");

        // EXPENSE GRID
        expenseGrid.removeAllColumns();

        expenseGrid.addColumn(CategoryTotal::getCategoryName).setHeader(
                new Html("<div style='font-size: 1.2rem; font-weight:900'>Category</div>"))
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getCategoryName));

        expenseGrid.addColumn(amountRenderer).setHeader(
                new Html("<div style='font-size: 1.2rem; font-weight:900'>Amount</div>"))
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getTotalAmount));

        expenseGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        expenseGrid.asSingleSelect();
        expenseGrid.getStyle().set("margin-right", "2rem");
    }


    private void updateGrids() {
        List<Object[]> result = service.sumTransactionByCategory(userId);
        List<CategoryTotal> categoryTotals = result
                .stream()
                .map(row -> new CategoryTotal((String) row[0], (BigDecimal) row[1])).toList();

        List<CategoryTotal> incomeData = categoryTotals.stream()
                .filter(row -> row.getCategoryName().equals("Income")).toList();
        incomeGrid.setItems(incomeData);

        List<CategoryTotal> expenseData = categoryTotals.stream()
                .filter(row -> !row.getCategoryName().equals("Income")).toList();
        expenseGrid.setItems(expenseData);
    }

    private Component getIncomeAndExpenseGridContent() {
        HorizontalLayout content = new HorizontalLayout(incomeGrid, expenseGrid);
        content.setClassName("grids-layout");
        content.getStyle().set("margin-top", "-1.5rem");
        content.getStyle().set("grid-gap", "3rem");
        content.setWidthFull();
        return content;
    }

    // private Component getTransactionStats() {
    //     Long userId = securityService.getCurrentUserId(service);
    //
    //     Span totalTransactions = new Span(service.countTransactionsByUser(userId) + " transactions");
    //     Span incomes = new Span("$" + service.sumAllTransactionsByType(userId, Type.INCOME) + " in income");
    //     Span expenses = new Span("$" + service.sumAllTransactionsByType(userId, Type.EXPENSE) + " in expenses");
    //
    //     totalTransactions.addClassNames("text-xl", "mt-m");
    //     incomes.addClassNames("text-xl", "mt-m");
    //     expenses.addClassNames("text-xl", "mt-m");
    //
    //     HorizontalLayout stats = new HorizontalLayout(incomes, expenses);
    //
    //     stats.setAlignItems(Alignment.CENTER);
    //
    //     return stats;
    // }

    private Component getTransactionsChart() {
        Long userId = service.findUserByEmail(securityService.getAuthenticatedUser().getUsername()).getId();

        Chart columnChart = new Chart(ChartType.COLUMN);
        Configuration config = columnChart.getConfiguration();
        DataSeries dataSeries = new DataSeries();
        dataSeries.setName("Transactions");


        DataSeriesItem incomeItem = new DataSeriesItem("Income", service.sumAllTransactionsByType(userId, Type.INCOME));
        incomeItem.setClassName("income-column-bar");

        DataSeriesItem expenseItem = new DataSeriesItem("Expenses", service.sumAllTransactionsByType(userId, Type.EXPENSE));
        expenseItem.setClassName("expense-column-bar");


        dataSeries.add(incomeItem);
        dataSeries.add(expenseItem);

        DataLabels totalLabel = new DataLabels(true);
        totalLabel.setShape(Shape.CALLOUT);
        totalLabel.setY(-10);
        totalLabel.setFormatter("function() { return '$' + Highcharts.numberFormat(this.point.y, 2, '.', ',') }");
        totalLabel.setInside(true);
        incomeItem.setDataLabels(totalLabel);
        expenseItem.setDataLabels(totalLabel);


        Tooltip tooltip = new Tooltip();
        tooltip.setFormatter("function() {" +
                "    return '<br/><b>' + this.point.name + '</b><br/>' +" +
                "        'Total: $' + Highcharts.numberFormat(this.point.y, 2, '.', ',') + '<br/><br/>'}");

        // tooltip.setFormatter("function() {\n" +
        //         "      return '<b>' + this.point.name + '</b><br/>' +\n" +
        //         "        this.point.series.name + ': ' + this.point.y + '<br/>' +\n" +
        //         "        'Color: ' + this.point.color;\n" +
        //         "    }");
        tooltip.setEnabled(true);
        config.setTooltip(tooltip);

        config.getChart().setStyledMode(true);
        config.getLegend().setEnabled(false);
        config.setSeries(dataSeries);
        config.getxAxis().setClassName("huge-axis");

        XAxis xAxis = config.getxAxis();
        xAxis.setType(AxisType.CATEGORY);

        YAxis yAxis = config.getyAxis();
        yAxis.setTitle("Amount");

        columnChart.addClassName("totals-chart");
        columnChart.setHeight("800px");

        return columnChart;
    }
}