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

package org.kie.workbench.common.stunner.client.widgets.explorer.tree;

import com.ait.lienzo.client.core.shape.Group;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.stunner.core.client.ShapeManager;
import org.kie.workbench.common.stunner.core.client.shape.factory.ShapeFactory;
import org.kie.workbench.common.stunner.core.client.shape.view.ShapeGlyph;
import org.kie.workbench.common.stunner.core.definition.util.DefinitionUtils;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.util.GraphUtils;
import org.uberfire.client.mvp.UberView;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

@Dependent
public class TreeExplorerItem implements IsWidget {

    private static Logger LOGGER = Logger.getLogger( TreeExplorerItem.class.getName() );

    public interface View extends UberView<TreeExplorerItem> {

        View setUUID( String uuid );

        View setName( String name );

        View setGlyph( ShapeGlyph<Group> glyph );

    }

    ShapeManager shapeManager;
    GraphUtils graphUtils;
    DefinitionUtils definitionUtils;
    View view;

    @Inject
    public TreeExplorerItem( final ShapeManager shapeManager,
                             final GraphUtils graphUtils,
                             final DefinitionUtils definitionUtils,
                             final View view ) {
        this.shapeManager = shapeManager;
        this.graphUtils = graphUtils;
        this.definitionUtils = definitionUtils;
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init( this );
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    @SuppressWarnings( "unchecked" )
    public void show( final Element<org.kie.workbench.common.stunner.core.graph.content.view.View> element ) {
        final Object definition = element.getContent().getDefinition();
        final String defId = definitionUtils.getDefinitionManager().adapters().forDefinition().getId( definition );
        final ShapeFactory factory = shapeManager.getFactory( defId );
        view.setUUID( element.getUUID() )
                .setName( getItemText( element ) )
                .setGlyph( factory.glyph( defId, 25, 25 ) );

    }

    private String getItemText( final Element<org.kie.workbench.common.stunner.core.graph.content.view.View> item ) {
        final String name = definitionUtils.getName( item.getContent().getDefinition() );
        return ( name != null ? name : "- No name -" );
    }

}
