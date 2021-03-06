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

package org.kie.workbench.common.stunner.backend.service;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.stunner.core.lookup.criteria.Criteria;
import org.kie.workbench.common.stunner.core.lookup.rule.RuleLookupManager;
import org.kie.workbench.common.stunner.core.lookup.rule.RuleLookupRequest;
import org.kie.workbench.common.stunner.core.rule.Rule;

import javax.inject.Inject;

@Service
public class RuleLookupService implements org.kie.workbench.common.stunner.core.remote.RuleLookupService {

    RuleLookupManager ruleLookupManager;

    @Inject
    public RuleLookupService( @Criteria RuleLookupManager ruleLookupManager ) {
        this.ruleLookupManager = ruleLookupManager;
    }

    @Override
    public LookupResponse<Rule> lookup( RuleLookupRequest request ) {
        return ruleLookupManager.lookup( request );
    }
}
