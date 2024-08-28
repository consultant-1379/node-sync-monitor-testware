Maven Profiles
==============

This test suite contains one maven profile. The profile is defined in the following pom:

./test-pom-nodesyncmonitor/pom.xml

The default profile will be executed if no properties are specified. This profile will execute rfa250 tests only.

To execute all rfa250 tests it must be specified on the command line as follows:

mvn clean install -Prfa250 -Dtaf.clusterId=<cluster id of the ENM system>

Note: The KGB+N job for Node Sync Monitor is configured to execute tests against all rfa250 tests.
Note: Maintrack will also execute the rfa250 tests.

Executing TAF tests against ENM System with 5K sims
===================================================

Execute the tests as follows:

mvn clean install -Dtaf.clusterId=<cluster id of the ENM system>

or

mvn clean install -Prfa250 -Dtaf.clusterId=<cluster id of the ENM system>

If you wish you can skip setup and/or teardown 

mvn clean install -Dtaf.clusterId=<cluster id of the ENM system> -Dservices.nodeSyncMonitor.skipSetup=true -Dservices.nodeSyncMonitor.skipTeardown=true

There is a delay added to the setup while waiting for the CmFunction.failedSyncsCount to increase. This is set at 10 seconds as default but can be 
increased or decreased by supplying -Dservices.nodeSyncMonitor.delay=<time in milliseconds>

The nodes added will be taken from the following CSV file:

./ERICTAFnodesyncmonitortestware_CXP9042742/src/main/resources/data/nodesToAdd.csv

Executing TAF tests against ENM System with 2K sims
====================================================

Execute the tests as follows:

mvn clean install -Prfa250 -Dtaf.clusterId=<cluster id of the ENM system> -Dtaf.profiles=maintrack -DMT_CSV_FILE_URI=https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/sites/tor/enm-maintrack-central-test-datasource/latest/maintrack/csv/nodeToAdd_2K.csv

The nodes added will be taken from a remote CSV file maintained by the NSS and Maintrack teams:

https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/sites/tor/enm-maintrack-central-test-datasource/latest/maintrack/csv/nodeToAdd_2K.csv

The test suite name for tests ran in MT is taken from taf_scheduler_kvm/src/main/resources/enm_schedule_RFA250_svc.xml

Generating allure reports on your local machine
===============================================

Install Allure command line if not already installed
----------------------------------------------------

Navigate to the following page and follow the steps to install Allure command line:

http://wiki.qatools.ru/display/AL/Allure+Commandline

For windows machines
--------------------

- Open a cmd prompt and navigate to the 'bin' folder of the 'allure-commandline' folder.

- Run the following command and give it the path to the 'allure-results' folder: 
  
  allure generate C:\<git directory>\node-sync-monitor-testware\target\allure-results

- Display the allure report with the following command:
  
  allure report open

For linux machines
------------------

- Open a shell and navigate to the 'bin' folder of the 'allure-commandline' folder.

- Run the following command and give it the path to the 'allure-results' folder:

  allure generate /<git directory>/node-sync-monitor-testware/target/allure-results/

- Display the allure report with the following command:

  allure report open
