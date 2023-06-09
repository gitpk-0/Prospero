package pk.wgu.capstone.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import pk.wgu.capstone.data.entity.Transaction;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.security.SecurityService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

        H4 info = new H4();
        info.setText("Click the button below to generate a CSV of all your transaction data");

        H5 contact = new H5();
        contact.setText("If you are having issues downloading your data, please contact prospero.support@pm.me");

        downloadBtn = new Button("Export Transaction Data");
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        downloadBtn.getStyle().set("--lumo-primary-color", "green");

        Long userId = securityService.getCurrentUserId(service);

        List<Transaction> transactions = service.findAllTransactions(userId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            // Write CSV content to the ByteArrayOutputStream
            outputStream.write("Date,Type,Category,Description,Amount\n".getBytes(StandardCharsets.UTF_8));
            for (Transaction transaction : transactions) {
                String row = String.format("%s,%s,%s,%s,%s\n",
                        transaction.getDate(),
                        transaction.getType(),
                        transaction.getCategory().getName(),
                        transaction.getDescription(),
                        transaction.getAmount());
                outputStream.write(row.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        StreamResource csvResource = new StreamResource("data.csv",
                () -> new ByteArrayInputStream(outputStream.toByteArray()));

        Anchor link = new Anchor(csvResource, "");
        link.add(downloadBtn);

        add(
                info,
                link,
                contact
        );
    }
}