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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.services.nodesyncmonitor.dataprovider.NetSimOperatorProvider;
import com.ericsson.oss.testware.enmbase.data.NetworkNode;
import com.ericsson.oss.testware.network.operators.netsim.NetsimOperator;

public class NetSimTestSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetSimTestSteps.class);

    private NetsimOperator netsimOperator;

    /**
     * Test step which restores a backup of a node in NetSim.
     *
     * @param node
     *            An object representing the node under test.
     */
    @TestStep(id = StepIds.RESTORE_NE_STATE_IN_NETSIM)
    public void restoreNeState(@Input(NODES_TO_ADD) final NetworkNode node) {
        netsimOperator = NetSimOperatorProvider.getInstance().getNetsimOperator();
        final NetworkElement networkElement = netsimOperator.getNetworkElement(node.getNetworkElementId());
        final String restoreImagePath =
                String.format("/netsim/netsimdir/%s/allsaved/dbs/%s_%s", networkElement.getSimulationName(), "curr", networkElement.getName());
        LOGGER.info("Restoring state of {} from {}", networkElement.getName(), restoreImagePath);

        networkElement.exec(NetSimCommands.stop());
        networkElement.exec(NetSimCommands.restorenedatabase(restoreImagePath));
        networkElement.exec(NetSimCommands.start());
    }

    /**
     * Test step which stops a node in NetSim.
     *
     * @param node
     *            An object representing the node under test.
     */
    @TestStep(id = StepIds.STOP_NODE_IN_NETSIM)
    public void stopNodeInNetSim(@Input("nodeToStopOrStart") final NetworkNode node) {
        netsimOperator = NetSimOperatorProvider.getInstance().getNetsimOperator();
        final NetworkElement networkElement = netsimOperator.getNetworkElement(node.getNetworkElementId());
        networkElement.exec(NetSimCommands.stop());
    }

    /**
     * Test step which starts a node in NetSim.
     *
     * @param node
     *            An object representing the node under test.
     */
    @TestStep(id = StepIds.START_NODE_IN_NETSIM)
    public void startNodeInNetSim(@Input("nodeToStopOrStart") final NetworkNode node) {
        netsimOperator = NetSimOperatorProvider.getInstance().getNetsimOperator();
        final NetworkElement networkElement = netsimOperator.getNetworkElement(node.getNetworkElementId());
        networkElement.exec(NetSimCommands.start());
    }

    /**
     * The test step IDs.
     */
    public static final class StepIds {
        public static final String RESTORE_NE_STATE_IN_NETSIM = "restoreNeState";
        public static final String STOP_NODE_IN_NETSIM = "stopNode";
        public static final String START_NODE_IN_NETSIM = "startNode";

        private StepIds() {}
    }

}
