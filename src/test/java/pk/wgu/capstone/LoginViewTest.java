package pk.wgu.capstone;

import com.vaadin.testbench.unit.UIUnitTest;
import com.vaadin.testbench.unit.ViewPackages;
import org.junit.jupiter.api.Test;
import pk.wgu.capstone.views.LoginView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ViewPackages(classes = {LoginView.class})
public class LoginViewTest extends UIUnitTest {

    @Test
    public void testLoginViewForm() {
        final LoginView loginView = navigate(LoginView.class);

        // verify that the login view is not null
        assertNotNull(loginView);

        // verify that the login view has the correct class name
        assertEquals("login-view", loginView.getClassName());

        // verify that the login view has the correct number of components
        assertEquals(5, loginView.getComponentCount()); // logo, h1, h3, form, router link
    }
}
