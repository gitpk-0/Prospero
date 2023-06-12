package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.converter.BigDecimalToDoubleConverter;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.entity.report.CategoryTotal;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Prospero")
@PermitAll
@CssImport(value = "./themes/prospero/prospero-charts.css", themeFor = "vaadin-chart")
@CssImport(value = "./themes/prospero/views/dashboard-view.css")
public class DashboardView extends Main {

    private final SecurityService securityService;
    private final PfmService service;

    BigDecimalToDoubleConverter amountConverter = new BigDecimalToDoubleConverter();
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    // board rows
    private Row highlightsRow;
    private Row areasplieChartRow;
    private Row pieChartsRow;

    // area spline chart
    private Select<Integer> yearSelect;
    private Chart yearViewChart;
    private Configuration yearViewChartConfig;
    private ListSeries incomeAreaSeries;
    private ListSeries expenseSeries;
    private List<Series> yearViewSeriesList;

    // income pie chart
    private Chart incomePieChart;
    private Configuration incomeChartConfig;
    private DataSeries incomePieSeries;
    private HorizontalLayout incomeChartHeader;

    // expense pie chart
    private Chart expensePieChart;
    private Configuration expenseChartConfig;
    private DataSeries expensePieSeries;
    private HorizontalLayout expenseChartHeader;

    public DashboardView(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        addClassName("dashboard-view");

        Long userId = securityService.getCurrentUserId(service);
        String firstName = service.findUserById(userId).getFirstName();
        Integer currentYear = LocalDate.now().getYear();

        Board board = new Board();
        highlightsRow = new Row(createWelcomeHighlight("Welcome " + firstName, userId),
                createHighlight("Income", getIncomeTotal(userId, currentYear), currentYear),
                createHighlight("Expenses", getExpensesTotal(userId, currentYear), currentYear),
                createHighlight("Transactions", getTransactionCount(userId, currentYear), currentYear));

        areasplieChartRow = new Row(createYearViewLayout(userId, currentYear));
        pieChartsRow = new Row(createIncomePieChart(userId, currentYear), createExpensePieChart(userId, currentYear));

        board.addRow(highlightsRow);
        board.addRow(areasplieChartRow);
        board.addRow(pieChartsRow);

        add(board);

        yearSelect.addValueChangeListener(e -> {
            Integer selectedYear = e.getValue(); // selected year
            updateDashboard(userId, firstName, selectedYear);
        });
    }

    private Component createWelcomeHighlight(String welcome, Long userId) {
        VaadinIcon icon = VaadinIcon.MONEY;
        String prefix = " ";
        String theme = "badge";

        Double accountBalance = getAccountBalance(userId);

        if (accountBalance > 0) {
            theme += " success";
        } else if (accountBalance < 0) {
            theme += " error";
        }

        H2 welcomeText = new H2(welcome);
        welcomeText.addClassNames(LumoUtility.FontWeight.NORMAL, LumoUtility.Margin.NONE,
                LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);

        Span accountBalanceSpan = new Span("Account Balance");
        accountBalanceSpan.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);

        Icon i = icon.create();
        i.addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Padding.XSMALL);

        String balance = currencyFormat.format(accountBalance);
        Span badge = new Span(i, new Span(prefix + balance));
        badge.getElement().getThemeList().add(theme);
        badge.addClassNames(LumoUtility.FontSize.LARGE);

        VerticalLayout layout = new VerticalLayout(welcomeText, accountBalanceSpan, badge);
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    private Component createHighlight(String title, String value, Integer year) {

        H2 titleText = new H2(title);
        titleText.addClassNames(LumoUtility.FontWeight.NORMAL, LumoUtility.Margin.NONE,
                LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.XXXLARGE);

        String theme = "badge";
        Span badge = new Span(year.toString());
        badge.getElement().getThemeList().add(theme);

        VerticalLayout layout = new VerticalLayout(titleText, valueSpan, badge);
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    private HorizontalLayout createHeader(String title, String subtitle) {
        H2 h2 = new H2(title);
        h2.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE);

        Span span = new Span(subtitle);
        span.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        span.getElement().getThemeList().add("badge");

        VerticalLayout column = new VerticalLayout(h2, span);
        column.setPadding(false);
        column.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(column);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setSpacing(false);
        header.setWidthFull();
        return header;
    }

    private Component createYearViewLayout(Long userId, Integer year) {
        HorizontalLayout yearViewHeader = createHeader("Year View", "Transaction Totals by Month");

        yearSelect = new Select<>();
        yearSelect.setWidth("100px");
        List<Integer> distinctYears = service.findDistinctYears(userId);

        VerticalLayout layout = new VerticalLayout();
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getElement().getThemeList().add("spacing-l");

        if (distinctYears.isEmpty()) {
            yearSelect.setEnabled(false); // no transactions exist yet

            layout.add(new H3("No transaction data - To view dashboard charts you need to add transactions"));
        } else {
            List<Integer> sortedYears = distinctYears.stream()
                    .sorted(Collections.reverseOrder()).toList();

            yearSelect.setItems(sortedYears);
            Integer currentYear = LocalDate.now().getYear();

            if (sortedYears.contains(currentYear)) {
                yearSelect.setValue(currentYear);
            }

            yearViewHeader.add(yearSelect);

            yearViewChart = new Chart(ChartType.AREASPLINE);
            yearViewChart.addClassName("year-view-chart");
            yearViewChartConfig = yearViewChart.getConfiguration();
            yearViewChartConfig.getChart().setStyledMode(true);

            Tooltip yearViewTooltip = new Tooltip();
            yearViewTooltip.setFormatter("function() {" +
                    "    return '<br/><b>' + '$' + Highcharts.numberFormat(this.point.y, 2, '.', ',') + '</b><br/>'}");
            yearViewTooltip.setEnabled(true);
            yearViewChartConfig.setTooltip(yearViewTooltip);

            XAxis xAxis = new XAxis();
            xAxis.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
            yearViewChartConfig.addxAxis(xAxis);

            yearViewChartConfig.getyAxis().setTitle("Totals");

            incomeAreaSeries = generateIncomeYearChartData(userId, year);
            expenseSeries = generateExpenseYearChartData(userId, year);

            yearViewSeriesList = new ArrayList<>();
            yearViewSeriesList.add(incomeAreaSeries);
            yearViewSeriesList.add(expenseSeries);

            yearViewChartConfig.setSeries(yearViewSeriesList);

            layout.add(yearViewHeader, yearViewChart);
        }
        return layout;
    }

    private Component createIncomePieChart(Long userId, Integer year) {

        VerticalLayout layout = new VerticalLayout();
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getElement().getThemeList().add("spacing-l");

        List<Integer> distinctYears = service.findDistinctYears(userId);

        if (distinctYears.isEmpty()) {
            layout.add(new H3(""));
        } else {
            incomeChartHeader = createHeader("Income", year.toString());

            incomePieChart = new Chart(ChartType.PIE);
            incomeChartConfig = incomePieChart.getConfiguration();
            incomeChartConfig.getChart().setStyledMode(true);
            incomePieChart.setThemeName("gradient");

            incomePieSeries = generateIncomePieChartData(userId, year);
            incomeChartConfig.setSeries(incomePieSeries);

            Tooltip incomeTooltip = new Tooltip();
            incomeTooltip.setFormatter("function() {" +
                    "    return '<br/><b>' + this.point.name + '</b><br/>' +" +
                    "        'Total: $' + Highcharts.numberFormat(this.point.y, 2, '.', ',') + '<br/><br/>'}");
            incomeTooltip.setEnabled(true);
            incomeChartConfig.setTooltip(incomeTooltip);
            layout.add(incomeChartHeader, incomePieChart);
        }
        return layout;
    }

    private Component createExpensePieChart(Long userId, Integer year) {

        VerticalLayout layout = new VerticalLayout();
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getElement().getThemeList().add("spacing-l");

        List<Integer> distinctYears = service.findDistinctYears(userId);

        if (distinctYears.isEmpty()) {
            layout.add(new H3(""));
        } else {
            expenseChartHeader = createHeader("Expense", year.toString());

            expensePieChart = new Chart(ChartType.PIE);
            expenseChartConfig = expensePieChart.getConfiguration();
            expenseChartConfig.getChart().setStyledMode(true);
            expensePieChart.setThemeName("classic");

            expensePieSeries = generateExpensePieChartData(userId,year);
            expenseChartConfig.setSeries(expensePieSeries);

            Tooltip expenseTooltip = new Tooltip();
            expenseTooltip.setFormatter("function() {" +
                    "    return '<br/><b>' + this.point.name + '</b><br/>' +" +
                    "        'Total: $' + Highcharts.numberFormat(this.point.y, 2, '.', ',') + '<br/><br/>'}");
            expenseTooltip.setEnabled(true);
            expenseChartConfig.setTooltip(expenseTooltip);
            layout.add(expenseChartHeader, expensePieChart);
        }
        return layout;
    }

    private void updateDashboard(Long userId, String firstName, Integer selectedYear) {
        // highlights row
        highlightsRow.removeAll();
        Row newHighlightsRow = new Row(createWelcomeHighlight("Welcome " + firstName, userId),
                createHighlight("Income", getIncomeTotal(userId, selectedYear), selectedYear),
                createHighlight("Expenses", getExpensesTotal(userId, selectedYear), selectedYear),
                createHighlight("Transactions", getTransactionCount(userId, selectedYear), selectedYear));
        highlightsRow.replace(highlightsRow, newHighlightsRow);

        // Year view chart
        incomeAreaSeries = generateIncomeYearChartData(userId, selectedYear);
        expenseSeries = generateExpenseYearChartData(userId, selectedYear);

        yearViewSeriesList = new ArrayList<>();
        yearViewSeriesList.add(incomeAreaSeries);
        yearViewSeriesList.add(expenseSeries);

        yearViewChartConfig.setSeries(yearViewSeriesList);
        yearViewChart.drawChart();

        // Pie charts row
        pieChartsRow.removeAll();
        Row newPieChartsRow =
                new Row(createIncomePieChart(userId, selectedYear), createExpensePieChart(userId, selectedYear));
        pieChartsRow.replace(pieChartsRow, newPieChartsRow);
    }

    private ListSeries generateIncomeYearChartData(Long userId, Integer year) {
        PlotOptionsAreaspline plotOptionsIncome = new PlotOptionsAreaspline();
        plotOptionsIncome.setColorIndex(4);  //#90ed7d
        plotOptionsIncome.setPointPlacement(PointPlacement.ON);
        plotOptionsIncome.setMarker(new Marker(false));

        ListSeries incomeSeries = new ListSeries("Income");
        List<BigDecimal> incomeData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            incomeData.add(service.getSumTransactionsByMonthAndYearAndType(
                    userId, year, month, Type.INCOME));
        }
        incomeData.forEach(incomeSeries::addData);
        incomeSeries.setPlotOptions(plotOptionsIncome);
        return incomeSeries;
    }

    private ListSeries generateExpenseYearChartData(Long userId, Integer year) {
        PlotOptionsAreaspline plotOptionsExpense = new PlotOptionsAreaspline();
        plotOptionsExpense.setColorIndex(2);
        plotOptionsExpense.setPointPlacement(PointPlacement.ON);
        plotOptionsExpense.setMarker(new Marker(false));

        ListSeries expenseSeries = new ListSeries("Expense");
        List<BigDecimal> expenseData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            expenseData.add(service.getSumTransactionsByMonthAndYearAndType(
                    userId, year, month, Type.EXPENSE));
        }
        expenseData.forEach(expenseSeries::addData);
        expenseSeries.setPlotOptions(plotOptionsExpense);
        return expenseSeries;
    }

    private DataSeries generateIncomePieChartData(Long userId, Integer year) {
        PlotOptionsPie incomePlotOptions = new PlotOptionsPie();
        incomeChartConfig.setPlotOptions(incomePlotOptions);

        DataLabels incomeDataLabels = new DataLabels();
        incomeDataLabels.setEnabled(false);
        incomePlotOptions.setDataLabels(incomeDataLabels);

        incomePieSeries = new DataSeries();

        List<Object[]> incomeResults = service.sumTransactionByCategoryAndYear(userId, Type.INCOME, year);

        // convert the result sets into a lists of CategoryTotal objects
        List<CategoryTotal> incomeData = incomeResults
                .stream()
                .map(row -> new CategoryTotal((String) row[0], (BigDecimal) row[1])).toList();

        incomeData.forEach(item -> {
            incomePieSeries.add(new DataSeriesItem(item.getCategoryName(), item.getTotalAmount()));
        });

        return incomePieSeries;
    }

    private DataSeries generateExpensePieChartData(Long userId, Integer year) {
        PlotOptionsPie expensePlotOptions = new PlotOptionsPie();
        expenseChartConfig.setPlotOptions(expensePlotOptions);

        DataLabels expensePlotsOptions = new DataLabels();
        expensePlotsOptions.setEnabled(false);
        expensePlotOptions.setDataLabels(expensePlotsOptions);

        DataSeries expensePieSeries = new DataSeries();

        List<Object[]> expenseResults = service.sumTransactionByCategoryAndYear(userId, Type.EXPENSE, year);

        // convert the result sets into a lists of CategoryTotal objects
        List<CategoryTotal> expenseData = expenseResults
                .stream()
                .map(row -> new CategoryTotal((String) row[0], (BigDecimal) row[1])).toList();

        expenseData.forEach(item -> {
            expensePieSeries.add(new DataSeriesItem(item.getCategoryName(), item.getTotalAmount()));
        });

        return expensePieSeries;
    }

    private String getTransactionCount(Long userId, Integer year) {
        return String.valueOf(service.getTransactionCountByYear(userId, year));
    }

    private String getIncomeTotal(Long userId, Integer year) {
        return currencyFormat.format(service.getSumIncomeTransactionsByYear(userId, year));
    }

    private String getExpensesTotal(Long userId, Integer year) {
        return currencyFormat.format(service.getSumExpenseTransactionsByYear(userId, year));
    }

    private Double getAccountBalance(Long userId) {
        BigDecimal income = service.getSumIncomeTransactions(userId);
        BigDecimal expenses = service.getSumExpenseTransactions(userId);
        BigDecimal accountBalanceBd = income.subtract(expenses);
        return amountConverter.convertToPresentation(accountBalanceBd, new ValueContext());
    }
}
