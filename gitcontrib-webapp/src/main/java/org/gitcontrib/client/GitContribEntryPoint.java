/*
 * Copyright 2014 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gitcontrib.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.dashbuilder.dataset.DataSetLookupService;
import org.dashbuilder.dataset.client.DataSetLookupClient;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.RendererLibLocator;
import org.dashbuilder.renderer.google.client.GoogleRenderer;
import org.dashbuilder.renderer.selector.client.SelectorRenderer;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.events.ApplicationReadyEvent;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBar;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import static org.uberfire.workbench.model.menu.MenuFactory.*;

/**
 * GWT's Entry-point for the GitContrib App
 */
@EntryPoint
public class GitContribEntryPoint {

    @Inject
    private SyncBeanManager manager;

    @Inject
    private WorkbenchMenuBar menubar;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private ActivityManager activityManager;

    @Inject
    private ClientMessageBus bus;

    @Inject
    private RendererLibLocator rendererLibLocator;

    @Inject
    private DataSetLookupClient dataSetLookupClient;

    @Inject
    private Caller<DataSetLookupService> dataSetLookupService;

    @PostConstruct
    public void startApp() {
        initDashbuilder();
        hideLoadingPopup();
    }

    @PostConstruct
    private void initDashbuilder() {
        // Enable the data set lookup backend service so that the DataSetLookupClient is able to send requests
        // not only to the ClientDataSetManager but also to the remote DataSetLookupService.
        dataSetLookupClient.setLookupService(dataSetLookupService);

        // Set the default renderer lib for each displayer type.
        rendererLibLocator.setDefaultRenderer( DisplayerType.BARCHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer( DisplayerType.PIECHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer( DisplayerType.AREACHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer( DisplayerType.LINECHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer( DisplayerType.BUBBLECHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer( DisplayerType.METERCHART, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer( DisplayerType.MAP, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer( DisplayerType.TABLE, GoogleRenderer.UUID);
        rendererLibLocator.setDefaultRenderer( DisplayerType.SELECTOR, SelectorRenderer.UUID);

        // Enable the ability to push and handle on client data sets greater than 2 Mb
        dataSetLookupClient.setPushRemoteDataSetEnabled(true);
        dataSetLookupClient.setPushRemoteDataSetMaxSize(2024);
    }

    private void setupMenu( @Observes final ApplicationReadyEvent event ) {
        final PerspectiveActivity defaultPerspective = getDefaultPerspectiveActivity();

        final Menus menus =
                newTopLevelMenu("KIE Contributors").respondsWith(new Command() {
                    public void execute() {
                        if (defaultPerspective != null) {
                            placeManager.goTo(new DefaultPlaceRequest(defaultPerspective.getIdentifier()));
                        } else {
                            Window.alert("Default perspective not found.");
                        }
                    }
                }).endMenu().
                build();

        menubar.addMenus( menus );
    }

    private PerspectiveActivity getDefaultPerspectiveActivity() {
        PerspectiveActivity defaultPerspective = null;
        final Collection<IOCBeanDef<PerspectiveActivity>> perspectives = manager.lookupBeans( PerspectiveActivity.class );
        final Iterator<IOCBeanDef<PerspectiveActivity>> perspectivesIterator = perspectives.iterator();

        while ( perspectivesIterator.hasNext() ) {
            final IOCBeanDef<PerspectiveActivity> perspective = perspectivesIterator.next();
            final PerspectiveActivity instance = perspective.getInstance();
            if ( instance.isDefault() ) {
                defaultPerspective = instance;
                break;
            } else {
                manager.destroyBean( instance );
            }
        }
        return defaultPerspective;
    }

    //Fade out the "Loading application" pop-up
    private void hideLoadingPopup() {
        final Element e = RootPanel.get( "loading" ).getElement();

        new Animation() {

            @Override
            protected void onUpdate( double progress ) {
                e.getStyle().setOpacity( 1.0 - progress );
            }

            @Override
            protected void onComplete() {
                e.getStyle().setVisibility( Style.Visibility.HIDDEN );
            }
        }.run( 500 );
    }
}