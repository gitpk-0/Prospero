package pk.wgu.capstone.views.budget;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.theme.lumo.LumoUtility;

@CssImport(value = "./styles/budget-card.css")
public class BudgetListViewCard extends ListItem {

    public BudgetListViewCard(String budgetName, Double progress, String status) {
        addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.AlignItems.START, LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.LARGE);

        Div card = new Div();
        card.addClassNames(LumoUtility.Background.CONTRAST, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER, LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Overflow.HIDDEN,
                LumoUtility.BorderRadius.MEDIUM, LumoUtility.Width.FULL);

        ProgressBar progressBar = new ProgressBar();
        progressBar.addClassName("progress-bar");
        String progressStr = (int) (progress * 100) + "%";


        Div budgetUtilization = new Div();
        budgetUtilization.setText("Budget Utilization: ");
        Div progressValue = new Div();
        progressValue.setText(progressStr);
        HorizontalLayout layout = new HorizontalLayout(budgetUtilization, progressValue);
        layout.addClassName("progress-bar-label");

        if (progress <= 0.75) {
            progressBar.getStyle().set("--progress-color", "#158443"); // green
            progressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
            progressValue.addClassName("green");
        } else if (progress > 0.75 && progress <= 0.9) {
            progressBar.getStyle().set("--progress-color", "#ffbd07"); // yellow
            progressValue.addClassName("yellow");
        } else {
            progressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
            progressBar.getStyle().set("--progress-color", "#E21D12"); // red
            progressValue.addClassName("red");
        }


        // String[] budgetStatusList = {"Not Started", "In Progress", "Complete"};
        String[] goalStatusList = {"Within Budget", "At Budget Capacity", "Over Budget"};


        // String budgetStatus = "";
        String goalStatus = "";
        double progressBarValue = 0.0;


        if (progress > 1.0) {
            progressBarValue = 1.0;
            goalStatus = goalStatusList[2];
        } else if (progress == 1.0) {
            progressBarValue = 1.0;
            goalStatus = goalStatusList[1];
        } else if (progress < 0.0) {
            progressBarValue = 0.0;
            goalStatus = goalStatusList[0];
        } else {
            progressBarValue = progress;
            goalStatus = goalStatusList[0];
        }
        progressBar.setValue(progressBarValue);
        VerticalLayout progressLayout = new VerticalLayout(layout, progressBar);
        card.add(progressLayout);


        Span budgetTitle = new Span();
        budgetTitle.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.SEMIBOLD);
        budgetTitle.setText(budgetName);

        Span startDate = new Span();
        startDate.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        startDate.setText("Start Date: Thur, Jun 1, 2023");
        Span endDate = new Span();
        endDate.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        endDate.setText("End Date: Fri, Jun 30, 2023");

        Paragraph budgetDescription = new Paragraph(
                "A brief description that describes the purpose of this budget.");
        budgetDescription.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

        Span statusBadge = new Span();
        statusBadge.getElement().setAttribute("theme", "badge");
        statusBadge.setText(status);

        Span goalStatusBadge = new Span();
        goalStatusBadge.getElement().setAttribute("theme", "badge");
        goalStatusBadge.setText(goalStatus);

        HorizontalLayout badges = new HorizontalLayout(statusBadge, goalStatusBadge);

        add(
                budgetTitle,
                startDate,
                endDate,
                budgetDescription,
                progressLayout,
                badges
        );

    }
}
