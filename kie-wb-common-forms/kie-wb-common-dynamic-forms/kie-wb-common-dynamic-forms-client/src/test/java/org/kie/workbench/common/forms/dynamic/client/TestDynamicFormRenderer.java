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

package org.kie.workbench.common.forms.dynamic.client;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.Caller;
import org.kie.workbench.common.forms.dynamic.service.FormRenderingContextGeneratorService;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.processing.engine.handling.FormHandler;

public class TestDynamicFormRenderer extends DynamicFormRenderer {

    protected Object model;

    protected boolean binded = false;

    public TestDynamicFormRenderer( DynamicFormRendererView view,
                                    Caller<FormRenderingContextGeneratorService> transformerService,
                                    FormHandler formHandler ) {
        super( view, transformerService, formHandler );
    }

    @Override
    protected void doBind( Widget input, FieldDefinition field ) {
    }
}
