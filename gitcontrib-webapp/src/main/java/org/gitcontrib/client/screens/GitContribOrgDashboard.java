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
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;

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
    Displayer bubbleChartByModule;

    @UiField(provided = true)
    Displayer tableAll;

    @UiField(provided = true)
    Displayer tableTopAuthors;

    @UiField(provided = true)
    Displayer repoSelector;

    @UiField(provided = true)
    Displayer moduleSelector;

    @UiField(provided = true)
    Displayer authorSelector;

    @UiField(provided = true)
    Displayer topAuthorSelector;

    protected String organization = null;

    public GitContribOrgDashboard(String organization) {
        this.organization = organization;

        // Create the chart definitions

        areaChartByDate = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newAreaChartSettings()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .group(COLUMN_DATE, 80, MONTH)
                .count("commits")
                .title("#Commits evolution")
                .titleVisible(true)
                .width(600).height(200)
                .margins(10, 60, 70, 0)
                .column("Date")
                .column("#Commits")
                .filterOff(true)
                .buildSettings());

        bubbleChartByModule = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBubbleChartSettings()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .group(COLUMN_MODULE)
                .distinct(COLUMN_REPO, "#repositories")
                .distinct(COLUMN_AUTHOR, "#contributors")
                .count("#commits")
                .title("Commits per module")
                .titleVisible(true)
                .width(400).height(220)
                .margins(10, 50, 70, 0)
                .column(COLUMN_MODULE, "Module")
                .column("#repositories", "#repositories")
                .column("#commits", "Number of commits")
                .column(COLUMN_MODULE, "Module")
                .column("#contributors", "#contributors")
                .filterOn(false, true, true)
                .buildSettings());

        pieChartYears = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newPieChartSettings()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .group(COLUMN_DATE, YEAR)
                .count("occurrences")
                .sort(COLUMN_DATE, ASCENDING)
                .title("Years")
                .titleVisible(false)
                .width(230).height(170)
                .margins(0, 0, 10, 5)
                .filterOn(false, true, false)
                .buildSettings());

        pieChartQuarters = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newPieChartSettings()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .group(COLUMN_DATE).fixed(QUARTER)
                .count("occurrences")
                .title("Quarters")
                .titleVisible(false)
                .width(230).height(170)
                .margins(0, 0, 5, 5)
                .filterOn(false, true, false)
                .buildSettings());

        barChartDayOfWeek = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .group(COLUMN_DATE).fixed(DAY_OF_WEEK).firstDay(SUNDAY)
                .count("occurrences")
                .title("Day of week")
                .titleVisible(false)
                .width(230).height(170)
                .margins(0, 10, 70, 0)
                .horizontal()
                .filterOn(false, true, true)
                .buildSettings());

        tableAll = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .title(organization + " Commits")
                .titleVisible(false)
                .tablePageSize(8)
                .tableWidth(1000)
                .tableOrderEnabled(true)
                .column(COLUMN_AUTHOR, "Author")
                .column(COLUMN_MSG, "Commit")
                .filterOn(true, true, true)
                .buildSettings());

        tableTopAuthors = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_ORG).select(organization)
                .group(COLUMN_AUTHOR)
                .count("#Commits")
                .sort("#Commits", DESCENDING)
                .title("Top contributors")
                .titleVisible(false)
                .tablePageSize(8)
                .tableWidth(400)
                .tableOrderEnabled(false)
                .column(COLUMN_AUTHOR, "Author")
                .column("#Commits", "#Commits")
                .filterOn(false, true, true)
                .buildSettings());

        // Create the selectors

        repoSelector = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newSelectorSettings()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_REPO)
                .count("#Commits")
                .sort(COLUMN_REPO, ASCENDING)
                .title("Repository Selector")
                .column("Repository")
                .column("#Commits")
                .filterOn(false, true, true)
                .buildSettings());

        moduleSelector = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newSelectorSettings()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_MODULE)
                .count("#Commits")
                .title("Module Selector")
                .sort(COLUMN_MODULE, ASCENDING)
                .column("Module")
                .column("#Commits")
                .filterOn(false, true, true)
                .buildSettings());

        authorSelector = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newSelectorSettings()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_AUTHOR)
                .count("#Commits")
                .sort(COLUMN_AUTHOR, ASCENDING)
                .title("Author Selector")
                .column("Author")
                .column("#Commits")
                .filterOn(false, true, true)
                .buildSettings());

        topAuthorSelector = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newSelectorSettings()
                .dataset(GITCONTRIB_ALL)
                .group(COLUMN_AUTHOR)
                .count("#Commits")
                .sort("#Commits", DESCENDING)
                .title("Top Contributor Selector")
                .column("Top Contributor")
                .column("#Commits")
                .filterOn(false, true, true)
                .buildSettings());

        // Make the displayers interact among them
        DisplayerCoordinator viewerCoordinator = new DisplayerCoordinator();
        viewerCoordinator.addDisplayer(areaChartByDate);
        viewerCoordinator.addDisplayer(pieChartYears);
        viewerCoordinator.addDisplayer(pieChartQuarters);
        viewerCoordinator.addDisplayer(barChartDayOfWeek);
        viewerCoordinator.addDisplayer(bubbleChartByModule);
        viewerCoordinator.addDisplayer(tableAll);
        viewerCoordinator.addDisplayer(tableTopAuthors);
        viewerCoordinator.addDisplayer(repoSelector);
        viewerCoordinator.addDisplayer(moduleSelector);
        viewerCoordinator.addDisplayer(authorSelector);
        viewerCoordinator.addDisplayer(topAuthorSelector);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        viewerCoordinator.drawAll();
    }
}
