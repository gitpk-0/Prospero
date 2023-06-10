package pk.wgu.capstone.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.converter.BigDecimalToDoubleConverter;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Prospero")
@PermitAll
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
}
