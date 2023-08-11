package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.entity.report.CategoryTotal;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;

@Route(value = "income-vs-expense", layout = MainLayout.class)
@PageTitle("I & E | Prospero")
@PermitAll
@CssImport(value = "./themes/prospero/prospero-charts.css", themeFor = "vaadin-chart")
@CssImport(value = "./themes/prospero/views/i-v-e.css")
public class IncomeVsExpenseView extends VerticalLayout {

    private SecurityService securityService;
    private PfmService service;

    private Long userId;

    Grid<CategoryTotal> incomeGrid = new Grid<>(CategoryTotal.class);
    Grid<CategoryTotal> expenseGrid = new Grid<>(CategoryTotal.class);

    Component filterDiv;

    // private final DatePicker startDate = new DatePicker("Transaction Date");
    private final DatePicker startDate = new DatePicker("Filter Range");
    private final DatePicker endDate = new DatePicker();

    // column chart
    private Chart columnChart;
    private Configuration columnChartConfig;
    private DataSeries columnChartData;

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

        filterDiv = createFilterLayout();

        add(
                filterDiv,
                getTransactionsChart(),
                getIncomeAndExpenseGridContent()
        );

        updateGridData();
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


    private void updateGridData() {
        Date start = startDate.getValue() != null ? Date.valueOf(startDate.getValue()) : null;
        Date end = endDate.getValue() != null ? Date.valueOf(endDate.getValue()) : null;

        List<Object[]> incomeResults = null;
        List<Object[]> expenseResults = null;

        if (start == null || end == null) {
            System.out.println("if called");
            incomeResults = service.sumTransactionByCategory(userId, Type.INCOME);
            System.out.println("incomeResults: " + incomeResults.size());
            expenseResults = service.sumTransactionByCategory(userId, Type.EXPENSE);
            System.out.println("expenseResults: " + expenseResults.size());
        } else {
            System.out.println("else called");
            incomeResults = service.sumTransactionsInDateRangeByCategory(userId, Type.INCOME, start, end);
            expenseResults = service.sumTransactionsInDateRangeByCategory(userId, Type.EXPENSE, start, end);
        }

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

    private void updateChartData() {
        Long userId = service.findUserByEmail(securityService.getAuthenticatedUser().getUsername()).getId();

        columnChartConfig = columnChart.getConfiguration();
        columnChartData = new DataSeries();
        columnChartData.setName("Transactions");

        Date start = startDate.getValue() != null ? Date.valueOf(startDate.getValue()) : null;
        Date end = endDate.getValue() != null ? Date.valueOf(endDate.getValue()) : null;

        DataSeriesItem incomeItem = null;
        DataSeriesItem expenseItem = null;

        if (start == null || end == null){
            incomeItem = new DataSeriesItem("Income", service.sumAllTransactionsByType(userId, Type.INCOME));
            expenseItem = new DataSeriesItem("Expenses", service.sumAllTransactionsByType(userId, Type.EXPENSE));
        } else {
            incomeItem = new DataSeriesItem("Income", service.sumAllTransactionsByTypeInDateRange(userId, Type.INCOME, start, end));
            expenseItem = new DataSeriesItem("Expenses", service.sumAllTransactionsByTypeInDateRange(userId, Type.EXPENSE, start, end));
        }

        incomeItem.setClassName("income-column-bar");
        expenseItem.setClassName("expense-column-bar");

        columnChartData.add(incomeItem);
        columnChartData.add(expenseItem);

        DataLabels totalLabel = new DataLabels(true);
        totalLabel.setShape(Shape.CALLOUT);
        totalLabel.setY(-10);
        totalLabel.setFormatter("function() { return '$' + Highcharts.numberFormat(this.point.y, 0, '.', ',') }");
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
        columnChartConfig.setTooltip(tooltip);

        columnChartConfig.getChart().setStyledMode(true);
        columnChartConfig.getLegend().setEnabled(false);
        columnChartConfig.setSeries(columnChartData);
        columnChartConfig.getxAxis().setClassName("huge-axis");

        XAxis xAxis = columnChartConfig.getxAxis();
        xAxis.setType(AxisType.CATEGORY);

        YAxis yAxis = columnChartConfig.getyAxis();
        yAxis.setTitle("Amount");

        columnChart.addClassName("totals-chart");
        columnChart.drawChart();
        System.out.println("reached end of updateChartData()");
    }

    private Component getIncomeAndExpenseGridContent() {
        HorizontalLayout content = new HorizontalLayout(incomeGrid, expenseGrid);
        content.setClassName("grids-layout");
        content.setWidthFull();
        return content;
    }

    private Component getTransactionsChart() {
        Long userId = service.findUserByEmail(securityService.getAuthenticatedUser().getUsername()).getId();

        columnChart = new Chart(ChartType.COLUMN);
        columnChartConfig = columnChart.getConfiguration();
        columnChartData = new DataSeries();
        columnChartData.setName("Transactions");

        Date start = startDate.getValue() != null ? Date.valueOf(startDate.getValue()) : null;
        Date end = endDate.getValue() != null ? Date.valueOf(endDate.getValue()) : null;

        DataSeriesItem incomeItem = null;
        DataSeriesItem expenseItem = null;

        if (start == null || end == null){
            incomeItem = new DataSeriesItem("Income", service.sumAllTransactionsByType(userId, Type.INCOME));
            expenseItem = new DataSeriesItem("Expenses", service.sumAllTransactionsByType(userId, Type.EXPENSE));
        } else {
            incomeItem = new DataSeriesItem("Income", service.sumAllTransactionsByTypeInDateRange(userId, Type.INCOME, start, end));
            expenseItem = new DataSeriesItem("Expenses", service.sumAllTransactionsByTypeInDateRange(userId, Type.EXPENSE, start, end));
        }

        incomeItem.setClassName("income-column-bar");
        expenseItem.setClassName("expense-column-bar");

        columnChartData.add(incomeItem);
        columnChartData.add(expenseItem);

        DataLabels totalLabel = new DataLabels(true);
        totalLabel.setShape(Shape.CALLOUT);
        totalLabel.setY(-10);
        totalLabel.setFormatter("function() { return '$' + Highcharts.numberFormat(this.point.y, 0, '.', ',') }");
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
        columnChartConfig.setTooltip(tooltip);

        columnChartConfig.getChart().setStyledMode(true);
        columnChartConfig.getLegend().setEnabled(false);
        columnChartConfig.setSeries(columnChartData);
        columnChartConfig.getxAxis().setClassName("huge-axis");

        XAxis xAxis = columnChartConfig.getxAxis();
        xAxis.setType(AxisType.CATEGORY);

        YAxis yAxis = columnChartConfig.getyAxis();
        yAxis.setTitle("Amount");

        columnChart.addClassName("totals-chart");

        return columnChart;
    }

    private Component createFilterLayout() {
        startDate.addValueChangeListener(e -> {
            if (startDate.getValue() != null) {
                endDate.setMin(startDate.getValue().plusDays(1));
            }
        });

        Div filterDiv = new Div();
        filterDiv.setWidthFull();
        filterDiv.addClassName("filter-layout");
        filterDiv.addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                LumoUtility.BoxSizing.BORDER);

        // Action buttons
        Button resetBtn = new Button("Reset");
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> resetFilterFields());

        Button filterBtn = new Button("Filter");
        filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterBtn.addClickListener(e -> updateGridAndChart());

        HorizontalLayout actions = new HorizontalLayout(filterBtn, resetBtn);
        actions.addClassName(LumoUtility.Gap.SMALL);
        actions.addClassName("actions");

        filterDiv.add(createDateRangeFilter(), actions);
        return filterDiv;
    }

    private void updateGridAndChart() {
        updateGridData();
        updateChartData();


    }

    private void resetFilterFields() {
        startDate.clear();
        endDate.clear();
    }

    private Component createDateRangeFilter() {
        startDate.setPlaceholder("From");
        endDate.setPlaceholder("To");

        FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" - "), endDate);
        dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
        dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

        return dateRangeComponent;
    }
}