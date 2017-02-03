# Performance MQTT Test Suite

## Getting started

Now you have an opportunity to independently evaluate the performance of **IoTBroker.Cloud**. Besides this test suite can be used to measure the performance of your own software. The following instruction will explain how to run the performance tests by yourself.

### Prerequisites

The following programs should be installed before starting to clone the project:

* **JDK** (version 7+);
* **Eclipse**;
* **Maven**.

### Installation

First of all, you should clone [Performance MQTT Test Suite](https://github.com/mobius-software-ltd/mqtt-test-suite).

Then you have to build the project. For this purpose, open the Performance Test Suite project in Eclipse, right-click **mqtt-test-suite** and go to _Maven / Update Project_. Then you should again right-click **mqtt-test-suite** and go to _Run As / Maven clean_. Finally you should right-click **mqtt-test-suite** and go to _Run As / Maven install_.

Now you have the controller (in _mqtt-test-suite/controller/target_ folder) and the test runner (in _mqtt-test-suite/runner/target_ folder) jar files on your computer.
To make the work more convenient, create _performance_test_ folder which will contain `controller-jar-with-dependencies.jar` and `runner-jar-with-dependencies.jar`.
Also you should add JSON files to this very _performance_test_ folder and `controller.params` file (you can find it in _mqtt-test-suite/controller_ folder).


### Test run

First you should open the terminal and `cd` to _performance_test_ folder. You should start the controller by running the command which is given below (do not forget to indicate your path):
 

Now you can start the controller by running the following command :

```
java -Xmx2048m -Xms512m -jar controller.jar http://127.0.0.1:9998/ /home/username/performance_test/controller.params

```
Here is a brief explanation:

**Xmx2048m** – maximum memory allocation;

**Xms512m** – initial memory allocation;

**controller.jar** – controller which is inside the _performance_test_ folder;

**http://192.168.1.1:9998/** - IP address and port of controller;

**/home/username/performance_test/controller.params** – path to controller.params file.

Now you should open the second terminal window and `cd` to _performance_test_ folder. Now you can run the test by running the following command:
```
java -jar test-runner.jar pipeline.json
```
The command mentioned above is an example of running the test scenario which is described in `pipeline.json` file.

Each JSON file contains different test scenarios. You can separately run each test scenario by indicating the name of a specific JSON file. When the test is over you will get the report for each test scenario:
```
+---------- Scenario-ID:  df2b3214-650e-41ed-b753-83862a473e6f ---------- Result: SUCCESS ----------+
| Start Time                      | 2016-12-21 18:36:49.526        | 1482338209526                  |
| Finish Time                     | 2016-12-21 18:51:53.762        | 1482339113762                  |
| Current Time                    | 2016-12-21 18:51:59.951        | 1482339119951                  |
+---------------------------------+--------------------------------+--------------------------------+
| Total clients               500 | Total commands          451000 | Errors occured               0 |
| Successfuly finished        500 | Successfuly finished    451000 | Duplicates received          0 |
| Failed                        0 | Failed                       0 | Duplicates sent              0 |
+--------------- Outgoing counters ---------------+--------------- Incoming counters ---------------+
|      Counter Name      |      Counter Value     |      Counter Name      |      Counter Value     |
|         CONNECT        |           500          |         CONNACK        |           500          |
|        SUBSCRIBE       |            0           |         SUBACK         |            0           |
|       UNSUBSCRIBE      |            0           |        UNSUBACK        |            0           |
|         PINGREQ        |           500          |        PINGRESP        |           500          |
|         PUBLISH        |         450000         |         PUBLISH        |            0           |
|         PUBACK         |            0           |         PUBACK         |            0           |
|         PUBREC         |            0           |         PUBREC         |         450000         |
|         PUBREL         |         450000         |         PUBREL         |            0           |
|         PUBCOMP        |            0           |         PUBCOMP        |         450000         |
|       DISCONNECT       |           500          |       DISCONNECT       |            0           |
+------------------------+------------------------+------------------------+------------------------+

+---------- Scenario-ID:  68dd3a83-6aca-48fd-80f6-ec79bf4ddcc7 ---------- Result: SUCCESS ----------+
| Start Time                      | 2016-12-21 18:36:49.514        | 1482338209514                  |
| Finish Time                     | 2016-12-21 18:51:53.794        | 1482339113794                  |
| Current Time                    | 2016-12-21 18:51:59.970        | 1482339119970                  |
+---------------------------------+--------------------------------+--------------------------------+
| Total clients               500 | Total commands          451000 | Errors occured               0 |
| Successfuly finished        500 | Successfuly finished    451000 | Duplicates received          0 |
| Failed                        0 | Failed                       0 | Duplicates sent              0 |
+--------------- Outgoing counters ---------------+--------------- Incoming counters ---------------+
|      Counter Name      |      Counter Value     |      Counter Name      |      Counter Value     |
|         CONNECT        |           500          |         CONNACK        |           500          |
|        SUBSCRIBE       |            0           |         SUBACK         |            0           |
|       UNSUBSCRIBE      |            0           |        UNSUBACK        |            0           |
|         PINGREQ        |           500          |        PINGRESP        |           500          |
|         PUBLISH        |         450000         |         PUBLISH        |            0           |
|         PUBACK         |            0           |         PUBACK         |            0           |
|         PUBREC         |            0           |         PUBREC         |         450000         |
|         PUBREL         |         450000         |         PUBREL         |            0           |
|         PUBCOMP        |            0           |         PUBCOMP        |         450000         |
|       DISCONNECT       |           500          |       DISCONNECT       |            0           |
+------------------------+------------------------+------------------------+------------------------+
```
Each test can be run in its current form.
Besides you can change the existing test scenarios or add the new ones.
The information given [here](https://github.com/mobius-software-ltd/mqtt-test-suite/blob/master/docs/docs-suite/src/main/asciidoc/Pipeline%20Test%20Scenario%20Parameters%20.adoc) will help you to get into these test requests. If you are interested in making considerable changes, the [The Structure of Performance Test Suite](https://github.com/mobius-software-ltd/mqtt-test-suite/blob/master/docs/docs-suite/src/main/asciidoc/The%20Structure%20of%20Performance%20Test%20Suite.adoc) will help you to get into the structure of this test suite.
## [License](LICENSE.md)

