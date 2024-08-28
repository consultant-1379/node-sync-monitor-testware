/*------------------------------------------------------------------------------

 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.nodesyncmonitor.teststeps;

import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.NODES_TO_ADD;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.tools.cli.TafCliToolShell;
import com.ericsson.de.tools.cli.CliCommandResult;
import com.ericsson.oss.testware.enmbase.data.NetworkNode;
import com.ericsson.oss.testware.hostconfigurator.HostConfigurator;
import com.ericsson.oss.testware.remoteexecution.operators.PibConnectorImpl;

public class NodeSyncMonitorTestSteps {

    @Inject
    private PibConnectorImpl pibConnector;

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSyncMonitorTestSteps.class);

    private static final String PIB_COMMANDS_SCRIPTPATH = "/ericsson/pib-scripts/etc/config.py";
    private static final String PIB_COMMANDS_SCRIPTPATH_CENM = "/opt/ericsson/PlatformIntegrationBridge/etc/config.py";
    private static final String SPACE = " ";
    private static final String PORT_8080 = ":8080";
    private static final String APP_SERVER_ADDRESS = "--app_server_address=";
    private static final String SERVICE_IDENTIFIER = "--service_identifier=";
    private static final String NAME = "--name=";
    private static final String VALUE = "--value=";
    private static final String SERVICE_NAME = "node-sync-monitor";
    private static final String PIB_PARAM = "cmNodeSyncMonitorFeature";
    private static final String UPDATE = "update";
    private static final String ON = "on";
    private static final String OFF = "off";
    private static final String SUDO = "sudo";
    private static final String GREP_KPISERV = " | grep kpiserv | tail -1 | awk '{print $1}'";

    /**
     * Test step which sets the ids of nodes under test to system properties, which are then used for input data preparation.
     *
     * @param node
     *            An object representing the node under test.
     */
    @TestStep(id = StepIds.SET_NODE_IDS)
    public void setNodeIds(@Input(NODES_TO_ADD) final NetworkNode node) {
        if (DataHandler.getAttribute("rbsNodeId") == null) {
            DataHandler.setAttribute("rbsNodeId", node.getNetworkElementId());
        }
    }

    @TestStep(id = StepIds.ENABLE_CM_NODE_SYNC_MONITOR_FEATURE)
    public void enableCmNodeSyncMonitorFeature() throws Exception {
        updateCmNodeSyncMonitorFeature(ON);
    }

    @TestStep(id = StepIds.DISABLE_CM_NODE_SYNC_MONITOR_FEATURE)
    public void disableCmNodeSyncMonitorFeature() throws Exception {
        updateCmNodeSyncMonitorFeature(OFF);
    }

    /**
     * Updates the 'cmNodeSyncMonitorFeature' configuration parameter using PIB.
     * <p>
     * Depending on the SUT the PIB command will be executed from one of the following servers:
     * <ul>
     * <li>For pENM the command is executed from the LMS of the deployment</li>
     * <li>For cENM (CaaS using Kubernetes) the command is executed from the director node of the deployment</li>
     * <li>For vENM (IaaS using Openstack) the command is executed from the EMP VM of the deployment</li>
     * </ul>
     * Note that {@code PibConnector#getConnection} will return a connection to the LMS, director node, or EMP VM depending on the SUT.
     */
    private void updateCmNodeSyncMonitorFeature(final String cmNodeSyncMonitorFeatureValue) throws Exception {
        TafCliToolShell toolShell = null;
        try {
            toolShell = pibConnector.getConnection();
            final String command = getPibCommand(cmNodeSyncMonitorFeatureValue);
            final CliCommandResult result = toolShell.execute(command);
            if (!result.isSuccess()) {
                final String errorMsg = String.format("Command [%s] failed with response [%s]", command, result.getOutput());
                LOGGER.error(errorMsg);
                throw new Exception(errorMsg);
            }
        } finally {
            if (toolShell != null) {
                toolShell.close();
            }
        }
    }

    private String getPibCommand(final String cmNodeSyncMonitorFeatureValue) {
        if (HostConfigurator.isCloudEnvironment()) {
            return buildUpdatePibCommandForCEnm(cmNodeSyncMonitorFeatureValue);
        }

        if (HostConfigurator.isVirtualEnvironment()) {
            return buildUpdatePibCommandForVEnm(cmNodeSyncMonitorFeatureValue);
        }

        return buildUpdatePibCommandForPEnm(cmNodeSyncMonitorFeatureValue);
    }

    /*
     * Build PIB command that will be executed from director node of the cENM deployment.
     */
    private String buildUpdatePibCommandForCEnm(final String cmNodeSyncMonitorFeatureValue) {
        final String command = buildPibCommand("localhost", cmNodeSyncMonitorFeatureValue, PIB_COMMANDS_SCRIPTPATH_CENM, false);
        final String namespace = HostConfigurator.getPibHost().getNamespace();
        final String getPodNameCommand = "kubectl get pods -n " + namespace + GREP_KPISERV;

        return "kubectl -n " + namespace + " exec -it $(" + getPodNameCommand + ") -c kpiserv -- " + command;
    }

    /*
     * Build PIB command that will be executed from the EMP VM of the vENM deployment.
     */
    private String buildUpdatePibCommandForVEnm(final String cmNodeSyncMonitorFeatureValue) {
        final String commandToRetrieveHost = "$(sudo consul members" + GREP_KPISERV + ")";

        return buildPibCommand(commandToRetrieveHost, cmNodeSyncMonitorFeatureValue, PIB_COMMANDS_SCRIPTPATH, true);
    }

    /*
     * Build PIB command that will be executed from LMS of the pENM deployoment.
     */
    private String buildUpdatePibCommandForPEnm(final String cmNodeSyncMonitorFeatureValue) {
        final String host = HostConfigurator.getKpiService().getIp();
        return buildPibCommand(host, cmNodeSyncMonitorFeatureValue, PIB_COMMANDS_SCRIPTPATH, true);
    }


    private String buildPibCommand(final String host, final String cmNodeSyncMonitorFeatureValue, final String scriptPath, final boolean isPEnm) {
        return new StringBuilder(isPEnm ? SUDO + SPACE + scriptPath + SPACE + UPDATE : scriptPath + SPACE + UPDATE)
                .append(SPACE + APP_SERVER_ADDRESS + host + PORT_8080)
                .append(SPACE + SERVICE_IDENTIFIER + SERVICE_NAME)
                .append(SPACE + NAME + PIB_PARAM)
                .append(SPACE + VALUE + cmNodeSyncMonitorFeatureValue)
                .toString();
    }

    /**
     * The test step IDs.
     */
    public static final class StepIds {
        public static final String SET_NODE_IDS = "setNodeIds";
        public static final String ENABLE_CM_NODE_SYNC_MONITOR_FEATURE = "enableCmNodeSyncMonitorFeature";
        public static final String DISABLE_CM_NODE_SYNC_MONITOR_FEATURE = "disableCmNodeSyncMonitorFeature";

        private StepIds() {}
    }

}