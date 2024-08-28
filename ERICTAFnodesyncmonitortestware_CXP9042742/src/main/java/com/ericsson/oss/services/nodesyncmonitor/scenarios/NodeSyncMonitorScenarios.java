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

package com.ericsson.oss.services.nodesyncmonitor.scenarios;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.configuration.TafProperty;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.api.TestScenarioBuilder;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.oss.services.nodesyncmonitor.flows.CliAlarmCommandFlows;
import com.ericsson.oss.services.nodesyncmonitor.flows.SetupTearDownFlows;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutRestFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.USER;

/**
 * Executes Node Sync Monitor Test Scenarios
 */
public class NodeSyncMonitorScenarios extends TafTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSyncMonitorScenarios.class);

    @TafProperty("services.nodeSyncMonitor.skipSetup")
    private boolean skipSetup;

    @TafProperty("services.nodeSyncMonitor.skipTeardown")
    private boolean skipTeardown;

    @TafProperty("services.nodeSyncMonitor.delay")
    private long delay;

    @TafProperty("services.nodeSyncMonitor.triggerAlarmDelay")
    private long triggerAlarmDelay;

    @Inject
    private GimCleanupFlows idmCleanupFlows;

    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Inject
    private SetupTearDownFlows setupTearDownFlows;

    @Inject
    private LoginLogoutRestFlows loginLogoutRestFlows;

    @Inject
    private CliAlarmCommandFlows cliAlarmCommandFlows;

    private static final String RFA250 = "RFA250";

    private static final int INCREMENT_FAILED_SYNCS_COUNT_BY = 8;

    private static final String READ_ACTIVE_ALARMS_DATA_SOURCE = "readActiveAlarms";
    private static final String READ_CLEAR_ALARMS_DATA_SOURCE = "readClearedAlarms";

    @BeforeClass(groups = { RFA250 }, alwaysRun = true)
    public void setUp() throws Exception {
        final TestScenarioBuilder setupScenario = scenario("Node Sync Monitor Setup Scenario")
                .addFlow(setupTearDownFlows.enableCmNodeSyncMonitorFeature())
                .addFlow(idmCleanupFlows.cleanUp(USER))
                .addFlow(userManagementTestFlows.createUserWithoutRoleVerification())
                .addFlow(loginLogoutRestFlows.loginWithUserName("nodesyncmonitor_administrator"))
                .addFlow(setupTearDownFlows.setNodeIds());

        if (!skipSetup) {
            setupScenario
                    .addFlow(setupTearDownFlows.restoreNeState()).alwaysRun()
                    .addFlow(setupTearDownFlows.addAndSyncNodes())
                    .addFlow(setupTearDownFlows.enableFmAlarms())
                    .addFlow(setupTearDownFlows.stopNodeInNetsim());
        }
        setupScenario.addFlow(loginLogoutRestFlows.logout());
        executeScenario(setupScenario.build());
    }

    @Test(groups = { RFA250 })
    @TestSuite
    public void triggerAlarm() throws InterruptedException {
        incrementFailedSyncsCount();
        Thread.sleep(triggerAlarmDelay);
        final TestScenario scenario = dataDrivenScenario("Triggering node sync monitor alarm")
                .addFlow(loginLogoutRestFlows.loginWithUserName("nodesyncmonitor_administrator"))
                .addFlow(setupTearDownFlows.setNodeIds())
                .addFlow(cliAlarmCommandFlows.sendCliAlarmCommand("readActiveAlarms"))
                .addFlow(loginLogoutRestFlows.logout())
                .withScenarioDataSources(dataSource(READ_ACTIVE_ALARMS_DATA_SOURCE))
                .build();
        executeScenario(scenario);
    }

    @Test(groups = { RFA250 })
    @TestSuite
    public void clearAlarm() {
        final TestScenario scenario = dataDrivenScenario("Clearing node sync monitor alarm")
                .addFlow(loginLogoutRestFlows.loginWithUserName("nodesyncmonitor_administrator"))
                .addFlow(setupTearDownFlows.setNodeIds())
                .addFlow(setupTearDownFlows.startNodeInNetsim())
                .addFlow(setupTearDownFlows.resyncNodes())
                .addFlow(cliAlarmCommandFlows.sendCliAlarmCommand("readClearedAlarms"))
                .addFlow(loginLogoutRestFlows.logout())
                .withScenarioDataSources(dataSource(READ_CLEAR_ALARMS_DATA_SOURCE))
                .build();
        executeScenario(scenario);

    }

    @AfterSuite(groups = { RFA250 }, alwaysRun = true)
    public void teardown() {
        final TestScenarioBuilder teardownScenario = scenario("Node Sync Monitor Teardown Scenario")
                .addFlow(loginLogoutRestFlows.loginWithUserName("nodesyncmonitor_administrator")).alwaysRun()
                .addFlow(setupTearDownFlows.stopNodeInNetsim());
        if (!skipTeardown) {
            teardownScenario
                    .addFlow(setupTearDownFlows.disableCmNodeSyncMonitorFeature()).alwaysRun()
                    .addFlow(setupTearDownFlows.deleteNodes()).alwaysRun()
                    .addFlow(loginLogoutRestFlows.logout()).alwaysRun()
                    .addFlow(setupTearDownFlows.restoreNeState()).alwaysRun();
        }
        teardownScenario.addFlow(loginLogoutRestFlows.logout()).alwaysRun();
        teardownScenario.addFlow(setupTearDownFlows.deleteUser()).alwaysRun();
        executeScenario(teardownScenario.build());
    }

    private void executeScenario(final TestScenario scenario) {
        final TestScenarioRunner runner = runner()
                .withListener(new LoggingScenarioListener())
                .build();
        runner.start(scenario);
    }

    private void incrementFailedSyncsCount() throws InterruptedException {
        for (int i = 0; i <= INCREMENT_FAILED_SYNCS_COUNT_BY; i++) {
            try {
                executeScenario(getIncrementFailedSyncsCountScenario());
            } catch (Exception e) {
                if (e.getMessage().contains("302 Found")) {
                    executeScenario(getIncrementFailedSyncsCountScenario());
                    LOGGER.debug("Exception on incrementing failedSyncCount {} ", e.getMessage());
                }
            }
            Thread.sleep(delay);
        }
    }

    private TestScenario getIncrementFailedSyncsCountScenario() {
        return scenario("Increasing failed sync count scenario")
                .addFlow(loginLogoutRestFlows.loginWithUserName("nodesyncmonitor_administrator"))
                .addFlow(setupTearDownFlows.incrementFailedSyncsCount())
                .addFlow(loginLogoutRestFlows.logout())
                .build();
    }

}
