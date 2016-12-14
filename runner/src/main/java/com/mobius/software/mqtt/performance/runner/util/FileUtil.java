package com.mobius.software.mqtt.performance.runner.util;

import java.io.File;
import java.net.URISyntaxException;

import javassist.NotFoundException;

import com.mobius.software.mqtt.performance.runner.TestRunner;

public class FileUtil
{
	public static File readJSONFile(String filename) throws URISyntaxException, NotFoundException
	{
		String path = TestRunner.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

		File json = new File(path).getParentFile();
		json = new File(json.getPath() + "/" + filename);
		if (!json.exists())
			throw new NotFoundException("file not found: " + filename);
		return json;
	}
}
