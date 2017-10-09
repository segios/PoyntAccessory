package com.ehopper.pos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.LoggerFactory;

import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

public class Logger
{
	private static final String EXCEPTION_MSG = "Exception was handled.";
	private static final int STACK_TRACE_LEVELS_UP = 4;
	private static boolean isEnabled = false;
	private static org.slf4j.Logger fileLogger = null;

	public static void start()
	{
		Logger.isEnabled = true;
		System.setProperty("app.log.dir", App.getLogDir().getAbsolutePath());
		fileLogger = LoggerFactory.getLogger(App.class);
	}

	public static String getLogs(final Date from)
	{
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", App.getLocale());

		FilenameFilter filenameFilter = new FilenameFilter()
		{
			@Override
			public boolean accept(File paramFile, String paramString)
			{
				Date logDate;
				try
				{
					logDate = dateFormat.parse(paramString);
					if (from.compareTo(logDate) >= 0)
						return true;
				}
				catch (Exception e)
				{
					Logger.error(e);
					return false;
				}

				return false;
			}
		};

		List<String> files = Arrays.asList(App.getLogDir().list(filenameFilter));
		Collections.sort(files);

		StringBuilder sb = new StringBuilder();
		for (String file : files)
			readLogFile(App.getLogDir() + "/" + file, sb, from);

		return sb.toString();
	}

	private static void readLogFile(String file, StringBuilder sb, Date from)
	{
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", App.getLocale());
		
		try
		{
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			
			for(String row = bufferedReader.readLine(); row != null; row = bufferedReader.readLine())
			{
				if (TextUtils.isEmpty(row))
					continue;
				
				Date logDate = null;
				try
				{
					logDate = dateFormat.parse(row);
				}
				catch(Exception e)
				{
					//e.printStackTrace();
					sb.append(row + "\n");
					continue;
				}
				
				if (from.compareTo(logDate) >= 0)
					sb.append(row + "\n");
			}
			
		}
		catch (Exception e)
		{
			Logger.error(e);
		}
		finally
		{
			if (bufferedReader != null)
				try
				{
					bufferedReader.close();
				}
				catch (IOException e)
				{
					Logger.error(e);
				}
			
			if (fileReader != null)
				try
				{
					fileReader.close();
				}
				catch (IOException e)
				{
					Logger.error(e);
				}
		}
	}

	public static void error(String log)
	{
		if (isEnabled)
		{
			Crashlytics.logException(new Exception(log));

			Log.e(getLocation(), log);
			fileLogger.error(formatMsg(getLocation(), log));
//			DynaHelper.reportError(log);
		}
	}

	public static void error(Throwable throwable)
	{
		if (isEnabled)
		{
            Crashlytics.logException(throwable);
			Log.e(getLocation(), EXCEPTION_MSG + " " + throwable.getMessage(), throwable);
			fileLogger.error(formatMsg(getLocation(), EXCEPTION_MSG), throwable.getMessage());
//			DynaHelper.reportError(EXCEPTION_MSG, throwable);
		}
	}

	public static void error(String log, Throwable throwable)
	{
		if (isEnabled)
		{
            Crashlytics.logException(throwable);
			Log.e(getLocation(), log + " " + throwable.getMessage(), throwable);
			fileLogger.error(formatMsg(getLocation(), log), throwable.getMessage());
		}
	}

	public static void info(String log)
	{
		if (isEnabled && fileLogger.isInfoEnabled())
		{
			Crashlytics.log(Log.INFO, getLocation(), log);
			Log.i(getLocation(), log);
			fileLogger.info(formatMsg(getLocation(), log));
		}

	}

	public static void debug(String log)
	{
		if (isEnabled && fileLogger.isInfoEnabled())
		{
			Log.d(getLocation(), log);
		}

	}

	public static void webError(String log)
	{
		if (isEnabled)
		{
			Crashlytics.logException(new Exception(log));

			Log.e("WebApp", log);
			fileLogger.error(formatMsg("WebApp", log));
//			DynaHelper.reportError(log);
		}
	}

	public static void webInfo(String log)
	{
		if (isEnabled && fileLogger.isInfoEnabled())
		{
			Crashlytics.log(Log.INFO, "WebApp", log);

			Log.i("WebApp", log);
			fileLogger.info(formatMsg("WebApp", log));
		}
	}

	private static String getLocation()
	{
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		if (stackTrace.length >= STACK_TRACE_LEVELS_UP)
		{
			StackTraceElement target = stackTrace[STACK_TRACE_LEVELS_UP];
			return String.format(App.getLocale(), "%s.%s-%d", target.getClassName(), target.getMethodName(), target.getLineNumber());
		}
		else
			return App.TAG;
	}

	private static String formatMsg(String location, String msg)
	{
		return location + ": " + msg;
	}
}
