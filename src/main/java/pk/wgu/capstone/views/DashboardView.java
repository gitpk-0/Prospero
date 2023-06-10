package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
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

    public DashboardView(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        addClassName("dashboard-view");

        Long userId = securityService.getCurrentUserId(service);
        String firstName = service.findUserById(userId).getFirstName();
        String currentMonth = LocalDate.now().getMonth().toString();

        Board board = new Board();
        board.addRow(
                createWelcomeHighlight("Welcome " + firstName, userId),
                createHighlight("Income", getIncomeTotal(userId), currentMonth),
                createHighlight("Expenses", getExpensesTotal(userId), currentMonth),
                createHighlight("Transactions", getTransactionCount(userId), currentMonth));
        board.addRow(createYearViewChart(userId));
        board.addRow(createIncomePieChart(), createExpensePieChart());
        add(board);
    }

    private String getTransactionCount(Long userId) {
        return String.valueOf(service.getTransactionCount(userId));
    }

    private String getExpensesTotal(Long userId) {
        System.out.println("expensesTOTAL: " + service.getSumExpenseTransactions(userId));
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

    private Component createYearViewChart(Long userId) {
        HorizontalLayout header = createHeader("Year View", "Transaction Totals by Month");

        Select<Integer> yearSelect = new Select<>();
        yearSelect.setWidth("100px");
        List<Integer> distinctYears = service.findDistinctYears(userId);

        if (distinctYears.isEmpty()) {
            yearSelect.setEnabled(false); // no transactions exist yet
        } else {
            List<Integer> sortedYears = distinctYears.stream()
                    .sorted(Collections.reverseOrder()).toList();

            yearSelect.setItems(sortedYears);
            Integer currentYear = LocalDate.now().getYear();

            if (sortedYears.contains(currentYear)) {
                yearSelect.setValue(currentYear);
            }
        }
        header.add(yearSelect);
        yearSelect.addValueChangeListener(e -> {
            // updateYearViewChart(userId, yearSelect.getValue());
            //  System.out.println("Year selection changed");
        });

        Chart yearViewChart = new Chart(ChartType.AREASPLINE);
        yearViewChart.addClassName("year-view-chart");
        Configuration config = yearViewChart.getConfiguration();
        config.getChart().setStyledMode(true);

        XAxis xAxis = new XAxis();
        xAxis.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
        config.addxAxis(xAxis);

        config.getyAxis().setTitle("Totals");

        PlotOptionsAreaspline plotOptionsIncome = new PlotOptionsAreaspline();
        plotOptionsIncome.setColorIndex(4);  //#90ed7d
        plotOptionsIncome.setPointPlacement(PointPlacement.ON);
        plotOptionsIncome.setMarker(new Marker(false));

        if (!distinctYears.isEmpty()) {
            ListSeries incomeSeries = updateYearViewChartIncome(userId, yearSelect.getValue());
            incomeSeries.setPlotOptions(plotOptionsIncome);
            config.addSeries(incomeSeries);
        }

        // ListSeries income = new ListSeries("Income", 189, 191, 291, 396, 501, 403, 609, 712, 729, 942, 1044, 1247);


        PlotOptionsAreaspline plotOptionsExpense = new PlotOptionsAreaspline();
        plotOptionsExpense.setColorIndex(2);
        plotOptionsExpense.setPointPlacement(PointPlacement.ON);
        plotOptionsExpense.setMarker(new Marker(false));

        ListSeries expense = new ListSeries("Expense", 138, 246, 248, 348, 352, 353, 463, 573, 778, 779, 885, 887);
        expense.setPlotOptions(plotOptionsExpense);
        config.addSeries(expense);


        VerticalLayout layout = new VerticalLayout(header, yearViewChart);
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getElement().getThemeList().add("spacing-l");
        return layout;
    }

    private ListSeries updateYearViewChartIncome(Long userId, Integer year) {
        ListSeries incomeSeries = new ListSeries("Income");
        List<Integer> incomeData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            incomeData.add(service.getSumTransactionsByMonthAndYearAndType(
                    userId, year, month, Type.INCOME));
        }
        incomeData.forEach(incomeSeries::addData);
        return incomeSeries;
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

    private Component createIncomePieChart() {
        HorizontalLayout header = createHeader("Income", "All Time");

        Chart incomePieChart = new Chart(ChartType.PIE);
        Configuration config = incomePieChart.getConfiguration();
        config.getChart().setStyledMode(true);
        incomePieChart.setThemeName("gradient");

        DataSeries incomeSeries = new DataSeries();
        incomeSeries.add(new DataSeriesItem("Salary", 12.5));
        incomeSeries.add(new DataSeriesItem("Bonus", 12.5));
        incomeSeries.add(new DataSeriesItem("Commission", 12.5));
        incomeSeries.add(new DataSeriesItem("Gift", 12.5));
        incomeSeries.add(new DataSeriesItem("Other", 12.5));
        incomeSeries.add(new DataSeriesItem("Investment", 12.5));
        config.addSeries(incomeSeries);

        VerticalLayout layout = new VerticalLayout(header, incomePieChart);
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getElement().getThemeList().add("spacing-l");
        return layout;
    }

    private Component createExpensePieChart() {
        HorizontalLayout header = createHeader("Expense", "All Time");

        Chart expensePieChart = new Chart(ChartType.PIE);
        Configuration config = expensePieChart.getConfiguration();
        config.getChart().setStyledMode(true);
        expensePieChart.setThemeName("classic");

        DataSeries expenseSeries = new DataSeries();
        expenseSeries.add(new DataSeriesItem("Salary", 12.5));
        expenseSeries.add(new DataSeriesItem("Bonus", 12.5));
        expenseSeries.add(new DataSeriesItem("Commission", 12.5));
        expenseSeries.add(new DataSeriesItem("Gift", 12.5));
        expenseSeries.add(new DataSeriesItem("Other", 12.5));
        expenseSeries.add(new DataSeriesItem("Investment", 12.5));
        config.addSeries(expenseSeries);

        VerticalLayout layout = new VerticalLayout(header, expensePieChart);
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getElement().getThemeList().add("spacing-l");
        return layout;
    }
}
