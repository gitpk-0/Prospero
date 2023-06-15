package pk.wgu.capstone;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.testbench.unit.UIUnitTest;
import com.vaadin.testbench.unit.ViewPackages;
import org.junit.jupiter.api.Test;
import pk.wgu.capstone.views.MainLayout;
import pk.wgu.capstone.views.UserSettingsView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ViewPackages(classes = {MainLayout.class, UserSettingsView.class})
public class UserSettingsViewTest extends UIUnitTest {

    @Test
    public void testCreateChangeAppThemeButton() {
        final UserSettingsView userSettingsView = navigate(UserSettingsView.class);

        // create the change app theme button
        Button resultButton = userSettingsView.getcreateChangeAppThemeButton();

        // verify that the result button is not null
        assertNotNull(resultButton);

        // verify that the result button has the correct label
        assertEquals("Change App Theme", resultButton.getText());

        // verify that the result button has the correct title
        assertEquals("Change current theme", resultButton.getElement().getAttribute("title"));
    }

    @Test
    public void testCreateButtonLayout() {
        final UserSettingsView userSettingsView = navigate(UserSettingsView.class);

        HorizontalLayout resultLayout = userSettingsView.getCreateButtonLayout();

        // verify that the result layout is not null
        assertNotNull(resultLayout);

        // verify that the result layout has the correct number of components
        assertEquals(2, resultLayout.getComponentCount());  // update and cancel button

        // verify that the result layout has the correct class name
        assertEquals("button-layout my-s", resultLayout.getClassName());
    }
}
