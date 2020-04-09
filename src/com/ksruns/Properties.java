package com.ksruns;

import java.io.*;
import java.util.Random;

import javax.servlet.ServletContext;

import org.json.JSONArray;
import org.json.JSONObject;

public class Properties
{
	private static JSONObject pages;
	private static JSONObject images;
	private static JSONObject levelBackdrops;
	private static JSONObject scripts;
	private static JSONObject styles;
	
	public static void initialize(ServletContext sc) throws IOException
	{
		JSONObject data = new JSONObject(Util.inputStreamToString(sc.getResourceAsStream("WEB-INF/properties.txt")));
		pages = data.getJSONObject("Pages");
		images = data.getJSONObject("Images");
		levelBackdrops = data.getJSONObject("LevelBackdrops");
		scripts = data.getJSONObject("Scripts");
		styles = data.getJSONObject("Style");
	}
	
	public static String getPage(String key)
	{
		try
		{
			return pages.getString(key);
		} catch(Exception e)
		{
			return null;
		}
	}
	
	public static String getImage(String key)
	{
		try
		{
			return images.getString(key);
		} catch(Exception e)
		{
			return null;
		}
	}
	
	public static LevelBackdrop getLevelBackdrop(int levelID)
	{
		try
		{
			// Backdrops directory
			String root = "";
			if (levelBackdrops.has("Root"))
				root = levelBackdrops.getString("Root");
			
			// Backdrop object
			Object backdrop;
			if (levelBackdrops.has("Backdrop" + levelID))
				backdrop = levelBackdrops.get("Backdrop" + levelID);
			else
				backdrop = levelBackdrops.get("BackdropDefault");
			
			// Randomly choose from a selection of backdrops
			if (backdrop instanceof JSONArray)
			{
				JSONArray backdropArray = (JSONArray) backdrop;
				int count = backdropArray.length();
				int pick = new Random().nextInt(count);
				backdrop = backdropArray.get(pick);
			}
			
			// Only one backdrop available
			if (backdrop instanceof JSONObject)
			{
				JSONObject backdropObj = (JSONObject) backdrop;
				if ((backdropObj).has("position"))
					return new LevelBackdrop(root + backdropObj.getString("file"), backdropObj.getString("position"));
				else
					return new LevelBackdrop(root + backdropObj.getString("file"));
			}
			else if (backdrop instanceof String)
				return new LevelBackdrop(root + backdrop);
		} catch(Exception e) {}
		
		// Some error occured
		return null;
	}
	
	public static String getScript(String scriptName)
	{
		try
		{
			return scripts.getString(scriptName);
		} catch(Exception e)
		{
			return null;
		}
	}

	public static String getStyle(String styleName)
	{
		try
		{
			return styles.getString(styleName);
		} catch(Exception e)
		{
			return null;
		}
	}
}
