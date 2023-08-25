package ua.vg.at.core.views.setting;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ua.vg.at.core.data.entity.SamplePerson;
import ua.vg.at.core.data.service.SamplePersonService;
import ua.vg.at.core.views.MainLayout;

@PageTitle("Setting")
@Route(value = "setting", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class SettingView extends Composite<VerticalLayout> {

    public SettingView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        Button buttonSecondary = new Button();
        Button buttonSecondary2 = new Button();
        Button buttonSecondary3 = new Button();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        Grid multiSelectGrid = new Grid(SamplePerson.class);
        getContent().setHeightFull();
        getContent().setWidthFull();
        getContent().setSpacing(false);
        layoutRow.setWidthFull();
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setFlexGrow(1.0, layoutRow2);
        layoutRow2.addClassName(Gap.XSMALL);
        buttonSecondary.setText("Edit");
        buttonSecondary2.setText("Add");
        buttonSecondary3.setText("Delete");
        getContent().setFlexGrow(1.0, layoutColumn2);
        layoutColumn2.setWidthFull();
        layoutColumn2.setPadding(false);
        multiSelectGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        layoutColumn2.setFlexGrow(1.0, multiSelectGrid);
        multiSelectGrid.setWidthFull();
        setGridSampleData(multiSelectGrid);
        getContent().add(layoutRow);
        layoutRow.add(layoutRow2);
        layoutRow2.add(buttonSecondary);
        layoutRow2.add(buttonSecondary2);
        layoutRow2.add(buttonSecondary3);
        getContent().add(layoutColumn2);
        layoutColumn2.add(multiSelectGrid);
    }

    private void setGridSampleData(Grid grid) {
        grid.setItems(query -> samplePersonService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
    }

    @Autowired()
    private SamplePersonService samplePersonService;
}
