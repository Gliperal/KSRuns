package com.ksruns;

import java.io.*;
import java.util.ArrayList;

import javax.servlet.ServletContext;

public class Properties_DEPRECATED
{
	private static Properties_DEPRECATED instance;
	private static String[][] data;
	
	private Properties_DEPRECATED() {}
	
	public static void initialize(ServletContext sc) throws IOException
	{
		BufferedReader f;
		try
		{
			f = new BufferedReader(new InputStreamReader(sc.getResourceAsStream("WEB-INF/properties_web.txt")));
		}
		catch(Exception e)
		{
			f = new BufferedReader(new InputStreamReader(sc.getResourceAsStream("WEB-INF/properties.txt")));
		}
		
		ArrayList<String> lines = new ArrayList<String>();
		String line;
		while((line = f.readLine()) != null)
		{
			if (line.contains("="))
				lines.add(line);
		}
		f.close();
		
		int totalLines = lines.size();
		data = new String[totalLines][2];
		for (int i = 0; i < totalLines; i++)
		{
			line = lines.get(i);
			int split = line.indexOf("=");
			data[i][0] = line.substring(0, split).trim();
			data[i][1] = line.substring(split + 1).trim();
		}
	}
	
	public static Properties_DEPRECATED instance()
	{
		if (instance == null)
			instance = new Properties_DEPRECATED();
		return instance;
	}
	
	public String get(String key)
	{
		if (data == null)
			return null;
		for (String[] pair : data)
		{
			if (pair[0].equals(key))
				return pair[1];
		}
		return null;
	}
	
	public int getInt(String key)
	{
		if (data == null)
			return -1;
		for (String[] pair : data)
		{
			if (pair[0].equals(key))
			{
				try
				{
					return Integer.parseInt(pair[1]);
				} catch(NumberFormatException e)
				{
					return -1;
				}
			}
		}
		return -1;
	}
}
