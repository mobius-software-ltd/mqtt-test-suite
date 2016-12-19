/**
 * Mobius Software LTD
 * Copyright 2015-2016, Mobius Software LTD
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.mobius.software.mqtt.performance.runner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobius.software.mqtt.performance.api.data.ScenarioRequest;
import com.mobius.software.mqtt.performance.runner.util.FileUtil;
import com.mobius.software.mqtt.performance.runner.util.RequestFormatter;

public class TestRunner
{
	private static final Log logger = LogFactory.getLog(TestRunner.class);

	public static void main(String[] args)
	{
		try
		{
			File json = FileUtil.readFile(args[0]);
			List<RequestWorker> workers = new ArrayList<>();
			List<ScenarioRequest> requests = RequestFormatter.parseScenarioRequests(json);
			CountDownLatch latch = new CountDownLatch(requests.size());
			for (ScenarioRequest request : requests)
				workers.add(new RequestWorker(request, latch));

			ExecutorService service = Executors.newFixedThreadPool(workers.size());
			for (RequestWorker worker : workers)
				service.submit(worker);
			latch.await();
			
			service.shutdownNow();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			System.exit(0);
		}
	}
}
