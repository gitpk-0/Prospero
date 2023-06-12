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
    private ListSeries incomeSeries;
    private ListSeries expenseSeries;
    private List<Series> yearViewSeriesList;

    // income pie chart
    private Chart incomePieChart;
    private Configuration incomeChartConfig;

    // expense pie chart
    private Chart expensePieChart;
    private Configuration expenseChartConfig;

    public DashboardView(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        addClassName("dashboard-view");

        Long userId = securityService.getCurrentUserId(service);
        String firstName = service.findUserById(userId).getFirstName();
        String currentMonth = LocalDate.now().getMonth().toString();
        Integer currentYear = LocalDate.now().getYear();


        Board board = new Board();
        highlightsRow = new Row(createWelcomeHighlight("Welcome " + firstName, userId),
                createHighlight("Income", getIncomeTotal(userId), currentMonth),
                createHighlight("Expenses", getExpensesTotal(userId), currentMonth),
                createHighlight("Transactions", getTransactionCount(userId), currentMonth));

        areasplieChartRow = new Row(createYearViewLayout(userId, currentYear));
        pieChartsRow = new Row(createIncomePieChart(userId), createExpensePieChart(userId));

        board.addRow(highlightsRow);
        board.addRow(areasplieChartRow);
        board.addRow(pieChartsRow);

        add(board);

        yearSelect.addValueChangeListener(e -> {
            Integer selectedYear = e.getValue();
            incomeSeries = updateYearViewChartIncome(userId, selectedYear);
            expenseSeries = updateYearViewChartExpense(userId, selectedYear);

            yearViewSeriesList = new ArrayList<>();
            yearViewSeriesList.add(incomeSeries);
            yearViewSeriesList.add(expenseSeries);

            yearViewChartConfig.setSeries(yearViewSeriesList);

            yearViewChart.drawChart();
        });
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

            layout.add(new H3("No transaction data"));
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

            XAxis xAxis = new XAxis();
            xAxis.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
            yearViewChartConfig.addxAxis(xAxis);

            yearViewChartConfig.getyAxis().setTitle("Totals");

            incomeSeries = updateYearViewChartIncome(userId, year);
            expenseSeries = updateYearViewChartExpense(userId, year);

            yearViewSeriesList = new ArrayList<>();
            yearViewSeriesList.add(incomeSeries);
            yearViewSeriesList.add(expenseSeries);

            yearViewChartConfig.setSeries(yearViewSeriesList);

            layout.add(yearViewHeader, yearViewChart);
        }
        return layout;
    }

    private ListSeries updateYearViewChartIncome(Long userId, Integer year) {
        PlotOptionsAreaspline plotOptionsIncome = new PlotOptionsAreaspline();
        plotOptionsIncome.setColorIndex(4);  //#90ed7d
        plotOptionsIncome.setPointPlacement(PointPlacement.ON);
        plotOptionsIncome.setMarker(new Marker(false));

        ListSeries incomeSeries = new ListSeries("Income");
        List<Integer> incomeData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            incomeData.add(service.getSumTransactionsByMonthAndYearAndType(
                    userId, year, month, Type.INCOME));
        }
        incomeData.forEach(incomeSeries::addData);
        incomeSeries.setPlotOptions(plotOptionsIncome);
        return incomeSeries;
    }

    private ListSeries updateYearViewChartExpense(Long userId, Integer year) {
        PlotOptionsAreaspline plotOptionsExpense = new PlotOptionsAreaspline();
        plotOptionsExpense.setColorIndex(2);
        plotOptionsExpense.setPointPlacement(PointPlacement.ON);
        plotOptionsExpense.setMarker(new Marker(false));

        ListSeries expenseSeries = new ListSeries("Expense");
        List<Integer> expenseData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            expenseData.add(service.getSumTransactionsByMonthAndYearAndType(
                    userId, year, month, Type.EXPENSE));
        }
        expenseData.forEach(expenseSeries::addData);
        expenseSeries.setPlotOptions(plotOptionsExpense);
        return expenseSeries;
    }

    private HorizontalLayout createHeader(String title, String subtitle) {
        H2 h2 = new H2(title);
        h2.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE);

        Span span = new Span(subtitle);
        span.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        VerticalLayout column = new VerticalLayout(h2, span);
        column.setPadding(false);
        column.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(column);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setSpacing(false);
        header.setWidthFull();
        return header;
    }

    private Component createIncomePieChart(Long userId) {
        HorizontalLayout incomeChartHeader = createHeader("Income", "All Time");

        incomePieChart = new Chart(ChartType.PIE);
        incomeChartConfig = incomePieChart.getConfiguration();
        incomeChartConfig.getChart().setStyledMode(true);
        incomePieChart.setThemeName("gradient");

        PlotOptionsPie incomePlotOptions = new PlotOptionsPie();
        incomeChartConfig.setPlotOptions(incomePlotOptions);

        DataLabels incomeDataLabels = new DataLabels();
        incomeDataLabels.setEnabled(false);
        incomePlotOptions.setDataLabels(incomeDataLabels);

        DataSeries incomeSeries = new DataSeries();

        List<Object[]> incomeResults = service.sumTransactionByCategory(userId, Type.INCOME);

        // convert the result sets into a lists of CategoryTotal objects
        List<CategoryTotal> incomeData = incomeResults
                .stream()
                .map(row -> new CategoryTotal((String) row[0], (BigDecimal) row[1])).toList();

        incomeData.forEach(item -> {
            incomeSeries.add(new DataSeriesItem(item.getCategoryName(), item.getTotalAmount()));
        });

        Tooltip incomeTooltip = new Tooltip();
        incomeTooltip.setFormatter("function() {" +
                "    return '<br/><b>' + this.point.name + '</b><br/>' +" +
                "        'Total: $' + Highcharts.numberFormat(this.point.y, 2, '.', ',') + '<br/><br/>'}");
        incomeTooltip.setEnabled(true);
        incomeChartConfig.setTooltip(incomeTooltip);


        incomeChartConfig.setSeries(incomeSeries);

        VerticalLayout layout = new VerticalLayout(incomeChartHeader, incomePieChart);
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getElement().getThemeList().add("spacing-l");
        return layout;
    }

    private Component createExpensePieChart(Long userId) {
        HorizontalLayout expenseChartHeader = createHeader("Expense", "All Time");

        expensePieChart = new Chart(ChartType.PIE);
        expenseChartConfig = expensePieChart.getConfiguration();
        expenseChartConfig.getChart().setStyledMode(true);
        expensePieChart.setThemeName("classic");

        PlotOptionsPie expensePlotOptions = new PlotOptionsPie();
        expenseChartConfig.setPlotOptions(expensePlotOptions);

        DataLabels expensePlotsOptions = new DataLabels();
        expensePlotsOptions.setEnabled(false);
        expensePlotOptions.setDataLabels(expensePlotsOptions);

        DataSeries expenseSeries = new DataSeries();

        List<Object[]> expenseResults = service.sumTransactionByCategory(userId, Type.EXPENSE);

        // convert the result sets into a lists of CategoryTotal objects
        List<CategoryTotal> expenseData = expenseResults
                .stream()
                .map(row -> new CategoryTotal((String) row[0], (BigDecimal) row[1])).toList();

        expenseData.forEach(item -> {
            expenseSeries.add(new DataSeriesItem(item.getCategoryName(), item.getTotalAmount()));
        });
        expenseChartConfig.addSeries(expenseSeries);

        Tooltip expenseTooltip = new Tooltip();
        expenseTooltip.setFormatter("function() {" +
                "    return '<br/><b>' + this.point.name + '</b><br/>' +" +
                "        'Total: $' + Highcharts.numberFormat(this.point.y, 2, '.', ',') + '<br/><br/>'}");
        expenseTooltip.setEnabled(true);
        expenseChartConfig.setTooltip(expenseTooltip);

        VerticalLayout layout = new VerticalLayout(expenseChartHeader, expensePieChart);
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getElement().getThemeList().add("spacing-l");

        return layout;
    }


    private String getTransactionCount(Long userId) {
        return String.valueOf(service.getTransactionCount(userId));
    }

    private String getExpensesTotal(Long userId) {
        return currencyFormat.format(service.getSumExpenseTransactions(userId));
    }

    private String getIncomeTotal(Long userId) {
        return currencyFormat.format(service.getSumIncomeTransactions(userId));
    }

    private Double getAccountBalance(Long userId) {
        BigDecimal income = service.getSumIncomeTransactions(userId);
        BigDecimal expenses = service.getSumExpenseTransactions(userId);
        BigDecimal accountBalanceBd = income.subtract(expenses);
        return amountConverter.convertToPresentation(accountBalanceBd, new ValueContext());
    }

    private Component createHighlight(String title, String value, String month) {

        H2 titleText = new H2(title);
        titleText.addClassNames(LumoUtility.FontWeight.NORMAL, LumoUtility.Margin.NONE,
                LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.XXXLARGE);

        String theme = "badge success";
        Span badge = new Span("ALL");
        badge.getElement().getThemeList().add(theme);

        VerticalLayout layout = new VerticalLayout(titleText, valueSpan, badge);
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
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
}
