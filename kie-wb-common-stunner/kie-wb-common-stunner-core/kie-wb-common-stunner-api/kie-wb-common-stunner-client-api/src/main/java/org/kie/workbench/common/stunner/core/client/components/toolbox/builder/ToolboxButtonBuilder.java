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

package org.kie.workbench.common.stunner.core.client.components.toolbox.builder;

import org.kie.workbench.common.stunner.core.client.components.toolbox.ToolboxButton;
import org.kie.workbench.common.stunner.core.client.components.toolbox.event.ToolboxButtonEventHandler;

public interface ToolboxButtonBuilder<I> {

    ToolboxButtonBuilder<I> setIcon( I icon );

    ToolboxButtonBuilder<I> setHoverAnimation( ToolboxButton.HoverAnimation animation );

    ToolboxButtonBuilder<I> setClickHandler( ToolboxButtonEventHandler handler );

    ToolboxButtonBuilder<I> setMouseDownHandler( ToolboxButtonEventHandler handler );

    ToolboxButtonBuilder<I> setMouseEnterHandler( ToolboxButtonEventHandler handler );

    ToolboxButtonBuilder<I> setMouseExitHandler( ToolboxButtonEventHandler handler );

    ToolboxButton<I> build();

}
