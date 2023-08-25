package ua.vg.at.core.views.endpoints;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ua.vg.at.core.data.entity.SamplePerson;
import ua.vg.at.core.data.service.SamplePersonService;
import ua.vg.at.core.views.MainLayout;

@PageTitle("Endpoints")
@Route(value = "endpoints", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class EndpointsView extends Composite<VerticalLayout> {

    public EndpointsView() {
        Grid basicGrid = new Grid(SamplePerson.class);
        getContent().setHeightFull();
        getContent().setWidthFull();
        setGridSampleData(basicGrid);
        getContent().add(basicGrid);
    }

    private void setGridSampleData(Grid grid) {
        grid.setItems(query -> samplePersonService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
    }

    @Autowired()
    private SamplePersonService samplePersonService;
}
