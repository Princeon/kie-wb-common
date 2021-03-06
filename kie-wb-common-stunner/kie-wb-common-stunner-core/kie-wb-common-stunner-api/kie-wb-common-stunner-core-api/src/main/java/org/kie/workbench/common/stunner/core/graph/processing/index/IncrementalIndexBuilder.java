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

package org.kie.workbench.common.stunner.core.graph.processing.index;

import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;

public interface IncrementalIndexBuilder<G extends Graph<?, N>, N extends Node, E extends Edge, I extends Index<N, E>>
        extends IndexBuilder<G, N, E, I> {

    /**
     * Adds a node into the given index.
     */
    IncrementalIndexBuilder<G, N, E, I> addNode( I index, N node );

    /**
     * Removes a node from the given index.
     */
    IncrementalIndexBuilder<G, N, E, I> removeNode( I index, N node );

    /**
     * Adds an edge into the given index.
     */
    IncrementalIndexBuilder<G, N, E, I> addEdge( I index, E edge );

    /**
     * Removes an edge from the given index.
     */
    IncrementalIndexBuilder<G, N, E, I> removeEdge( I index, E edge );

    /**
     * Clears an index.
     */
    IncrementalIndexBuilder<G, N, E, I> clear( I index );

}
