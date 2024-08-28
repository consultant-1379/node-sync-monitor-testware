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

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.transform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.ConfigurationSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Data provider which dynamically updates the datasource identified by the property {@link NodeIdDataProvider#FILENAME_CSV_PROPERTY}.
 * It replaces all occurrences of the following strings with values defined in system properties:
 * <p></p>
 * <ul>
 * <li>${rbsNodeId}</li>
 * </ul>
 * The following system properties are set in {@link com.ericsson.oss.services.nodesyncmonitor.teststeps.NetSimTestSteps} to contain the ids of the
 * nodes under test:
 * <p></p>
 * <ul>
 * <li>rbsNodeId</li>
 * </ul>
 * The data provider is intended to be used in a {@code DataDriven.properties} file as a class of type DataSource.
 * <p></p>
 * Example usage:
 * <p></p>
 *
 * <pre>
 * dataprovider.cms.type=class
 * dataprovider.cms.class=com.ericsson.oss.services.nodesyncmonitor.dataprovider.NodeIdDataProvider
 * dataprovider.cms.filename.csv=cmsUsecases.csv
 * </pre>
 */
public class NodeIdDataProvider {

    private static final String FILENAME_CSV_PROPERTY = "filename.csv";
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeIdDataProvider.class);
    private static final String[] findList = { "${rbsNodeId}" };
    private static final String[] replaceList = { (String) DataHandler.getAttribute("rbsNodeId") };

    @DataSource
    public Iterable<Map<String, Object>> data(final ConfigurationSource reader) {
        final TestDataSource<DataRecord> csvDataSource = getCsvDataSource(reader);
        final Function<DataRecord, DataRecord> transformFunction = updateDataSourceWithNodeIds();
        final TestDataSource<DataRecord> modifiedDataSource = transform(csvDataSource, transformFunction);
        return createIterableDataSource(modifiedDataSource);
    }

    private TestDataSource<DataRecord> getCsvDataSource(final ConfigurationSource reader) {
        final String csvFile = reader.getProperty(FILENAME_CSV_PROPERTY);
        if (StringUtils.isNotBlank(csvFile)) {
            return fromCsv("data/" + csvFile);
        } else {
            throw new IllegalArgumentException(String.format("%s property is not provided", FILENAME_CSV_PROPERTY));
        }
    }

    private static Function<DataRecord, DataRecord> updateDataSourceWithNodeIds() {
        return new Function<DataRecord, DataRecord>() {
            @Override
            public DataRecord apply(final DataRecord input) {
                final Map<String, Object> data = Maps.newHashMap(input.getAllFields());
                for (final String fieldName : data.keySet()) {
                    final String fieldValue = (String) input.getFieldValue(fieldName);
                    final String updatedFieldValue = updateFieldValue(fieldValue);
                    LOGGER.info("Replacing field value {} with new value {}", fieldValue, updatedFieldValue);
                    data.put(fieldName, updatedFieldValue);
                }
                return new DataRecordImpl(data);
            }
        };
    }

    private static String updateFieldValue(final String fieldValue) {
        return StringUtils.replaceEach(fieldValue, findList, replaceList);
    }

    private Iterable<Map<String, Object>> createIterableDataSource(final TestDataSource<DataRecord> modifiedDataSource) {
        final ArrayList<Map<String, Object>> iterableDataSource = Lists.newArrayList();
        final Iterator<DataRecord> dataSourceIterator = modifiedDataSource.iterator();
        while (dataSourceIterator.hasNext()) {
            final DataRecord dataRecord = dataSourceIterator.next();
            iterableDataSource.add(dataRecord.getAllFields());
        }
        return iterableDataSource;
    }

}
