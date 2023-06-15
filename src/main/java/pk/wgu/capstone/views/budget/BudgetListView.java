package pk.wgu.capstone.views.budget;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.entity.Budget;
import pk.wgu.capstone.data.entity.Type;
import pk.wgu.capstone.data.entity.report.CategoryTotal;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;
import pk.wgu.capstone.views.MainLayout;
import pk.wgu.capstone.views.forms.BudgetForm;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@PageTitle("Budgets | Prospero")
@Route(value = "budget-list", layout = MainLayout.class)
@PermitAll
@CssImport(value = "./themes/prospero/prospero-charts.css", themeFor = "vaadin-chart")
@CssImport(value = "./themes/prospero/views/budget-list-view.css")
public class BudgetListView extends Main implements HasComponents, HasStyle {

    private SecurityService securityService;
    private PfmService service;

    private List<BudgetListViewCard> budgetListViewCards;
    private OrderedList budgetContainer;
    private VerticalLayout dialogLayout;
    private Dialog dialog;
    private Select<String> sortBy;
    private BudgetForm budgetForm;

    public BudgetListView(SecurityService securityService, PfmService service) {
        addClassName("budget-list-view");
        this.securityService = securityService;
        this.service = service;

        generateUI();
        configureBudgetCards();
    }

    private void generateUI() {
        addClassName("budget-list-view");
        addClassNames(LumoUtility.MaxWidth.SCREEN_LARGE,
                LumoUtility.Margin.Horizontal.AUTO,
                LumoUtility.Padding.Bottom.LARGE,
                LumoUtility.Padding.Horizontal.LARGE);

        VerticalLayout headerContainer = new VerticalLayout();
        headerContainer.addClassNames(LumoUtility.AlignItems.CENTER);
        H2 header = new H2("Budgets");
        header.addClassNames(LumoUtility.Margin.Bottom.NONE,
                LumoUtility.Margin.Top.NONE, LumoUtility.FontSize.XXLARGE);

        Paragraph description = new Paragraph("Conquer Your Wealth Objectives");
        description.addClassNames(LumoUtility.Margin.Bottom.NONE,
                LumoUtility.Margin.Top.NONE, LumoUtility.TextColor.SECONDARY);

        headerContainer.add(header, description);

        HorizontalLayout optionsContainer = new HorizontalLayout();
        optionsContainer.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        optionsContainer.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Newest to oldest", "Oldest to newest",
                "Start Date →", "← Start Date", "End Date →", "← End Date");
        sortBy.setValue("Newest to oldest");

        sortBy.addValueChangeListener(e -> {
            sortBudgetsByOption(sortBy);
        });

        Button createBudgetBtn = new Button("Create Budget");
        createBudgetBtn.addClickListener(e -> {
            addBudget();
        });

        optionsContainer.add(sortBy, createBudgetBtn);

        budgetListViewCards = new ArrayList<>();
        budgetContainer = new OrderedList();
        budgetContainer.addClassNames(LumoUtility.Gap.MEDIUM,
                LumoUtility.Display.GRID, LumoUtility.ListStyleType.NONE,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE);

        add(
                headerContainer,
                optionsContainer,
                budgetContainer
        );
    }

    private void configureBudgetCards() {
        budgetListViewCards.clear();
        budgetContainer.removeAll();
        Long userId = securityService.getCurrentUserId(service);

        List<Budget> budgets = service.getBudgetsByUserId(userId);
        budgets.stream().forEach(budget -> {
            BudgetListViewCard card = new BudgetListViewCard(
                    budget,
                    calcBudgetProgress(budget, userId),
                    calcBudgetStatus(budget),
                    service.getSumExpensesInDateRange(
                            budget.getStart(),
                            budget.getEnd(),
                            Type.EXPENSE,
                            userId)
            );
            card.addClickListener(e -> {
                viewBudgetChart(budget);
            });
            budgetListViewCards.add(card);
        });
        Collections.reverse(budgetListViewCards); // newest created to oldest

        for (BudgetListViewCard card : budgetListViewCards) {
            budgetContainer.add(card);
        }
    }

    private void sortBudgetsByOption(Select<String> sortBy) {
        String sortOption = sortBy.getValue();
        applySortingToBudgets(budgetListViewCards, sortOption);
        budgetContainer.removeAll();
        budgetListViewCards.forEach(budgetContainer::add);
    }

    private void applySortingToBudgets(List<BudgetListViewCard> budgetListViewCards, String sortOption) {
        if (sortOption.equals("Newest to oldest")) {
            budgetListViewCards.sort(Comparator.comparing(card -> card.getBudget().getDateCreated()));
            Collections.reverse(budgetListViewCards);
        } else if (sortOption.equals("Oldest to newest")) {
            budgetListViewCards.sort(Comparator.comparing(card -> card.getBudget().getDateCreated()));
        } else if (sortOption.equals("Start Date →")) {
            budgetListViewCards.sort(Comparator.comparing(card -> card.getBudget().getStart()));
        } else if (sortOption.equals("← Start Date")) {
            budgetListViewCards.sort(Comparator.comparing(card -> card.getBudget().getStart()));
            Collections.reverse(budgetListViewCards);
        } else if (sortOption.equals("End Date →")) {
            budgetListViewCards.sort(Comparator.comparing(card -> card.getBudget().getEnd()));
        } else if (sortOption.equals("← End Date")) {
            budgetListViewCards.sort(Comparator.comparing(card -> card.getBudget().getEnd()));
            Collections.reverse(budgetListViewCards);
        }
    }

    private String calcBudgetStatus(Budget budget) {
        Date start = budget.getStart();
        Date end = budget.getEnd();
        Date currentDate = Date.valueOf(LocalDate.now());

        if (currentDate.before(start)) {
            return "Not Started";
        } else if (currentDate.after(end)) {
            return "Completed";
        } else {
            return "In Progress";
        }
    }

    private Double calcBudgetProgress(Budget budget, Long userId) {
        Date start = budget.getStart();
        Date end = budget.getEnd();
        Double spendingTotal = Double.valueOf(String.valueOf(
                service.getSumExpensesInDateRange(start, end, Type.EXPENSE, userId)));
        Double spendingGoal = Double.valueOf(String.valueOf(budget.getSpendingGoal()));

        return spendingTotal / spendingGoal;
    }

    // Budget Form
    public void editBudget(Budget budget, Dialog dialog) {
        if (budget == null) {
            dialog.close();
            closeBudgetEditor();
        } else {
            configureForm();
            if (dialog != null) {
                dialog.close();
            }
            Dialog editBudgetDialog = new Dialog();
            H1 header;
            if (budget.getName() == null) { // create new budget
                header = new H1("Create Budget");
                budgetForm.addClassName("create-budget-form");
                budgetForm.setBudget(budget);
                budgetForm.setDatePickerValues();
            } else {
                header = new H1("Edit Budget");
                budgetForm.setBudget(budget);
            }
            header.getStyle().set("font-size", "xx-large").set("padding-left", "1rem").set("padding-top", "0.5rem");
            editBudgetDialog.add(header);
            editBudgetDialog.add(budgetForm);

            if (header.getText().equals("Create Budget")) {
                editBudgetDialog.getFooter().add(budgetForm.createNewBudgetButtonLayout(editBudgetDialog));
            } else {
                editBudgetDialog.getFooter().add(budgetForm.editBudgetButtonLayout(editBudgetDialog));
            }

            editBudgetDialog.open();
        }

    }

    public void updateBudgetList() {
        configureBudgetCards();
        closeBudgetEditor();
    }

    public void closeDialog() {
        if (dialog != null && dialog.isOpened()) {
            dialog.close();
        }
    }

    private void configureForm() {
        budgetForm = new BudgetForm(securityService, service);
        budgetForm.getStyle()
                .set("padding-left", "1rem")
                .set("padding-right", "1rem")
                .set("padding-bottom", "1rem");
        budgetForm.setWidth("25em");
        budgetForm.addClassName("budget-form");
        budgetForm.setBudgetListView(this);
        budgetForm.setDatePickerValues();
    }

    private void addBudget() {
        editBudget(new Budget(), new Dialog());
    }

    private void closeBudgetEditor() {
        if (budgetForm != null) {
            budgetForm.setBudget(null);
            dialogLayout = null;
        }
    }

    private void viewBudgetChart(Budget budget) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

        if (budget == null) {
            dialog.close();
            closeBudgetEditor();
        } else {
            dialogLayout = new VerticalLayout();
            H1 title = new H1(budget.getName());
            title.getStyle().set("font-size", "xx-large");
            H3 subtitle = new H3("Expense Breakdown");
            subtitle.getStyle().set("font-size", "larger").set("padding-bottom", "2rem");
            subtitle.addClassName("subtitle-text");
            H3 spendingGoal = new H3("Spending Goal: " + currencyFormat.format(budget.getSpendingGoal()));
            spendingGoal.getStyle().set("font-size", "large").set("padding-top", "1rem");
            H3 totalExpenses = new H3("Total Expenses: " +
                    currencyFormat.format(
                            service.getSumExpensesInDateRange(
                                    budget.getStart(), budget.getEnd(), Type.EXPENSE, budget.getUserId())));
            totalExpenses.getStyle().set("font-size", "large");
            totalExpenses.addClassName("total-expenses-text");

            dialogLayout.add(title, subtitle, getBudgetChart(budget), spendingGoal, totalExpenses);
            dialogLayout.addClassName("budget-dialog-vl");

            dialog = new Dialog(dialogLayout);
            dialog.setClassName("budget-dialog");

            Button cancelButton = new Button("Cancel", e -> {
                dialog.close();
                closeBudgetEditor();
            });
            cancelButton.getStyle().set("padding", "1rem");

            Button editButton = new Button("Edit Budget", e -> {
                editBudget(budget, dialog);
            });
            editButton.getStyle().set("padding", "1rem");
            editButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

            dialog.getFooter().add(cancelButton, editButton);
            dialog.open();
        }
    }

    private Component getBudgetChart(Budget budget) {
        Long userId = securityService.getCurrentUserId(service);

        Chart pieChart = new Chart(ChartType.PIE);
        pieChart.setThemeName("classic");
        pieChart.setClassName("expense-pie-chart");
        Configuration config = pieChart.getConfiguration();
        DataSeries dataSeries = new DataSeries();
        dataSeries.setName("Expenses");

        List<Object[]> expenseResults =
                service.sumTransactionsInDateRangeByCategory(userId, Type.EXPENSE, budget.getStart(), budget.getEnd());

        if (!expenseResults.isEmpty()) {
            List<CategoryTotal> expenseData = expenseResults
                    .stream()
                    .map(row -> new CategoryTotal((String) row[0], (BigDecimal) row[1])).toList();

            expenseData.stream().forEach(dataItem -> {
                DataSeriesItem expenseItem = new DataSeriesItem(dataItem.getCategoryName(), dataItem.getTotalAmount());
                expenseItem.setClassName("expense-item-slice");
                dataSeries.add(expenseItem);
                DataLabels totalLabel = new DataLabels(true);
                totalLabel.setShape(Shape.CALLOUT);
                totalLabel.setY(-10);
                totalLabel.setFormatter("function() { return this.point.name }");
                totalLabel.setInside(true);
                expenseItem.setDataLabels(totalLabel);
            });

            Tooltip tooltip = new Tooltip();
            tooltip.setFormatter("function() {" +
                    "    return '<br/><b>' + this.point.name + '</b><br/>' +" +
                    "        '$' + Highcharts.numberFormat(this.point.y, 2, '.', ',') + '<br/><br/>'}");


            tooltip.setEnabled(true);
            config.setTooltip(tooltip);

            config.getChart().setStyledMode(true);
            config.getLegend().setEnabled(false);
            config.setSeries(dataSeries);

            pieChart.addClassName("budget-pie-chart");

            return pieChart;
        } else {
            return new Text("No transactions within the time frame of this budget.");
        }
    }
}
