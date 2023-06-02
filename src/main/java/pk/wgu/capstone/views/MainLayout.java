package pk.wgu.capstone.views;


import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

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
        // logo
        Image logo = new Image("icons/icon.png", "Icon");
        logo.addClassName("logo-image");

        // prospero
        H1 prospero = new H1("Prospero");
        prospero.addClassName("app-name");
        H6 slogan = new H6("Your Path to Prosperity");
        slogan.addClassName("slogan");


        VerticalLayout appAndSlogan = new VerticalLayout(prospero, slogan);
        appAndSlogan.setPadding(false);
        HorizontalLayout logoAppSlogan = new HorizontalLayout(logo, appAndSlogan);
        logoAppSlogan.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        logoAppSlogan.addClassName("logo-app-slogan");

        Anchor listViewLink = new Anchor("https://patrick-kell.com/");
        listViewLink.add(logoAppSlogan);
        listViewLink.setTarget("_blank"); // open in new window
        listViewLink.addClassNames("text-l", "m-m");

        // log out
        Button logout = new Button("Log out");
        logout.addClassName("btn-large");
        logout.addClickListener(e -> {
            confirmLogoutDialog();
        });

        // light/dark mode toggle
        Icon themeIcon = new Icon(VaadinIcon.MOON);
        Button themeBtn = new Button(themeIcon);
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


        HorizontalLayout header = new HorizontalLayout(menuButton, listViewLink, themeBtn, logout);
        header.addClassNames("header");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(listViewLink);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
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
        H1 prospero = new H1("Prospero");
        prospero.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(prospero);

        // log out
        Button logout = new Button("Log out");
        logout.addClassNames("btn-large", "header-log-out-btn");
        logout.addClickListener(e -> {
            confirmLogoutDialog();
        });

        // light/dark mode toggle
        Icon themeIcon = new Icon(VaadinIcon.MOON);
        Button themeBtn = new Button(themeIcon);


        themeBtn.addClickListener(e -> {
            if (isDarkTheme) {
                setTheme(false);
                isDarkTheme = false;
            } else {
                setTheme(true);
                isDarkTheme = true;
            }
        });

        FlexLayout linkContent = new FlexLayout();

        linkContent.addClassName("link-content");
        linkContent.add(links);

        HorizontalLayout logoutAndTheme = new HorizontalLayout(logout, themeBtn);
        logoutAndTheme.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        logoutAndTheme.addClassName("logout-and-theme");

        VerticalLayout drawerContent = new VerticalLayout(linkContent, logoutAndTheme);

        addToDrawer(
                // drawerContent
                // listView,
                // incomeVsExpenseView,
                links,
                logoutAndTheme
                // logout,
                // themeToggle
                // dashboardView
        );
    }

    private Tabs getTabs() {
        Tabs tabs = new Tabs();
        tabs.add(
                createTab(VaadinIcon.LIST, "Transactions", ListView.class),
                createTab(VaadinIcon.CHART, "Income vs. Expenses", IncomeVsExpenseView.class)
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
