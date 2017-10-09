package com.ehopper.pos;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.text.format.DateUtils;

import com.ehopper.antroshell.entities.MemoryStat;
import com.ehopper.antroshell.util.Util;

public class App extends Application
{
	public static final String TAG = "Antropos";
	 
	private static final String LOG_DIR = "logs";
	private static App instance = null;
	private static AppSettings appSettings;
	//Database database;
 
   
	public App()
	{
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		instance = this;

		//Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
		Fabric fabric = new Fabric.Builder(this)
				.kits( new Crashlytics(), new CrashlyticsNdk())
		//		.debuggable(true)
				.build();
		Fabric.with(fabric);

		Logger.start();

		appSettings = AppSettings.initInstance(getApplicationContext());

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread thread, Throwable e)
			{
				Logger.error("Unhandled exception.", e);
			}
		});

	}
	
	public static App getInstance()
	{
		return instance;
	}

	public static void exit()
	{
		System.exit(0);
	}

	public static Locale getLocale()
	{
		return Locale.getDefault();
	}

	public static File getInternalDataDir()
	{
		return getInstance().getFilesDir();
	}

	public static File getLogDir()
	{
		return new File(getInternalDataDir() + "/" + LOG_DIR);
	}


}
