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

package org.kie.workbench.common.screens.datasource.management.backend.integration.wildfly;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import static org.jboss.as.controller.client.helpers.ClientConstants.*;

/**
 * Helper client that enables the generic deployment of contents on a Wildfy/EAP server.
 */
public class WildflyDeploymentClient
        extends WildflyBaseClient {

    /**
     *
     * @param deploymentName Unique identifier of the deployment. Must be unique across all deployments.
     *
     * @param runtimeName Name by which the deployment should be known within a server's runtime. This would be
     * equivalent to the file name of a deployment file, and would form the basis for such things as default
     * Java Enterprise Edition application and module names.
     * This would typically be the same as 'name', but in some cases users may wish to have two deployments with
     * the same 'runtime-name' (e.g. two versions of "foo.war") both available in the deployment content repository,
     * in which case the deployments would need to have distinct 'name' values but would have the same 'runtime-name'.
     *
     * @param content Content that comprise the deployment.
     *
     * @param enabled Boolean indicating whether the deployment content is currently deployed in the runtime
     * (or should be deployed in the runtime the next time the server starts.)
     *
     * @throws Exception
     */
    public void deployContent( String deploymentName, String runtimeName, byte[] content, boolean enabled ) throws Exception {

        ModelControllerClient client = null;
        ModelNode response = null;

        try {
            client = createControllerClient();

            ModelNode operation = new ModelNode( );
            operation.get( OP ).set( ADD );
            operation.get( OP_ADDR ).add( DEPLOYMENT, deploymentName );

            List<ModelNode> contentList = new ArrayList<ModelNode>();
            ModelNode contentNode = new ModelNode();
            contentNode.set( "bytes", content );
            contentList.add( contentNode );

            operation.get( "name" ).set( deploymentName );
            operation.get( "content" ).set( contentList );
            operation.get( "enabled" ).set( enabled );
            operation.get( "runtime-name" ).set( runtimeName );

            response = client.execute( operation );

        } finally {
            safeClose( client );
            checkResponse( response );
        }
    }

    /**
     *
     * @param deploymentName Unique identifier of the deployment to be enabled/disabled.
     *
     * @param enabled true if the deployment should be enabled, false if it should be disabled.
     *
     * @throws Exception
     */
    public void enableDeployment( String deploymentName, boolean enabled ) throws Exception {

        ModelControllerClient client = null;
        ModelNode response = null;

        try {
            client = createControllerClient();

            ModelNode operation = new ModelNode( );
            if ( enabled ) {
                operation.get( OP ).set( DEPLOYMENT_DEPLOY_OPERATION );
            } else {
                operation.get( OP ).set( DEPLOYMENT_UNDEPLOY_OPERATION );
            }
            operation.get( OP_ADDR ).add( DEPLOYMENT, deploymentName );

            response = client.execute( operation );
        } finally {
            safeClose( client );
            checkResponse( response );
        }
    }

    /**
     *
     * @param deploymentName Unique identifier of the deployment to be removed.
     *
     * @throws Exception
     */
    public void removeDeployment( String deploymentName ) throws Exception {
        ModelControllerClient client = null;
        ModelNode response = null;

        try {
            client = createControllerClient();

            ModelNode operation = new ModelNode( );
            operation.get( OP ).set( DEPLOYMENT_REMOVE_OPERATION );
            operation.get( OP_ADDR ).add( DEPLOYMENT, deploymentName );
            operation.get( "name" ).set( deploymentName );
            response = client.execute( operation );

        } finally {
            safeClose( client );
            checkResponse( response );
        }
    }

}
