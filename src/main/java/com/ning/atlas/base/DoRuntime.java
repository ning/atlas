package com.ning.atlas.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
			InputStream is = p.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line, newLine = "\n";

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

	/**
	 * Returns the specified string in quotes
	 * @param s A string
	 * @return The modified string
	 */
	public static String stringify(String s) {
		s = s.replace("\\", "\\\\");
		s = s.replace("\"", "\\\"");
		return "\"" + s + "\"";
	}
}
