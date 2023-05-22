package pk.wgu.capstone.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pk.wgu.capstone.data.binder.RegistrationFormBinder;
import pk.wgu.capstone.data.service.PfmService;
import pk.wgu.capstone.views.forms.RegistrationForm;

@Route("register")
@PageTitle("Register | Prospero")
public class RegistrationView  extends VerticalLayout {

    private PfmService service;

    public RegistrationView(PfmService service) {
        this.service = service;

        setSizeFull();
        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setWidth("600px");
        setAlignItems(Alignment.CENTER);

        setHorizontalComponentAlignment(Alignment.CENTER, registrationForm);
        
        add(registrationForm);

        RegistrationFormBinder registrationFormBinder = new RegistrationFormBinder(registrationForm, service);
        registrationFormBinder.addBindingAndValidation();
    }
}
