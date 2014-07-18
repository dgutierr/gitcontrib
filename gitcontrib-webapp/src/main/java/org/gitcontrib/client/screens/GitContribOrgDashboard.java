/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gitcontrib.client.screens;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.kpi.KPI;
import org.dashbuilder.kpi.KPIFactory;
import org.dashbuilder.renderer.table.client.TableRenderer;

import static org.gitcontrib.client.screens.GitContribDataSetConstants.*;
import static org.dashbuilder.dataset.date.DayOfWeek.*;
import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;

/**
 * A GIT Contrib dashboard showing KPIs for a given GIT organization.
 */
public class GitContribOrgDashboard extends Composite {

    interface SalesDashboardBinder extends UiBinder<Widget, GitContribOrgDashboard>{}
    private static final SalesDashboardBinder uiBinder = GWT.create(SalesDashboardBinder.class);

    @UiField(provided = true)
    Displayer areaChartByDate;

    @UiField(provided = true)
    Displayer pieChartYears;

    @UiField(provided = true)
    Displayer pieChartQuarters;

    @UiField(provided = true)
    Displayer barChartDayOfWeek;

    @UiField(provided = true)
    Displayer pieChartByModule;

    @UiField(provided = true)
    Displayer tableAll;

    public Displayer lookupViewer(KPI kpi) {
        return DisplayerHelper.lookupDisplayer(kpi.getDataSetRef(), kpi.getDisplayerSettings());
    }

    protected String organization = null;

    public GitContribOrgDashboard(String organization) {
        this.organization = organization;

        // Create the chart definitions

        areaChartByDate = lookupViewer(KPIFactory.newAreaChartKPI()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .group(COLUMN_DATE, 80, MONTH)
                .count("commits")
                .title("#Commits evolution")
                .titleVisible(true)
                .width(850).height(200)
                .margins(10, 70, 60, 0)
                .column("Date")
                .column("#Commits")
                .filterOn(false, true, true)
                .buildKPI());

        pieChartByModule = lookupViewer(KPIFactory.newPieChartKPI()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .group(COLUMN_MODULE)
                .count("#commits")
                .title("Modules")
                .titleVisible(true)
                .width(300).height(150)
                .margins(0, 0, 0, 100)
                .column("Module")
                .column("Number of commits")
                .filterOn(false, true, true)
                .buildKPI());

        pieChartYears = lookupViewer(KPIFactory.newPieChartKPI()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .group(COLUMN_DATE, YEAR)
                .count("occurrences")
                .sort(COLUMN_DATE, ASCENDING)
                .title("Years")
                .titleVisible(true)
                .width(200).height(150)
                .margins(0, 0, 0, 0)
                .filterOn(false, true, false)
                .buildKPI());

        pieChartQuarters = lookupViewer(KPIFactory.newPieChartKPI()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .group(COLUMN_DATE).fixed(QUARTER)
                .count("occurrences")
                .title("Quarters")
                .titleVisible(true)
                .width(200).height(150)
                .margins(0, 0, 0, 0)
                .filterOn(false, true, false)
                .buildKPI());

        barChartDayOfWeek = lookupViewer(KPIFactory.newBarChartKPI()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .group(COLUMN_DATE).fixed(DAY_OF_WEEK).firstDay(SUNDAY)
                .count("occurrences")
                .title("Day of week")
                .titleVisible(true)
                .width(200).height(150)
                .margins(0, 40, 70, 0)
                .horizontal()
                .filterOn(false, true, true)
                .buildKPI());

        tableAll = lookupViewer(KPIFactory.newTableKPI()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .title("Commits")
                .titleVisible(false)
                .tablePageSize(8)
                .tableOrderEnabled(true)
                .tableOrderDefault(COLUMN_ORG, DESCENDING)
                .column(COLUMN_MODULE, "Module")
                .column(COLUMN_REPO, "Repository")
                .column(COLUMN_AUTHOR, "Author")
                .column(COLUMN_DATE, "Date")
                //.column(COLUMN_MSG, "Message")
                .filterOn(true, true, true)
                .renderer(TableRenderer.UUID)
                .buildKPI());

        // Make that charts interact among them
        DisplayerCoordinator viewerCoordinator = new DisplayerCoordinator();
        viewerCoordinator.addDisplayer(areaChartByDate);
        viewerCoordinator.addDisplayer(pieChartYears);
        viewerCoordinator.addDisplayer(pieChartQuarters);
        viewerCoordinator.addDisplayer(barChartDayOfWeek);
        viewerCoordinator.addDisplayer(pieChartByModule);
        viewerCoordinator.addDisplayer(tableAll);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        viewerCoordinator.drawAll();
    }
}
