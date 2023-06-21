package pk.wgu.capstone;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
@Theme(value = "prospero", variant = Lumo.DARK)
// @Theme(value = "prospero", variant = Lumo.LIGHT)
@PWA(
        name = "Prospero",
        shortName = "Prospero",
        offlinePath = "offline.html",
        offlineResources = {"images/offline.png"}
)
@NpmPackage(value = "line-awesome", version = "1.3.0")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.systemDefault()));
        SpringApplication.run(Application.class, args);
    }

}