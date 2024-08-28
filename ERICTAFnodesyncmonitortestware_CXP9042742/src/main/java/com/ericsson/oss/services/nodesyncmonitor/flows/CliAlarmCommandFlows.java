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

import static com.ericsson.oss.testware.fm.api.constants.FmCommonDataSources.CLI_COMMANDS_DS;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.services.nodesyncmonitor.teststeps.CliAlarmCommandTestSteps;


public class CliAlarmCommandFlows {

    @Inject
    private CliAlarmCommandTestSteps cliAlarmCommandTestSteps;

    /**
     * Flow to execute alarm commands on the ENM Cli and check the syntax of the response.
     * The default datasource name is "{@value com.ericsson.oss.testware.fm.api.constants.FmCommonDataSources#CLI_COMMANDS_DS}" <br/>
     * The datasource shall have the following columns with mandatory paramaters:<br/>
     * "commandToSend" <br/>
     * "expectedResponse" <br/>
     *
     * @return TestStepFlowBuilder
     */

    public TestStepFlowBuilder sendCliAlarmCommand(final String dataSourceName) {
        return flow("Send Cli Alarm Command")
                .addTestStep(annotatedMethod(cliAlarmCommandTestSteps, CliAlarmCommandTestSteps.StepIds.CLI_ALARM_COMMAND))
                .withDataSources(dataSource(dataSourceName).bindTo(CLI_COMMANDS_DS));
    }

}
