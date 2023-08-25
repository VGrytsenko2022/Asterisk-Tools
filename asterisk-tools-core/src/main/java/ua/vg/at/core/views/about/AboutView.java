package ua.vg.at.core.views.about;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import jakarta.annotation.security.RolesAllowed;
import ua.vg.at.core.views.MainLayout;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AboutView extends VerticalLayout {
    public AboutView() {
        setSpacing(false);

        Image img = new Image("images/PidginBird.png", "placeholder plant");
        img.setWidth("200px");
        add(img);

        H2 header = new H2("Asterisk - control functions remotely");
        header.addClassNames(Margin.Top.XLARGE, Margin.Bottom.MEDIUM);
        add(header);
        add(new Paragraph("Software developer: Valentyn Grytsenko, vallico@ukr.net, Ukraine Kyiv 2023 🤗"));

        Button donateButton = new Button("Help this project");
        donateButton.addClickListener(e -> this.openPaypal());
        add(donateButton);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    private void openPaypal() {
        if (getUI().isPresent()) {
            getUI().get().getPage().open("https://www.paypal.com/donate/?hosted_button_id=GRQBC554NA356", "_blank");
        }
    }

}