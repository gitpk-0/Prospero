package pk.wgu.capstone.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

public class MainLayout extends AppLayout {

    private SecurityService securityService;
    private PfmService service;

    public MainLayout(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        createHeader();
        createDrawer();
    }

    private void createDrawer() {
        Image logo = new Image("icons/icon.png", "Icon");
        logo.addClassName("logo-image");

        H1 appname = new H1("Prospero");
        appname.addClassName("app");

        H6 slogan = new H6("Your Path to Prosperity");

        VerticalLayout appAndSlogan = new VerticalLayout(appname, slogan);
        appAndSlogan.setPadding(false);

        HorizontalLayout logoAppSlogan = new HorizontalLayout(logo, appAndSlogan);
        logoAppSlogan.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Anchor listViewLink = new Anchor("localhost:8080");
        listViewLink.add(logoAppSlogan);
        listViewLink.setTarget("_blank"); // open in new window
        listViewLink.addClassNames("text-l", "m-m");

        String firstName = service.findUserById(securityService.getCurrentUserId(service)).getFirstName();

        Button logout = new Button("Log out, " + firstName, e -> securityService.logout());
        logout.addClassName("btn-large");

        Checkbox themeToggle = new Checkbox("Dark Mode");
        themeToggle.setValue(true); // selected on start
        themeToggle.addValueChangeListener(e -> {
            setTheme(e.getValue());
        });

        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.addClassName("drawer-toggle");

        HorizontalLayout header = new HorizontalLayout(drawerToggle, listViewLink, themeToggle, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(listViewLink);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createHeader() {
        RouterLink listView = new RouterLink("Transactions", ListView.class);
        listView.setHighlightCondition(HighlightConditions.sameLocation());
        RouterLink incomeVsExpenseView = new RouterLink("Income vs. Expense", IncomeVsExpenseView.class);
        RouterLink dashboardView = new RouterLink("Dashboard", DashboardView.class);

        addToDrawer(new VerticalLayout(
                listView,
                incomeVsExpenseView
                // dashboardView
        ));
    }

    private void setTheme(boolean dark) {
        var js = "document.documentElement.setAttribute('theme', $0)";
        getElement().executeJs(js, dark ? Lumo.DARK : Lumo.LIGHT);
    }
}
