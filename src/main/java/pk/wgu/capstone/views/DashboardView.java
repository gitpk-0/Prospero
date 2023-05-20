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

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | Prospero")
@PermitAll
public class DashboardView extends VerticalLayout {

    private PfmService pfmService;

    public DashboardView(PfmService pfmService) {
        this.pfmService = pfmService;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add(getTransactionStats(), getTransactionsChart());
    }

    private Component getTransactionStats() {
        Span totalTransactions = new Span(pfmService.countTransactions() + " transactions");
        Span incomes = new Span(pfmService.countTransactionsByType(Type.INCOME) + " income transactions");
        Span expenses = new Span(pfmService.countTransactionsByType(Type.EXPENSE) + " expenses transactions");
        totalTransactions.addClassNames("text-xl", "mt-m");
        incomes.addClassNames("text-xl", "mt-m");
        expenses.addClassNames("text-xl", "mt-m");

        return new VerticalLayout(totalTransactions, incomes, expenses);
    }

    private Component getTransactionsChart() {
        Chart chart = new Chart(ChartType.PIE);

        DataSeries dataSeries = new DataSeries();
        pfmService.findAllTypes().forEach(type -> {
            dataSeries.add(new DataSeriesItem(type.name(), pfmService.countTransactionsByType(type)));
        });

        chart.getConfiguration().setSeries(dataSeries);
        return chart;
    }
}
