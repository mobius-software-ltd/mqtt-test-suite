# Performance Test Suite

Performance Test Suite allows you to independently evaluate the performance of IoTBroker.Cloud. Performance Test Suite contains controller, test runner and test scenarios that can be tailored to your demands. Performance Test Suite enables you to run the performance tests by yourself.

## How does it work?

The test runner retrieves JSON file where the test scenario is already described and sends it to controller. The controller performs the test according to the scenario and creates the report. Having got the request for report from the test runner, the controller sends it to the test runner. 

### Prerequisites

The following programs should be installed before starting the test process:

* **JVM**;
* **JDK**;

### Test run

First of all, it is necessary to download the performance_test.tar.gz file and unzip it. This folder contains test runner, controller, test requests and controller parameters.

You should open the terminal and `cd` to *performance_test* folder. 

Now you can start the controller by running the following command :

```
java -Xmx[maximum memory allocation] -Xms[initial memory allocation] -jar controller.jar [IP address of controller]:[port] [path to controller.params file] 
```

Here is an example of this command:

```
java -Xmx2048m -Xms512m -jar controller.jar http://127.0.0.1:9998/ /home/username/performance_test/controller.params 

```

Each JSON file contains different test scenarios. Before the test run you should indicate IP address and port of the controller in JSON file ("hostname" and "port" attributes in "controllers" object).

The test scenarios can be run in its current form. Besides you can change the existing test scenarios or add the new ones. Each JSON file can contain unlimited number of test scenarios.

In order to start running the tests you should open the second terminal window and `cd` to performance_test folder. Now you can run the test by running the following command:

```
java -jar test-runner.jar ping.json
```
The command mentioned above is an example of running the test scenario which is described in ping.json file. If you want to run some other tests, you should indicate the corresponding name of JSON file instead of ping.json. 

When the testing process will be finished, you will get the detailed report in the terminal window.

## License

This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site: http://www.fsf.org

