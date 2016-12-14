package com.mobius.software.mqtt.performance.runner.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.mobius.software.mqtt.performance.api.data.ClientReport;
import com.mobius.software.mqtt.performance.api.data.ErrorReport;
import com.mobius.software.mqtt.performance.api.data.ErrorReportComparator;

public class ReportWriter
{
	private static final String DIRECTORY_NAME = "errors";

	public static void writeErrors(UUID scenarioID, List<ClientReport> reports) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		for (ClientReport clientReport : reports)
		{
			List<ErrorReport> errorReports = clientReport.getErrors();
			if (!errorReports.isEmpty())
			{
				Collections.sort(errorReports, new ErrorReportComparator());
				String errorContent = ReportBuilder.buildError(clientReport.getIdentifier(), errorReports);
				sb.append(errorContent);
			}
		}

		if (sb.length() > 0)
		{
			File errorsDir = new File(DIRECTORY_NAME);
			if (!errorsDir.exists())
			{
				try
				{
					errorsDir.mkdir();
				}
				catch (SecurityException se)
				{

				}
			}

			String errorFileName = DIRECTORY_NAME + "/" + scenarioID.toString();
			File errorFile = new File(errorFileName);
			errorFile.createNewFile();

			try (PrintWriter out = new PrintWriter(errorFileName))
			{
				out.println(sb.toString());
			}
		}
	}
}
