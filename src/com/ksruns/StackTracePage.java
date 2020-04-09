package com.ksruns;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletContext;

public class StackTracePage
{
	public static String exceptionToHTML(ServletContext sc, Exception e) throws IOException
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String stackTrace = sw.toString();
		stackTrace = stackTrace.replaceAll("\r\n", "<br/>");
		stackTrace = stackTrace.replaceAll("\t", "----");
		stackTrace = stackTrace.replaceAll("\\$", "&dollar;");
		
		String html = Util.inputStreamToString(sc.getResourceAsStream(Properties.getPage("StackTrace")));
		html = html.replaceAll("=STACKTRACE=", stackTrace);
		html = Util.insertPaths(html);
		return html;
	}
}
