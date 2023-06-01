package pk.wgu.capstone.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
        // logo
        Image logo = new Image("icons/icon.png", "Icon");
        logo.addClassName("logo-image");

        // prospero
        H1 prospero = new H1("Prospero");
        prospero.addClassName("app");
        H6 slogan = new H6("Your Path to Prosperity");


        VerticalLayout appAndSlogan = new VerticalLayout(prospero, slogan);
        appAndSlogan.setPadding(false);
        HorizontalLayout logoAppSlogan = new HorizontalLayout(logo, appAndSlogan);
        logoAppSlogan.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Anchor listViewLink = new Anchor("https://patrick-kell.com/");
        listViewLink.add(logoAppSlogan);
        listViewLink.setTarget("_blank"); // open in new window
        listViewLink.addClassNames("text-l", "m-m");

        // log out
        Button logout = new Button("Log out");
        logout.addClassName("btn-large");
        logout.addClickListener(e -> {
            openConfirmLogoutDialogue();
        });

        // light/dark mode toggle
        Checkbox themeToggle = new Checkbox("Dark Mode");
        themeToggle.setValue(true); // selected on start
        themeToggle.addValueChangeListener(e -> {
            setTheme(e.getValue());
        });

        // menu button
        DrawerToggle menuButton = new DrawerToggle();
        menuButton.addClassName("drawer-toggle");

        HorizontalLayout header = new HorizontalLayout(menuButton, listViewLink, themeToggle, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(listViewLink);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void openConfirmLogoutDialogue() {
        // current user's first name
        String firstName = service.findUserById(securityService.getCurrentUserId(service)).getFirstName();

        ConfirmDialog confirmLogout = new ConfirmDialog();
        confirmLogout.setHeader("Log out?");
        confirmLogout.setText(firstName + ", are you sure you want to log out?");
        confirmLogout.setCancelable(true);
        confirmLogout.setConfirmText("Log out");
        confirmLogout.addConfirmListener(e -> securityService.logout());
        confirmLogout.open();
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
