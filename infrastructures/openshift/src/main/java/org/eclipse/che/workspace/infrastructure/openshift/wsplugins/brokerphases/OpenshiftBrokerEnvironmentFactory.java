/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.wsplugins.brokerphases;

import static java.util.Collections.singletonMap;

import com.google.common.annotations.Beta;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.provision.env.AgentAuthEnableEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MachineTokenEnvVarProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.KubernetesBrokerEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/**
 * Extends {@link KubernetesBrokerEnvironmentFactory} to be used in the openshift infrastructure.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class OpenshiftBrokerEnvironmentFactory
    extends BrokerEnvironmentFactory<OpenShiftEnvironment> {

  @Inject
  public OpenshiftBrokerEnvironmentFactory(
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      @Named("che.workspace.plugin_broker.pull_policy") String brokerPullPolicy,
      AgentAuthEnableEnvVarProvider authEnableEnvVarProvider,
      MachineTokenEnvVarProvider machineTokenEnvVarProvider,
      @Named("che.workspace.plugin_broker.images") Map<String, String> pluginTypeToImage) {
    super(
        cheWebsocketEndpoint,
        brokerPullPolicy,
        authEnableEnvVarProvider,
        machineTokenEnvVarProvider,
        pluginTypeToImage);
  }

  @Override
  protected OpenShiftEnvironment doCreate(BrokersConfigs brokersConfigs) {
    return OpenShiftEnvironment.builder()
        .setConfigMaps(brokersConfigs.configMaps)
        .setMachines(brokersConfigs.machines)
        .setPods(singletonMap(brokersConfigs.pod.getMetadata().getName(), brokersConfigs.pod))
        .build();
  }
}
