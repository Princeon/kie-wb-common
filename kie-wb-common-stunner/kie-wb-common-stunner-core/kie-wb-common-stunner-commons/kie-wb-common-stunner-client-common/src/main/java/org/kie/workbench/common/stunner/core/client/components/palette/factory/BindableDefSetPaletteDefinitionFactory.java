/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.core.client.components.palette.factory;

import org.kie.workbench.common.stunner.core.client.ShapeManager;
import org.kie.workbench.common.stunner.core.client.components.palette.model.definition.DefinitionSetPaletteBuilder;
import org.kie.workbench.common.stunner.core.definition.adapter.binding.BindableAdapterUtils;

public abstract class BindableDefSetPaletteDefinitionFactory
        extends BindablePaletteDefinitionFactory<DefinitionSetPaletteBuilder>
        implements DefSetPaletteDefinitionFactory {

    public BindableDefSetPaletteDefinitionFactory( final ShapeManager shapeManager,
                                                   final DefinitionSetPaletteBuilder paletteBuilder ) {
        super( shapeManager, paletteBuilder );
    }

    protected abstract String getCategoryTitle( String id );

    protected abstract String getCategoryDescription( String id );

    protected abstract String getMorphGroupTitle( String morphBaseId, Object definition );

    protected abstract String getMorphGroupDescription( String morphBaseId, Object definition );

    @Override
    protected DefinitionSetPaletteBuilder newBuilder() {
        paletteBuilder.setCategoryProvider( new DefinitionSetPaletteBuilder.PaletteCategoryProvider() {

            @Override
            public String getTitle( final String id ) {
                return getCategoryTitle( id );

            }

            @Override
            public String getDescription( final String id ) {
                return getCategoryDescription( id );

            }

        } );
        paletteBuilder.setMorphGroupProvider( new DefinitionSetPaletteBuilder.PaletteMorphGroupProvider() {

            @Override
            public String getTitle( final String morphBaseId,
                                    final Object definition ) {
                return getMorphGroupTitle( morphBaseId, definition );

            }

            @Override
            public String getDescription( final String morphBaseId,
                                          final Object definition ) {
                return getMorphGroupDescription( morphBaseId, definition );

            }

        } );
        configureBuilder();
        return paletteBuilder;
    }

    protected void configureBuilder() {
    }

    protected void exclude( final Class<?> type ) {
        final String id = BindableAdapterUtils.getDefinitionId( type );
        paletteBuilder.exclude( id );
    }

}
