package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.entity.report.CategoryTotal;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Route(value = "income-vs-expense", layout = MainLayout.class)
@PageTitle("I&E | Prospero")
@PermitAll
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

        configureGrids();

        add(
                getTransactionStats(),
                getTransactionsChart(),
                getIncomeAndExpenseGridContent()
        );

        updateGrids();
    }

    private void configureGrids() {
        // INCOME GRID
        incomeGrid.removeAllColumns();
        incomeGrid.addClassName("income-grid");

        incomeGrid.addColumn(CategoryTotal::getCategoryName).setHeader("Category")
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getCategoryName));

        incomeGrid.addColumn(CategoryTotal::getTotalAmount).setHeader("Amount")
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getTotalAmount));

        incomeGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        incomeGrid.asSingleSelect();

        // EXPENSE GRID
        expenseGrid.removeAllColumns();
        expenseGrid.addClassName("expense-grid");

        expenseGrid.addColumn(CategoryTotal::getCategoryName).setHeader("Category")
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getCategoryName));

        expenseGrid.addColumn(CategoryTotal::getTotalAmount).setHeader("Amount")
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getTotalAmount));

        expenseGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        expenseGrid.asSingleSelect();
    }


    private void updateGrids() {
        List<Object[]> result = service.sumTransactionByCategory(userId);
        List<CategoryTotal> categoryTotals = result
                .stream()
                .map(row -> {
                    return new CategoryTotal((String) row[0], (BigDecimal) row[1]);
                }).toList();

        List<CategoryTotal> incomeData = categoryTotals.stream()
                .filter(row -> row.getCategoryName().equals("Income")).toList();
        incomeGrid.setItems(incomeData);

        List<CategoryTotal> expenseData = categoryTotals.stream()
                .filter(row -> !row.getCategoryName().equals("Income")).toList();
        expenseGrid.setItems(expenseData);
    }

    private Component getIncomeAndExpenseGridContent() {
        HorizontalLayout content = new HorizontalLayout(incomeGrid, expenseGrid);
        content.setWidthFull();
        return content;
    }

    private Component getTransactionStats() {
        Long userId = securityService.getCurrentUserId(service);

        Span totalTransactions = new Span(service.countTransactionsByUser(userId) + " transactions");
        Span incomes = new Span("$" + service.sumAllTransactionsByType(userId, Type.INCOME) + " in income");
        Span expenses = new Span("$" + service.sumAllTransactionsByType(userId, Type.EXPENSE) + " in expenses");

        totalTransactions.addClassNames("text-xl", "mt-m");
        incomes.addClassNames("text-xl", "mt-m");
        expenses.addClassNames("text-xl", "mt-m");

        return new VerticalLayout(totalTransactions, incomes, expenses);
    }

    private Component getTransactionsChart() {
        Long userId = service.findUserByEmail(securityService.getAuthenticatedUser().getUsername()).getId();

        Chart columnChart = new Chart(ChartType.COLUMN);
        DataSeries dataSeries = new DataSeries();
        service.findAllTypes().forEach(type -> {
            dataSeries.add(new DataSeriesItem(type.name(), service.sumAllTransactionsByType(userId, type)));
        });

        Configuration config = columnChart.getConfiguration();
        config.getChart().setStyledMode(true);


        config.setSeries(dataSeries);

        XAxis xAxis = config.getxAxis();
        xAxis.setTitle("Transaction Type");

        xAxis.setType(AxisType.CATEGORY);

        YAxis yAxis = config.getyAxis();
        yAxis.setTitle("Amount");

        // Chart chart = new Chart(ChartType.PIE);
        //
        // DataSeries dataSeries = new DataSeries();
        // service.findAllTypes().forEach(type -> {
        //     dataSeries.add(new DataSeriesItem(type.name(), service.sumAllTransactionsByType(userId, type)));
        // });
        //
        // chart.getConfiguration().setSeries(dataSeries);
        return columnChart;
    }
}
