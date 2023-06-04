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

    public BudgetListViewCard(String budgetName, Double progress) {
        addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.AlignItems.START, LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.LARGE);

        Div div = new Div();
        div.addClassNames(LumoUtility.Background.CONTRAST, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER, LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Overflow.HIDDEN,
                LumoUtility.BorderRadius.MEDIUM, LumoUtility.Width.FULL);
        // div.setHeight("160px"); // **** CHANGE TO EM/REM ****

        ProgressBar progressBar = new ProgressBar();
        progressBar.setHeight(".2rem");
        progressBar.addClassName("progress-bar");
        if (progress > 1.0) {
            progress = 1.0;
        }

        String progressStr = String.valueOf(Integer.valueOf((int) (progress * 100))) + "%";
        Div budgetUtilization = new Div();
        budgetUtilization.setText("Budget Utilization: ");
        Div progressValue = new Div();
        progressValue.setText(progressStr);
        HorizontalLayout layout = new HorizontalLayout(budgetUtilization, progressValue);
        layout.addClassName("progress-bar-label");

        if (progress <= 0.7) {
            progressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
            progressValue.addClassName("green");
        } else if (progress > 0.8 && progress <= 0.9) {
            progressValue.addClassName("yellow");
        } else {
            progressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
            progressValue.addClassName("red");
        }


        progressBar.setValue(progress);
        VerticalLayout progressLayout = new VerticalLayout(layout, progressBar);
        div.add(progressLayout);


        Span header = new Span();
        header.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.SEMIBOLD);
        header.setText(budgetName);

        Span subtitle = new Span();
        subtitle.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        subtitle.setText("Card subtitle");

        Paragraph description = new Paragraph(
                "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut.");
        description.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

        Span badge = new Span();
        badge.getElement().setAttribute("theme", "badge");
        badge.setText("Label");

        add(header, subtitle, description, progressLayout, badge);

    }
}
