package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | Prospero")
@PermitAll
public class DashboardView extends VerticalLayout {

    private SecurityService securityService;
    private PfmService service;

    public DashboardView(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add(getTransactionStats(), getTransactionsChart());
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
