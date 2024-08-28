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

package com.ericsson.oss.services.nodesyncmonitor.flows;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.oss.services.nodesyncmonitor.teststeps.NetSimTestSteps.StepIds.RESTORE_NE_STATE_IN_NETSIM;
import static com.ericsson.oss.services.nodesyncmonitor.teststeps.NetSimTestSteps.StepIds.START_NODE_IN_NETSIM;
import static com.ericsson.oss.services.nodesyncmonitor.teststeps.NetSimTestSteps.StepIds.STOP_NODE_IN_NETSIM;
import static com.ericsson.oss.services.nodesyncmonitor.teststeps.NodeSyncMonitorTestSteps.StepIds.DISABLE_CM_NODE_SYNC_MONITOR_FEATURE;
import static com.ericsson.oss.services.nodesyncmonitor.teststeps.NodeSyncMonitorTestSteps.StepIds.ENABLE_CM_NODE_SYNC_MONITOR_FEATURE;
import static com.ericsson.oss.services.nodesyncmonitor.teststeps.NodeSyncMonitorTestSteps.StepIds.SET_NODE_IDS;
import static com.ericsson.oss.testware.cm.cruda.flows.CrudaFlows.DataSources.CMEDIT_ACTION_DATA_SOURCE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.NODES_TO_ADD;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_DELETE;
import static com.ericsson.oss.testware.fm.api.constants.FmCommonDataSources.CLI_COMMANDS_DS;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.oss.services.nodesyncmonitor.teststeps.NetSimTestSteps;
import com.ericsson.oss.services.nodesyncmonitor.teststeps.NodeSyncMonitorTestSteps;
import com.ericsson.oss.testware.cm.cruda.flows.CrudaFlows;
import com.ericsson.oss.testware.nodeintegration.flows.NodeIntegrationFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;

public class SetupTearDownFlows {

    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Inject
    private TestContext context;

    @Inject
    private NodeIntegrationFlows nodeIntegrationFlows;

    @Inject
    private CrudaFlows crudaFlows;

    @Inject
    private NetSimTestSteps netsimTestSteps;

    @Inject
    private NodeSyncMonitorTestSteps nodeSyncMonitorTestSteps;

    @Inject
    private CliAlarmCommandFlows cliAlarmCommandFlows;

    public TestStepFlow restoreNeState() {
        return flow("Restore state of NEs in Netsim flow")
                .addTestStep(annotatedMethod(netsimTestSteps, RESTORE_NE_STATE_IN_NETSIM))
                .withDataSources(dataSource(NODES_TO_ADD))
                .build();
    }

    public TestStepFlow setNodeIds() {
        return flow("Set Node Ids Required for Test Case Input Data Preparation flow")
                .addTestStep(annotatedMethod(nodeSyncMonitorTestSteps, SET_NODE_IDS))
                .withDataSources(dataSource(NODES_TO_ADD))
                .build();
    }

    public TestStepFlow enableCmNodeSyncMonitorFeature() {
        return flow("Enable the CM Node Sync Monitor Feature flow")
                .addTestStep(annotatedMethod(nodeSyncMonitorTestSteps, ENABLE_CM_NODE_SYNC_MONITOR_FEATURE))
                .build();
    }

    public TestStepFlow disableCmNodeSyncMonitorFeature() {
        return flow("Disable the CM Node Sync Monitor Feature flow")
                .addTestStep(annotatedMethod(nodeSyncMonitorTestSteps, DISABLE_CM_NODE_SYNC_MONITOR_FEATURE))
                .build();
    }

    public TestStepFlow addAndSyncNodes() {
        return flow("Add and Sync Nodes flow")
                .addSubFlow(nodeIntegrationFlows.addNode())
                .addSubFlow(nodeIntegrationFlows.syncNode())
                .withDataSources(dataSource(NODES_TO_ADD))
                .build();
    }

    public TestStepFlow enableFmAlarms() {
        return flow("Enable FM Alarms flow")
                .addSubFlow(cliAlarmCommandFlows.sendCliAlarmCommand(CLI_COMMANDS_DS))
                .build();
    }

    public TestStepFlow resyncNodes() {
        return flow("Resync Nodes flow")
                .addSubFlow(nodeIntegrationFlows.syncNode())
                .withDataSources(dataSource(NODES_TO_ADD))
                .build();
    }

    public TestStepFlow stopNodeInNetsim() {
        return flow("Stop node in netsim flow")
                .addTestStep(annotatedMethod(netsimTestSteps, STOP_NODE_IN_NETSIM))
                .withDataSources(dataSource("nodeToStopOrStart"))
                .build();
    }

    /*
     * Flow that increments the CmFunction FailedSyncsCount attribute by triggering a manual sync of the node. Note that the node must be stopped in
     * netsim before calling this flow.
     */
    public TestStepFlow incrementFailedSyncsCount() {
        return flow("Increment the failed sync count flow")
                .addSubFlow(crudaFlows.cmEditAction())
                .withDataSources(dataSource(CMEDIT_ACTION_DATA_SOURCE))
                .build();
    }

    public TestStepFlow startNodeInNetsim() {
        return flow("Start node in netsim flow")
                .addTestStep(annotatedMethod(netsimTestSteps, START_NODE_IN_NETSIM))
                .withDataSources(dataSource("nodeToStopOrStart"))
                .build();
    }

    public TestStepFlow deleteNodes() {
        return flow("Delete nodes")
                .addSubFlow(nodeIntegrationFlows.deleteNode())
                .withDataSources(dataSource(ADDED_NODES))
                .build();
    }

    public TestStepFlow deleteUser() {
        context.addDataSource(USERS_TO_DELETE, context.dataSource(AVAILABLE_USERS));
        return flow("Delete Users flow")
                .addSubFlow(userManagementTestFlows.deleteUser())
                .build();
    }

}
