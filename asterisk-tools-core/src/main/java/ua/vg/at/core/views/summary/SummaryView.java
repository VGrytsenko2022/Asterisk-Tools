package ua.vg.at.core.views.summary;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import jakarta.annotation.security.RolesAllowed;
import ua.vg.at.core.views.MainLayout;

@PageTitle("Summary")
@Route(value = "summary", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class SummaryView extends Composite<VerticalLayout> {

    public SummaryView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        Paragraph textMedium = new Paragraph();
        Paragraph textMedium2 = new Paragraph();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        Paragraph textMedium3 = new Paragraph();
        Paragraph textMedium4 = new Paragraph();
        HorizontalLayout layoutRow3 = new HorizontalLayout();
        Paragraph textMedium5 = new Paragraph();
        Paragraph textMedium6 = new Paragraph();
        HorizontalLayout layoutRow4 = new HorizontalLayout();
        Paragraph textMedium7 = new Paragraph();
        Paragraph textMedium8 = new Paragraph();
        HorizontalLayout layoutRow5 = new HorizontalLayout();
        Avatar avatar = new Avatar();
        getContent().setHeightFull();
        getContent().setWidthFull();
        getContent().setSpacing(false);
        getContent().addClassName(Padding.XSMALL);
        layoutRow.setWidthFull();
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setAlignItems(Alignment.START);
        layoutRow.setJustifyContentMode(JustifyContentMode.CENTER);
        textMedium.setText("Asterisk version:");
        textMedium.getStyle().set("font-size", "var(--lumo-font-size-m)");
        textMedium2.setText("20");
        textMedium2.getStyle().set("font-size", "var(--lumo-font-size-m)");
        layoutRow2.setWidthFull();
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.setAlignItems(Alignment.START);
        layoutRow2.setJustifyContentMode(JustifyContentMode.CENTER);
        textMedium3.setText("Asterisk version:");
        textMedium3.getStyle().set("font-size", "var(--lumo-font-size-m)");
        textMedium4.setText("20");
        textMedium4.getStyle().set("font-size", "var(--lumo-font-size-m)");
        layoutRow3.setWidthFull();
        layoutRow3.addClassName(Gap.MEDIUM);
        layoutRow3.setAlignItems(Alignment.START);
        layoutRow3.setJustifyContentMode(JustifyContentMode.CENTER);
        textMedium5.setText("Asterisk version:");
        textMedium5.getStyle().set("font-size", "var(--lumo-font-size-m)");
        textMedium6.setText("20");
        textMedium6.getStyle().set("font-size", "var(--lumo-font-size-m)");
        layoutRow4.setWidthFull();
        layoutRow4.addClassName(Gap.MEDIUM);
        layoutRow4.setAlignItems(Alignment.START);
        layoutRow4.setJustifyContentMode(JustifyContentMode.CENTER);
        textMedium7.setText("Asterisk version:");
        textMedium7.getStyle().set("font-size", "var(--lumo-font-size-m)");
        textMedium8.setText("20");
        textMedium8.getStyle().set("font-size", "var(--lumo-font-size-m)");
        layoutRow5.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow5);
        layoutRow5.setAlignItems(Alignment.CENTER);
        layoutRow5.setJustifyContentMode(JustifyContentMode.CENTER);
        avatar.setName("Firstname Lastname");
        getContent().add(layoutRow);
        layoutRow.add(textMedium);
        layoutRow.add(textMedium2);
        getContent().add(layoutRow2);
        layoutRow2.add(textMedium3);
        layoutRow2.add(textMedium4);
        getContent().add(layoutRow3);
        layoutRow3.add(textMedium5);
        layoutRow3.add(textMedium6);
        getContent().add(layoutRow4);
        layoutRow4.add(textMedium7);
        layoutRow4.add(textMedium8);
        getContent().add(layoutRow5);
        layoutRow5.add(avatar);
    }
}
