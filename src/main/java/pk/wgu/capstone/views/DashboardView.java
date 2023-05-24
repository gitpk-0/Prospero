package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
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

    private PfmService service;
    private SecurityService securityService;

    public DashboardView(PfmService pfmService, SecurityService securityService) {
        this.service = pfmService;
        this.securityService = securityService;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add(getTransactionStats(), getTransactionsChart());


    }

    private Component getTransactionStats() {
        Long userId = service.findUserByEmail(securityService.getAuthenticatedUser().getUsername()).getId();

        Span totalTransactions = new Span(service.countTransactions() + " transactions");
        Span incomes = new Span(service.countTransactionsByType(userId, Type.INCOME) + " income transactions");
        Span expenses = new Span(service.countTransactionsByType(userId, Type.EXPENSE) + " expenses transactions");
        totalTransactions.addClassNames("text-xl", "mt-m");
        incomes.addClassNames("text-xl", "mt-m");
        expenses.addClassNames("text-xl", "mt-m");

        return new VerticalLayout(totalTransactions, incomes, expenses);
    }

    private Component getTransactionsChart() {
        Long userId = service.findUserByEmail(securityService.getAuthenticatedUser().getUsername()).getId();


        Chart chart = new Chart(ChartType.PIE);

        DataSeries dataSeries = new DataSeries();
        service.findAllTypes().forEach(type -> {
            dataSeries.add(new DataSeriesItem(type.name(), service.countTransactionsByType(userId, type)));
        });

        chart.getConfiguration().setSeries(dataSeries);
        return chart;
    }
}
