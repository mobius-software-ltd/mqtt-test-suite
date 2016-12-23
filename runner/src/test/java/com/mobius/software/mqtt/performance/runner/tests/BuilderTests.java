package com.mobius.software.mqtt.performance.runner.tests;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.mobius.software.mqtt.performance.api.data.ErrorReport;
import com.mobius.software.mqtt.performance.api.data.ErrorType;
import com.mobius.software.mqtt.performance.runner.util.ReportBuilder;

public class BuilderTests
{
	@Test
	public void testErrorBuilder()
	{
		try
		{
			Random random = new Random();
			String identifier = "dummy identity";
			List<ErrorReport> errors = new ArrayList<>();
			for (int i = 0; i < 10; i++)
			{
				Thread.sleep(random.nextInt(100) + 10);
				ErrorReport report = new ErrorReport(ErrorType.PREVIOUS_COMMAND_FAILED, "GENERIC MESSAGE", System.currentTimeMillis());
				errors.add(report);
				Thread.sleep(random.nextInt(100) + 10);
				report = new ErrorReport(ErrorType.CONNECT, "GENERIC MESSAGE", System.currentTimeMillis());
				errors.add(report);
			}
			String report = ReportBuilder.buildError(identifier, errors);
			System.out.println(report);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}
}
