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
@CssImport(value = "./styles/i-v-e-chart.css", themeFor = "vaadin-chart")
@CssImport(value = "./styles/i-v-e-grids.css")
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
                getTransactionsChart(),
                getIncomeAndExpenseGridContent()
        );

        updateGrids();
    }

    private void configureGrids() {
        // INCOME GRID
        incomeGrid.removeAllColumns();
        incomeGrid.setClassName("income-grid");

        incomeGrid.addColumn(CategoryTotal::getCategoryName).setHeader(
                        new Html("<div class='grid-header'>Category</div>"))
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getCategoryName));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        NumberRenderer<CategoryTotal> amountRenderer = new NumberRenderer<>(CategoryTotal::getTotalAmount, currencyFormat);

        incomeGrid.addColumn(amountRenderer).setHeader(
                        new Html("<div class='grid-header'>Amount</div>"))
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getTotalAmount));

        incomeGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        incomeGrid.asSingleSelect();

        // EXPENSE GRID
        expenseGrid.removeAllColumns();
        expenseGrid.setClassName("expense-grid");

        expenseGrid.addColumn(CategoryTotal::getCategoryName).setHeader(
                        new Html("<div class='grid-header'>Category</div>"))
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getCategoryName));

        expenseGrid.addColumn(amountRenderer).setHeader(
                        new Html("<div class='grid-header'>Amount</div>"))
                .setSortable(true)
                .setComparator(Comparator.comparing(CategoryTotal::getTotalAmount));

        expenseGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        expenseGrid.asSingleSelect();
    }


    private void updateGrids() {
        List<Object[]> incomeResults = service.sumTransactionByCategory(userId, Type.INCOME);
        List<Object[]> expenseResults = service.sumTransactionByCategory(userId, Type.EXPENSE);

        // convert the result sets into a lists of CategoryTotal objects
        List<CategoryTotal> incomeData = incomeResults
                .stream()
                .map(row -> new CategoryTotal((String) row[0], (BigDecimal) row[1])).toList();

        List<CategoryTotal> expenseData = expenseResults
                .stream()
                .map(row -> new CategoryTotal((String) row[0], (BigDecimal) row[1])).toList();

        incomeGrid.setItems(incomeData);
        expenseGrid.setItems(expenseData);
    }

    private Component getIncomeAndExpenseGridContent() {
        HorizontalLayout content = new HorizontalLayout(incomeGrid, expenseGrid);
        content.setClassName("grids-layout");
        content.setWidthFull();
        return content;
    }

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

        return columnChart;
    }
}