package pk.wgu.capstone.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinResponse;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


@PageTitle("Export Data | Prospero")
@Route(value = "export-transaction-data", layout = MainLayout.class)
@PermitAll
public class ExportDataView extends VerticalLayout {


    private SecurityService securityService;
    private PfmService service;
    private Button downloadBtn;

    public ExportDataView(SecurityService securityService, PfmService service) {
        this.securityService = securityService;
        this.service = service;

        downloadBtn = new Button("Export Transaction Data");
        downloadBtn.addClickListener(e -> downloadCsv());

        add(downloadBtn);
    }

    private InputStream generateCsvInputStream() {
        Long userId = securityService.getCurrentUserId(service);

        List<Transaction> transactions = service.findAllTransactions(userId, null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            // Write CSV content to the ByteArrayOutputStream
            outputStream.write("Date,Type,Category,Description,Amount\n".getBytes(StandardCharsets.UTF_8));
            for (Transaction transaction : transactions) {
                String row = String.format("%s,%s,%s,%s,%s\n",
                        transaction.getDate(),
                        transaction.getType(),
                        transaction.getCategory(),
                        transaction.getDescription(),
                        transaction.getAmount());
                outputStream.write(row.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private void downloadCsv() {
        InputStream csvInputStream = generateCsvInputStream();

        StreamResource csvResource = new StreamResource("transactions.csv", () -> csvInputStream);


        Anchor downloadLink = new Anchor(csvResource, "");
        downloadLink.getElement().setAttribute("download", true);

        VaadinResponse.getCurrent().setHeader("Content-Disposition", "attachment; filename=transactions.csv");

        downloadLink.getElement().callJsFunction("click");

        // // Set the Content-Disposition header for file download
        // VaadinResponse.getCurrent().setHeader("Content-Disposition", "attachment; filename=transactions.csv");
        //
        // // Redirect the user to the download URL
        // UI.getCurrent().getPage().executeJs("window.open($0, '_blank');", csvResource);
        //
        // // Remove the anchor element from the UI
        // downloadLink.getElement().removeFromParent();

    }
}