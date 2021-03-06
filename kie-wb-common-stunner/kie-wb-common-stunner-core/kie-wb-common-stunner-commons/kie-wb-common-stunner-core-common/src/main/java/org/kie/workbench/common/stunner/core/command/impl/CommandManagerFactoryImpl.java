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

package org.kie.workbench.common.stunner.core.command.impl;

import org.kie.workbench.common.stunner.core.command.CommandManager;
import org.kie.workbench.common.stunner.core.command.CommandManagerFactory;
import org.kie.workbench.common.stunner.core.command.batch.BatchCommandManager;
import org.kie.workbench.common.stunner.core.command.stack.StackCommandManager;
import org.kie.workbench.common.stunner.core.registry.RegistryFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CommandManagerFactoryImpl implements CommandManagerFactory {

    private final RegistryFactory registryFactory;

    protected CommandManagerFactoryImpl() {
        this( null );
    }

    @Inject
    public CommandManagerFactoryImpl( final RegistryFactory registryFactory ) {
        this.registryFactory = registryFactory;
    }

    @Override
    public <C, V> CommandManager<C, V> newCommandManager() {
        return new CommandManagerImpl<C, V>();
    }

    @Override
    public <C, V> BatchCommandManager<C, V> newBatchCommandManager() {
        return new BatchCommandManagerImpl<C, V>( this );
    }

    @Override
    public <C, V> StackCommandManager<C, V> newStackCommandManagerFor( final BatchCommandManager<C, V> commandManager ) {
        return new StackCommandManagerImpl<C, V>( registryFactory, commandManager );
    }
}
