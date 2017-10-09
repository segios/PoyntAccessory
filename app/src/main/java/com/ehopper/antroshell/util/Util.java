package com.ehopper.antroshell.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Base64;

import com.ehopper.antroshell.entities.HttpResponse;
import com.ehopper.pos.App;
import android.app.NotificationManager;
import com.ehopper.pos.Logger;
import com.ehopper.pos.R;
import com.google.gson.Gson;

public class Util
{
	public static int getAPILevel()
	{
		return Build.VERSION.SDK_INT;
	}

	public static boolean isDebug()
	{
		return (App.getInstance().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
	}

	public static void openAndroidMarket(Activity activity, String appPackageName){
		try {
			activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
		} catch (android.content.ActivityNotFoundException anfe) {
			activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
		}
	}

    public static void shoppingCartChanged(final Activity activity, final String shoppingCartJson)
    {
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                try {
                    Intent showCartIntent = new Intent("ehopper.intent.action.CART_CHANGED");
                    showCartIntent.putExtra("SHOPPING_CART", shoppingCartJson);
                    showCartIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

                    PackageManager packageManager = activity.getPackageManager();
                    List activities = packageManager.queryIntentActivities(showCartIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    boolean isIntentSafe = activities.size() > 0;
                    if(isIntentSafe) {
                        activity.sendBroadcast(showCartIntent);
                    }
                } catch (Throwable e) {
                    Logger.error(e);

                }

            }
        });

    }

	public static boolean isNetworkAvailable(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static boolean isWiFiNetworkAvailable(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo network = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		return network != null && network.isConnected();
	}

	public static boolean isMobileNetworkAvailable(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo network = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		return network != null && network.isConnected();
	}

    public static boolean isEthernetNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo network = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);

        return network != null && network.isConnected();
    }

	public static String getExternalStoragePath(Context context)
	{
		File file = context.getExternalFilesDir(null);

		if (!file.exists())
			file.mkdir();

		return file.getAbsolutePath();
	}

	public static boolean isExternalStorageWritable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state))
			return true;

		return false;
	}

	public static boolean isExternalStorageReadable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
			return true;

		return false;
	}

	public static String inputStream2String(InputStream inputStream)
	{
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = "";
		StringBuffer result = new StringBuffer();
		try
		{
			while ((line = bufferedReader.readLine()) != null)
				result.append(line);
		}
		catch (IOException e)
		{
			Logger.error(e);
			return null;
		}
		finally
		{
			try
			{
				if (bufferedReader != null)
					bufferedReader.close();

				if (inputStream != null)
					inputStream.close();
			}
			catch (IOException e)
			{
			}
		}

		return result.toString();
	}


	public static HttpResponse execHttpCommand(String targetURL, String requestMethod, String parameters) 
	{
		URL url;
		HttpURLConnection connection = null;
		HttpResponse httpResponse = new HttpResponse();
		
		try
		{
			// Create connection
			url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestMethod);
			
			if (!TextUtils.isEmpty(parameters))
			{
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Content-Length", Integer.toString(parameters.length()));
			}

			connection.setUseCaches(false);
			connection.setDoInput(true);
			
			// Send request
			if (!TextUtils.isEmpty(parameters))
			{
				connection.setDoOutput(true);
				
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
				out.write(parameters);
				out.close();				
			}
			
			httpResponse.setResponseCode(connection.getResponseCode());
			httpResponse.setResponseMessage(connection.getResponseMessage());
			
			// Get Response
			InputStream is = connection.getInputStream();
			httpResponse.setData(Util.inputStream2String(is));
			return httpResponse;
		}
		catch (Exception e)
		{
			Logger.error(e);
			
			InputStream es = connection.getErrorStream();
			if (es != null)
				httpResponse.setErrorMessage(Util.inputStream2String(es));
			return httpResponse; 
		}
		finally
		{
			if (connection != null)
				connection.disconnect();
		}
	}
	
	
	public static String readFile2String(String file)
	{
		return readFile2String(new File(file));
	}
	
	public static String readFile2String(File file)
	{
		FileReader reader = null;

		try
		{
			if (file == null || !file.exists())
				return null;

			reader = new FileReader(file);
			char[] buf = new char[(int) file.length()];
			reader.read(buf);
			return new String(buf);
		}
		catch (Exception e)
		{
			Logger.error(e);
			return null;
		}
		finally
		{
			if (reader != null)
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
				}
		}
	}

	public static boolean deleteFileOrDir(String file)
	{
		return deleteFileOrDir(new File(file));
	}
	
	public static boolean deleteFileOrDir(File file)
	{
		if (file == null || !file.exists())
			return true;
		
		if (file.isDirectory())
		{
			String[] children = file.list();
			for (int i = 0; i < children.length; i++)
			{
				boolean success = deleteFileOrDir(new File(file, children[i]));
				if (!success)
					return false;
			}
		}

		return file.delete(); // The directory is empty now and can be deleted.
	}
	
	public static void setItem(Activity activity, String key, String value)
	{
		FileWriter fileWriter = null;

		try
		{
			File file = new File(activity.getFilesDir().getPath() + "/" + key);
			fileWriter = new FileWriter(file);
			fileWriter.write(value);
		}
		catch (Exception e)
		{
			Logger.error("Cannot setItem.", e);
		}
		finally
		{
			if (fileWriter != null)
			{
				try
				{
					fileWriter.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		/*
		 * try {
		 * 
		 * if (getItem(key) == null) { ContentValues cv = new ContentValues();
		 * cv.put(DbHelper.DICTIONARY_TABLE_ID_FIELD_NAME, key);
		 * cv.put(DbHelper.DICTIONARY_TABLE_VALUE_FIELD_NAME, value);
		 * 
		 * database.insert(DbHelper.DICTIONARY_TABLE_NAME, null, cv); } else {
		 * ContentValues cv = new ContentValues();
		 * cv.put(DbHelper.DICTIONARY_TABLE_VALUE_FIELD_NAME, value);
		 * 
		 * String where = String.format("%s = ?",
		 * DbHelper.DICTIONARY_TABLE_ID_FIELD_NAME); String []whereArgs = { key
		 * };
		 * 
		 * database.update(DbHelper.DICTIONARY_TABLE_NAME, cv, where,
		 * whereArgs); } } catch(Exception e) {
		 * Logger.logError("Cannot setItem.", e); }
		 */
	}
	
	public static String getItem(Activity activity, String key)
	{ 
		FileReader reader = null;

		try
		{
			File file = new File(activity.getFilesDir().getPath() + "/" + key);
			if (!file.exists())
				return null;

			reader = new FileReader(file);
			char[] buf = new char[(int) file.length()];
			reader.read(buf);
			String ret = new String(buf);

			return ret;
		}
		catch (Exception e)
		{
			Logger.error("Cannot getItem from db.", e);
			return null;
		}
		finally
		{
			if (reader != null)
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
				}
		}

		/*
		 * Cursor cursor = null;
		 * 
		 * try { String []columns = { DbHelper.DICTIONARY_TABLE_VALUE_FIELD_NAME
		 * }; String where = String.format("%s = ?",
		 * DbHelper.DICTIONARY_TABLE_ID_FIELD_NAME); String []whereArgs = { key
		 * };
		 * 
		 * cursor = database.query(DbHelper.DICTIONARY_TABLE_NAME, columns,
		 * where, whereArgs, null, null, null);
		 * 
		 * if (cursor.moveToFirst()) return cursor.getString(0); else return
		 * null; } catch(Exception e) {
		 * Logger.logError("Cannot getItem from db.", e); return null; } finally
		 * { if (cursor != null) cursor.close(); }
		 */
	}
	
	public static String addFileResource(Activity activity, String fileName, String fileContent)
	{
		if (TextUtils.isEmpty(fileContent))
			return null;

		FileOutputStream fos = null;

		try
		{
			byte[] buf = Base64.decode(fileContent, Base64.DEFAULT);

			// File file = new File(App.getImageDir().getAbsolutePath() + "/" +
			// fileName);
			File file = new File(activity.getFilesDir().getPath() + "/"
					+ fileName);

			fos = new FileOutputStream(file);
			fos.write(buf);
			return file.getAbsolutePath();
		}
		catch (Exception e)
		{
			Logger.error("Cannot addFileResource.", e);
			return null;
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	public static void showNotification(Context context, String title, String message){
        int notificationId = 7;
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.icon)
						.setContentTitle(title)
						.setContentText(message);
/*// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, ResultActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ResultActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
				stackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_UPDATE_CURRENT
				);
		mBuilder.setContentIntent(resultPendingIntent);*/
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
		mNotificationManager.notify(notificationId, mBuilder.build());
	}

}
