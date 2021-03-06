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

package org.kie.workbench.common.stunner.core.client.canvas.controls.toolbox.command.builder;

import org.kie.workbench.common.stunner.core.client.ShapeManager;
import org.kie.workbench.common.stunner.core.client.api.ClientDefinitionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.toolbox.command.Context;
import org.kie.workbench.common.stunner.core.client.canvas.event.keyboard.KeyDownEvent;
import org.kie.workbench.common.stunner.core.client.canvas.event.keyboard.KeyboardEvent;
import org.kie.workbench.common.stunner.core.client.components.drag.DragProxyCallback;
import org.kie.workbench.common.stunner.core.client.components.glyph.DefinitionGlyphTooltip;
import org.kie.workbench.common.stunner.core.client.components.glyph.GlyphTooltip;
import org.kie.workbench.common.stunner.core.client.service.ClientFactoryServices;
import org.kie.workbench.common.stunner.core.client.shape.factory.ShapeFactory;
import org.kie.workbench.common.stunner.core.client.shape.view.ShapeGlyph;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.processing.index.bounds.GraphBoundsIndexer;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

public abstract class AbstractElementBuilderCommand<I> extends AbstractBuilderCommand<I> {

    protected ShapeManager shapeManager;
    protected DefinitionGlyphTooltip<?> glyphTooltip;
    protected I iconView;
    protected ShapeFactory factory;

    protected AbstractElementBuilderCommand() {
        this( null, null, null, null, null );
    }

    @Inject
    public AbstractElementBuilderCommand( final ClientDefinitionManager clientDefinitionManager,
                                          final ClientFactoryServices clientFactoryServices,
                                          final ShapeManager shapeManager,
                                          final DefinitionGlyphTooltip<?> glyphTooltip,
                                          final GraphBoundsIndexer graphBoundsIndexer ) {
        super( clientDefinitionManager, clientFactoryServices, graphBoundsIndexer );
        this.shapeManager = shapeManager;
        this.glyphTooltip = glyphTooltip;
    }

    protected abstract String getGlyphDefinitionId();

    @Override
    public void destroy() {
        super.destroy();
        this.shapeManager = null;
        this.glyphTooltip = null;
        this.factory = null;
        this.iconView = null;

    }

    @Override
    @SuppressWarnings( "unchecked" )
    public I getIcon( final double width,
                      final double height ) {
        if ( null == iconView ) {
            final ShapeFactory factory = getFactory();
            final ShapeGlyph<I> glyph = factory.glyph( getGlyphDefinitionId(), width, height );
            this.iconView = glyph.getGroup();

        }
        return iconView;
    }

    @Override
    public void mouseEnter( final Context<AbstractCanvasHandler> context,
                            final Element element ) {
        super.mouseEnter( context, element );
        if ( null != getFactory() ) {
            glyphTooltip
                    .showTooltip( getGlyphDefinitionId(),
                            context.getClientX() + 20,
                            context.getClientY(),
                            GlyphTooltip.Direction.WEST );

        }

    }

    @Override
    public void mouseExit( final Context<AbstractCanvasHandler> context,
                           final Element element ) {
        super.mouseExit( context, element );
        glyphTooltip.hide();

    }

    protected ShapeFactory getFactory() {
        if ( null == factory ) {
            factory = shapeManager.getFactory( getGlyphDefinitionId() );

        }
        return factory;
    }

    @Override
    protected DragProxyCallback getDragProxyCallback( final Context<AbstractCanvasHandler> context,
                                                      final Element element,
                                                      final Element item ) {
        return new DragProxyCallback() {

            @Override
            public void onStart( final int x1,
                                 final int y1 ) {
                AbstractElementBuilderCommand.this.onStart( context, element, item, x1, y1 );

            }

            @Override
            public void onMove( final int x1,
                                final int y1 ) {
                AbstractElementBuilderCommand.this.onMove( context, element, item, x1, y1 );

            }

            @Override
            public void onComplete( final int x1,
                                    final int y1 ) {
                AbstractElementBuilderCommand.this.onComplete( context, element, item, x1, y1 );

            }
        };

    }

    protected void clearDragProxy() {
        getDragProxyFactory().clear();

    }

    void onKeyDownEvent( @Observes KeyDownEvent keyDownEvent ) {
        checkNotNull( "keyDownEvent", keyDownEvent );
        final KeyboardEvent.Key key = keyDownEvent.getKey();
        if ( null != key && KeyboardEvent.Key.ESC.equals( key ) ) {
            clearDragProxy();

        }

    }

}
