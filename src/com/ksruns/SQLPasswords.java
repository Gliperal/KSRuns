package com.ksruns;

import java.io.IOException;
import java.net.URL;

import org.json.JSONObject;

public class SQLPasswords
{
	private static String henna;
	private static String juni;
	
	public static String henna()
	{
		if (henna == null)
		{
			try
			{
				URL passwordsFile = new SQLPasswords().getClass().getResource("SQL.txt");
				JSONObject data = new JSONObject(Util.inputStreamToString(passwordsFile.openStream()));
				henna = data.getString("Henna");
			}
			catch (IOException e)
			{
				return null;
			}
		}
		return henna;
	}
	
	public static String juni()
	{
		if (juni == null)
		{
			try
			{
				URL passwordsFile = new SQLPasswords().getClass().getResource("SQL.txt");
				JSONObject data = new JSONObject(Util.inputStreamToString(passwordsFile.openStream()));
				juni = data.getString("Juni");
			}
			catch (IOException e)
			{
				return null;
			}
		}
		return juni;
	}
}
