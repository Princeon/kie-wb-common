/*
 * Copyright 2015 JBoss Inc
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
package org.kie.workbench.common.screens.explorer.client.widgets;

import org.junit.Before;
import org.junit.Test;
import org.kie.workbench.common.screens.explorer.service.ExplorerService;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mocks.EventSourceMock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ActiveContextOptionsTest {

    private EventSourceMock<ActiveOptionsChangedEvent> changedEvent;
    private ActiveContextOptions options;

    @Before
    public void setUp() throws Exception {
        changedEvent = spy( new EventSourceMock<ActiveOptionsChangedEvent>() {
            @Override public void fire( ActiveOptionsChangedEvent event ) {

            }
        } );

        options = new ActiveContextOptions( new CallerMock<ExplorerService>( mock( ExplorerService.class ) ),
                                            changedEvent );
    }

    @Test
    public void testActivateBusinessView() throws Exception {
        verify( changedEvent, never() ).fire( any( ActiveOptionsChangedEvent.class ) );

        options.activateBusinessView();

        verify( changedEvent ).fire( any( ActiveOptionsChangedEvent.class ) );

        assertTrue( options.isBusinessViewActive() );
        assertFalse( options.isTechnicalViewActive() );
    }

    @Test
    public void testActivateTechView() throws Exception {
        verify( changedEvent, never() ).fire( any( ActiveOptionsChangedEvent.class ) );

        options.activateTechView();

        verify( changedEvent ).fire( any( ActiveOptionsChangedEvent.class ) );

        assertTrue( options.isTechnicalViewActive() );
        assertFalse( options.isBusinessViewActive() );
    }

    @Test
    public void testBreadCrumbNavigation() throws Exception {
        verify( changedEvent, never() ).fire( any( ActiveOptionsChangedEvent.class ) );

        options.activateBreadCrumbNavigation();

        verify( changedEvent ).fire( any( ActiveOptionsChangedEvent.class ) );

        assertTrue( options.isBreadCrumbNavigationVisible() );
        assertFalse( options.isTreeNavigatorVisible() );
    }

    @Test
    public void testTreeViewNavigation() throws Exception {
        verify( changedEvent, never() ).fire( any( ActiveOptionsChangedEvent.class ) );

        options.activateTreeViewNavigation();

        verify( changedEvent ).fire( any( ActiveOptionsChangedEvent.class ) );

        assertTrue( options.isTreeNavigatorVisible() );
        assertFalse( options.isBreadCrumbNavigationVisible() );
    }
}