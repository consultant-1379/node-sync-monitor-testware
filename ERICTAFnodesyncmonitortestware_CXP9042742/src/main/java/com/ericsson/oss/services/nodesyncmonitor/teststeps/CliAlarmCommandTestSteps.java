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

import static com.ericsson.oss.testware.fm.api.constants.FmCommonDataSources.CLI_COMMANDS_DS;

import javax.inject.Inject;
import javax.inject.Provider;

import org.assertj.core.api.Assertions;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.fm.api.datarecord.CliCommandDataRecord;
import com.ericsson.oss.testware.fm.impl.RestImpl;
import com.ericsson.oss.testware.fm.teststeps.RecursiveGetTestStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliAlarmCommandTestSteps extends RecursiveGetTestStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(CliAlarmCommandTestSteps.class);

    @Inject
    private Provider<RestImpl> provider;

    /**
     * Sends an ENM Cli alarm command and parses its response by content.
     *
     * @param cliCommandDataRecord
     *            The dataRecord for the command to be sent will contain:
     *            commandToSend and expectedResponse fields.
     * @return True if the response complies with the expected one.
     */
    @TestStep(id = StepIds.CLI_ALARM_COMMAND)
    public boolean sendCliAlarmCommand(@Input(CLI_COMMANDS_DS) final CliCommandDataRecord cliCommandDataRecord) {
        checkDataSource(cliCommandDataRecord, CLI_COMMANDS_DS);
        EnmCliResponse enmCliResponse = sendCommand(cliCommandDataRecord.getCommandToSend());

        String actualResponse = enmCliResponse.getSummaryDto().getStatusMessage();
        String expectedResponse = cliCommandDataRecord.getExpectedResponse();
        LOGGER.info("Actual response for get active alarm {}", actualResponse);
        LOGGER.info("Expected response for get active alarm {}", expectedResponse);

        Assertions.assertThat(actualResponse)
                .as("Actual response [%s] does not match expected response [%s]", actualResponse, expectedResponse)
                .isEqualTo(expectedResponse);
        return true;
    }

    /**
     * Sends a CLI command from a given String.
     *
     * @param command
     *            cli parameter (String).
     */
    private EnmCliResponse sendCommand(final String command) {
        final RestImpl restImpl = provider.get();
        return restImpl.sendCommand(command);
    }

    /**
     * Class of Test Step Id constants.
     */
    public static final class StepIds {
        public static final String CLI_ALARM_COMMAND = "CliAlarmCommand";
        public static final String CLI_ENABLE_FM_ALARM = "CliEnableFmAlarm";
    }

}
