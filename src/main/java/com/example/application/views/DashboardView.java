package com.example.application.views;

import com.example.application.services.CrmService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "dashboard", layout = MainLayout.class) // <1>
@PageTitle("Dashboard | Vaadin CRM")
public class DashboardView extends VerticalLayout {
    private final CrmService service;

    public DashboardView(CrmService service) { // <2>
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER); // <3>

        add(getContactStats(), getCompaniesChart());
    }

    private Component getContactStats() {
        Span stats = new Span(service.countContacts() + " contacts"); // <4>
        stats.addClassNames(
            LumoUtility.FontSize.XLARGE,
            LumoUtility.Margin.Top.MEDIUM);
        return stats;
    }

    private Chart getCompaniesChart() {
        Chart chart = new Chart(ChartType.PIE);

        DataSeries dataSeries = new DataSeries();
        service.findAllCompanies().forEach(company ->
            dataSeries.add(new DataSeriesItem(company.getName(), company.getEmployeeCount()))); // <5>
        chart.getConfiguration().setSeries(dataSeries);
        return chart;
    }
}
