package com.ehopper.pos;

import java.lang.reflect.Type;

import com.google.gson.Gson;

public class Json
{
	public static <T> T fromJson(String json, Type typeOfT)
	{
		try
		{
			Gson gson = new Gson();
			return gson.fromJson(json, typeOfT);
		}
		catch(Exception e)
		{
			throw e;
		}
	}

	public static String toJson(Object object)
	{
		Gson gson = new Gson();
		return gson.toJson(object);
	}
}
