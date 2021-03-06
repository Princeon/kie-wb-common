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

package org.kie.workbench.common.forms.dynamic.backend.server.impl;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.kie.workbench.common.forms.dynamic.model.config.SelectorDataProvider;
import org.kie.workbench.common.forms.dynamic.service.AbstractSelectorDataProviderManager;

@Dependent
public class BackendSelectorDataProviderManager extends AbstractSelectorDataProviderManager {

    public static final String PREFFIX = "remote";

    @Inject
    public BackendSelectorDataProviderManager( Instance<SelectorDataProvider> providers ) {
        for ( SelectorDataProvider provider : providers ) {
            registerProvider( provider );
        }
    }

    @Override
    public String getPreffix() {
        return PREFFIX;
    }
}
