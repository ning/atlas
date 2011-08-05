package com.ning.atlas.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ning.atlas.UnableToProvisionServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Wrapper around Runtime.getRuntime().exec()
 */
public class DoRuntime {

	private final static Logger logger = LoggerFactory.getLogger(DoRuntime.class);

	public static String exec(String ... cmdarray) throws UnableToProvisionServerException
	{
		Runtime runtime = Runtime.getRuntime();

		Joiner joiner = Joiner.on(" ").skipNulls();
		logger.info("Exec: $ {}", joiner.join(cmdarray));

		StringBuilder sb = new StringBuilder();
		try {
			Process p = runtime.exec(cmdarray);
			String line, newLine = "\n";

			// stdout
			InputStreamReader isr = new InputStreamReader(p.getInputStream());
			BufferedReader br = new BufferedReader(isr);

			while ((line = br.readLine()) != null) {
				sb.append(line).append(newLine);
			}

			// stderr
			isr = new InputStreamReader(p.getErrorStream());
			br = new BufferedReader(isr);

			while ((line = br.readLine()) != null) {
				sb.append(line).append(newLine);
			}

			try {
				int exitcode = p.waitFor();
			} catch (InterruptedException e) {
				throw new UnableToProvisionServerException(e.toString());
			}
		} catch (IOException e) {
			throw new UnableToProvisionServerException(e.toString());
		}
		return sb.toString();
	}

}
