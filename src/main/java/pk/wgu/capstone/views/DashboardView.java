package pk.wgu.capstone.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | Prospero")
@PermitAll
public class DashboardView extends VerticalLayout {

    private SecurityService securityService;
    private PfmService service;

    public DashboardView(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add();
    }
}
