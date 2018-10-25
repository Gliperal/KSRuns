package com.ksruns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class Util
{
	public static String htmlEscape(String s)
	{
		if (s == null)
			return null;
		return s.replaceAll("\"", "&quot;").replaceAll("'", "&#39;");
	}
	
	public static String csvToTextList(String csv, String lastSeparator)
	{
		if (!csv.contains(","))
			return csv;
		String[] items = csv.split(",");
		if (items.length == 2)
			return items[0] + " " + lastSeparator + " " + items[1];
		else
		{
			String result = lastSeparator + " " + items[items.length-1];
			for (int i = items.length-2; i >= 0; i--)
				result += items[i] + ", ";
			return result;
		}
	}
	
	public static String csvToTextList(String csv, String lastSeparator, String padding)
	{
		if (!csv.contains(","))
			return csv;
		String[] items = csv.split(",");
		if (items.length == 2)
			return padding + items[0] + padding + " " + lastSeparator + " " + padding + items[1] + padding;
		else
		{
			String result = lastSeparator + " " + padding + items[items.length-1] + padding;
			for (int i = items.length-2; i >= 0; i--)
				result += padding + items[i] + padding + ", ";
			return result;
		}
	}
	
	public static String insertPaths(String str)
	{
		String newStr = str + "";
		newStr = newStr.replaceAll("=PATH\\(LEVELICON\\)=", Properties.getImage("LevelIcon"));
		newStr = newStr.replaceAll("=LEVELICONDEFAULT=", Level.iconPath_default());
		newStr = newStr.replaceAll("=PATH\\(LEVELBACKDROP\\)=", Properties.getImage("LevelBackdrop"));
		newStr = newStr.replaceAll("=PATH\\(IMAGEUTIL\\)=", Properties.getImage("Util"));
		return newStr;
	}
	
	public static String inputStreamToString(InputStream is) throws IOException
	{
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		return writer.toString();
	}
	
	public static String stackTraceToString(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String sStackTrace = sw.toString(); // stack trace as a string
		return sStackTrace;
	}
	
	public static JSONObject readJSONData(HttpServletRequest req) throws IOException
	{
		String requestData = "";
		String line;
		BufferedReader br = req.getReader();
		while((line = br.readLine()) != null)
			requestData += line;
		return new JSONObject(requestData);
	}
}
