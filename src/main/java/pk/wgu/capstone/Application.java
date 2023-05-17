package pk.wgu.capstone;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
@Theme(value = "prospero")
@PWA(
        name = "Prospero",
        shortName = "Prospero",
        offlinePath = "offline.html",
        offlineResources = {"images/offline.png"}
)
@NpmPackage(value = "line-awesome", version = "1.3.0")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
