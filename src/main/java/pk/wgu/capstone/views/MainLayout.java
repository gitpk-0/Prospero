package pk.wgu.capstone.views;


import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;
import pk.wgu.capstone.views.budget.BudgetListView;

public class MainLayout extends AppLayout {

    private SecurityService securityService;
    private PfmService service;

    boolean isDarkTheme = true;

    public MainLayout(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        Tabs links = getTabs();

        addDrawer(links);
        addHeader();
    }

    private void addHeader() {
        // header logo and app name
        Image logo = new Image("icons/icon.png", "Icon");
        logo.addClassName("logo");
        H1 appname = new H1("Prospero");
        appname.addClassName("app-name");
        HorizontalLayout logoAppname = new HorizontalLayout(logo, appname);
        logoAppname.addClassName("logo-app-name");

        Anchor listViewLink = new Anchor("https://patrick-kell.com/");
        listViewLink.add(logoAppname);
        listViewLink.setTarget("_blank"); // open in new window
        listViewLink.addClassNames("text-l", "m-m");

        // light/dark mode toggle button
        Button themeBtn = new Button(VaadinIcon.MOON.create());
        themeBtn.addClassName("theme-btn");
        themeBtn.getElement().setAttribute("title", "Change current theme");
        themeBtn.addClickListener(e -> {
            if (isDarkTheme) {
                setTheme(false);
                isDarkTheme = false;
            } else {
                setTheme(true);
                isDarkTheme = true;
            }
        });

        // menu button
        DrawerToggle menuButton = new DrawerToggle();
        menuButton.addClassName("drawer-toggle");

        HorizontalLayout pageHeader = new HorizontalLayout(menuButton, logoAppname, themeBtn);

        pageHeader.addClassNames("page-header");
        pageHeader.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        pageHeader.expand(listViewLink);
        pageHeader.setWidthFull();
        pageHeader.addClassName("px-m");

        addToNavbar(pageHeader);
    }

    private void confirmLogoutDialog() {
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

    private void addDrawer(Tabs links) {
        // drawer logo
        Image logo = new Image("icons/icon.png", "Icon");
        logo.addClassName("drawer-logo");

        // drawer app name
        H1 prospero = new H1("Prospero");
        prospero.addClassNames("drawer-app-name");

        // drawer header
        HorizontalLayout drawerHeader = new HorizontalLayout(logo, prospero);
        drawerHeader.addClassNames("drawer-header");

        // drawer log out
        Button logoutBtn = new Button("Log out");
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        logoutBtn.setIcon(VaadinIcon.SIGN_OUT.create());
        logoutBtn.addClassNames("logout-btn");
        logoutBtn.addClickListener(e -> {
            confirmLogoutDialog();
        });

        FlexLayout linkContent = new FlexLayout();
        linkContent.addClassName("link-content");
        linkContent.add(links);

        addToDrawer(
                drawerHeader,
                linkContent,
                logoutBtn
        );
    }

    private Tabs getTabs() {
        Tabs tabs = new Tabs();
        tabs.add(
                createTab(VaadinIcon.LIST, "Transactions", TransactionView.class),
                createTab(VaadinIcon.CHART, "Income vs. Expenses", IncomeVsExpenseView.class),
                createTab(VaadinIcon.ABACUS, "Budgets", BudgetListView.class)
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        return tabs;
    }

    private Tab createTab(VaadinIcon viewIcon, String viewName, Class viewClass) {
        Icon icon = viewIcon.create();
        icon.getStyle().set("box-sizing", "border-box")
                .set("margin-inline-end", "var(--lumo-space-xs)")
                .set("margin-inline-start", "var(--lumo-space-xs)")
                .set("padding", "var(--lumo-space-xs)");

        RouterLink link = new RouterLink();
        link.add(icon, new Span(viewName));
        link.setRoute(viewClass);
        link.setTabIndex(-1);

        return new Tab(link);
    }

    private void setTheme(boolean dark) {
        var js = "document.documentElement.setAttribute('theme', $0)";
        getElement().executeJs(js, dark ? Lumo.DARK : Lumo.LIGHT);
    }
}
