/**
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.whirr.cm.handler;

import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.whirr.service.ClusterActionEvent;

import com.cloudera.whirr.cm.CmServerClusterInstance;
import com.cloudera.whirr.cm.server.CmServerException;
import com.cloudera.whirr.cm.server.CmServerServiceBuilder;

public class CmNodeHandler extends BaseHandlerCm {

  public static final String ROLE = "cm-node";

  @Override
  public String getRole() {
    return ROLE;
  }

  @Override
  protected String getInstanceId() {
    return super.getInstanceId() + "-" + (CmServerClusterInstance.getCluster().getNodes().size() + 1);
  }

  @Override
  protected void beforeBootstrap(ClusterActionEvent event) throws IOException, InterruptedException {
    super.beforeBootstrap(event);
    try {
      CmServerClusterInstance.getCluster().addNode(new CmServerServiceBuilder().host(getInstanceId()).build());
    } catch (CmServerException e) {
      throw new IOException("Unexpected error building cluster", e);
    }
    addStatement(event, call("install_cm"));
  }

  @Override
  protected void beforeConfigure(ClusterActionEvent event) throws IOException, InterruptedException {
    super.beforeConfigure(event);
    @SuppressWarnings("unchecked")
    List<String> clusterPorts = CmServerClusterInstance.getConfiguration(event.getClusterSpec()).getList(
        ROLE + CONFIG_WHIRR_INTERNAL_PORTS_SUFFIX);
    handleFirewallRules(event, Collections.<String> emptyList(), clusterPorts);
  }

}
