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

package com.mobius.software.mqtt.performance.runner.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import javassist.NotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobius.software.mqtt.performance.api.data.ClientReport;
import com.mobius.software.mqtt.performance.api.data.ErrorReport;
import com.mobius.software.mqtt.performance.runner.TestRunner;

public class FileUtil
{
	private static final Log logger = LogFactory.getLog(FileUtil.class);

	private static final String DIRECTORY_NAME = "errors";
	private static final String LOG_EXTENSION = ".log";

	public static File readFile(String filename) throws URISyntaxException, NotFoundException
	{
		String path = TestRunner.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		File file = new File(path).getParentFile();
		file = new File(file.getPath() + File.separator + filename);
		if (!file.exists())
			throw new NotFoundException("file not found: " + filename);
		return file;
	}

	public static void logErrors(UUID scenarioID, List<ClientReport> reports)
	{
		if (reports.isEmpty())
			return;

		String filename = DIRECTORY_NAME + File.separator + scenarioID.toString() + LOG_EXTENSION;
		File log = new File(filename);
		if (!log.getParentFile().exists())
			log.getParentFile().mkdir();
		try (PrintWriter pw = new PrintWriter(log))
		{
			for (ClientReport clientReport : reports)
			{
				List<ErrorReport> errorReports = clientReport.getErrors();
				if (!errorReports.isEmpty())
				{
					/*Collections.sort(errorReports, new Comparator<ErrorReport>()
					{
						@Override
						public int compare(ErrorReport o1, ErrorReport o2)
						{
							if (o2 == null)
								return 1;
							if (o1 == null)
								return -1;
							if (o2.getTimestamp() == null)
								return 1;
							if (o1.getTimestamp() == null)
								return -1;
							if (o1.getTimestamp().longValue() > o2.getTimestamp().longValue())
								return 1;
							else
								return -1;
						}
					});*/
					String errorContent = ReportBuilder.buildError(clientReport.getIdentifier(), errorReports);
					pw.println(errorContent);
				}
			}
		}
		catch (IOException e)
		{
			logger.error("An error occured while writing error reports to file:" + e.getMessage());
		}

		if (log.length() == 0)
			log.delete();
	}
}