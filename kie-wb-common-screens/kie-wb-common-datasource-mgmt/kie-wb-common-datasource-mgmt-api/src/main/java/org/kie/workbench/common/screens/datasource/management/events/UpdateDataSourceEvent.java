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
import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;

@Portable
public class UpdateDataSourceEvent extends BaseDataSourceEvent {

    private DataSourceDef originalDataSourceDef;

    public UpdateDataSourceEvent() {
    }

    public UpdateDataSourceEvent( final DataSourceDef originalDataSourceDef,
            final DataSourceDef dataSourceDef,
            final Project project,
            final String sessionId,
            final String identity ) {
        super( dataSourceDef, project, sessionId, identity );
        this.originalDataSourceDef = originalDataSourceDef;
    }

    public UpdateDataSourceEvent( final DataSourceDef dataSourceDef,
            final String sessionId,
            final String identity,
            final DataSourceDef originalDataSourceDef ) {
        super( dataSourceDef, sessionId, identity );
        this.originalDataSourceDef = originalDataSourceDef;
    }

    public DataSourceDef getOriginalDataSourceDef() {
        return originalDataSourceDef;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        if ( !super.equals( o ) ) {
            return false;
        }

        UpdateDataSourceEvent that = ( UpdateDataSourceEvent ) o;

        return originalDataSourceDef != null ? originalDataSourceDef.equals( that.originalDataSourceDef ) : that.originalDataSourceDef == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = ~~result;
        result = 31 * result + ( originalDataSourceDef != null ? originalDataSourceDef.hashCode() : 0 );
        result = ~~result;
        return result;
    }
}
