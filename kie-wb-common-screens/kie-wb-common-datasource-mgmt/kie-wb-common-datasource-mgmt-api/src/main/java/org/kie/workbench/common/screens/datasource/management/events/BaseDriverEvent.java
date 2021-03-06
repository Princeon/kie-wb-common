/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.screens.datasource.management.events;

import org.guvnor.common.services.project.model.Project;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;

public abstract class BaseDriverEvent {

    private DriverDef driverDef;

    private Project project;

    private String sessionId;

    private String identity;

    protected BaseDriverEvent() {
    }

    protected BaseDriverEvent( final DriverDef driverDef,
            final Project project,
            final String sessionId,
            final String identity ) {
        this.driverDef = driverDef;
        this.project = project;
        this.sessionId = sessionId;
        this.identity = identity;
    }

    protected BaseDriverEvent( final DriverDef driverDef,
            final String sessionId,
            final String identity ) {
        this.driverDef = driverDef;
        this.sessionId = sessionId;
        this.identity = identity;
    }

    public DriverDef getDriverDef() {
        return driverDef;
    }

    public Project getProject() {
        return project;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getIdentity() {
        return identity;
    }

    public boolean isGlobal() {
        return project == null;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        BaseDriverEvent that = ( BaseDriverEvent ) o;

        if ( driverDef != null ? !driverDef.equals( that.driverDef ) : that.driverDef != null ) {
            return false;
        }
        if ( project != null ? !project.equals( that.project ) : that.project != null ) {
            return false;
        }
        if ( sessionId != null ? !sessionId.equals( that.sessionId ) : that.sessionId != null ) {
            return false;
        }
        return identity != null ? identity.equals( that.identity ) : that.identity == null;

    }

    @Override
    public int hashCode() {
        int result = driverDef != null ? driverDef.hashCode() : 0;
        result = ~~result;
        result = 31 * result + ( project != null ? project.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( sessionId != null ? sessionId.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( identity != null ? identity.hashCode() : 0 );
        result = ~~result;
        return result;
    }
}
