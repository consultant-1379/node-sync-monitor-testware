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

package com.ericsson.oss.services.nodesyncmonitor.dataprovider;

import com.ericsson.oss.testware.network.operators.netsim.NetsimOperator;

/**
 * Ensures only a single instance of the {@link NetsimOperator} is
 * created.
 */
public class NetSimOperatorProvider {

    private static NetSimOperatorProvider instance = new NetSimOperatorProvider();
    private static NetsimOperator operator = new NetsimOperator();

    private NetSimOperatorProvider() {}

    public static NetSimOperatorProvider getInstance() {
        return instance;
    }

    public NetsimOperator getNetsimOperator() {
        return operator;
    }

}
