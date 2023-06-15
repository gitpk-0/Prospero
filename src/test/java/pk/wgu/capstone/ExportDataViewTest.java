package pk.wgu.capstone;

import com.vaadin.flow.component.button.Button;
import com.vaadin.testbench.unit.UIUnitTest;
import com.vaadin.testbench.unit.ViewPackages;
import org.junit.jupiter.api.Test;
import pk.wgu.capstone.views.ExportDataView;
import pk.wgu.capstone.views.MainLayout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ViewPackages(classes = {MainLayout.class, ExportDataView.class})
public class ExportDataViewTest extends UIUnitTest {

    @Test
    public void testGetDownloadBtn() {
        final ExportDataView exportDataView = navigate(ExportDataView.class);

        // verify that the result view is not null
        assertNotNull(exportDataView);

        // create the download button
        Button resultButton = exportDataView.getDownloadBtn();

        // verify that the result button is not null
        assertNotNull(resultButton);

        // verify that the result button has the correct label
        assertEquals("Export Transaction Data", resultButton.getText());
    }
}
