package com.ksruns;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

//@WebServlet("TODO")
public class SubmitPageServlet extends HttpServlet
{
	private static final long serialVersionUID = -6049366651367094514L;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, java.io.IOException
	{
		// Ready servlet context
		ServletContext sc = getServletContext();
		
		// Ready properties file
		Properties.initialize(sc);

		// Build html
		String html;
		try
		{
			html = Util.inputStreamToString(sc.getResourceAsStream(Properties.getPage("Submit")));
			JSONArray categoriesByLevel = HennaSQL.fetchCategoriesByLevelJSON();
			html = html.replaceAll("=ALLCATEGORIESJSON=", categoriesByLevel.toString());
			html = html.replaceAll("=SCRIPTS/LOGINBAR=", Properties.getScript("LoginBar"));
			html = html.replaceAll("=CSS/LOGINBAR=", Properties.getStyle("LoginBar"));
		}
		catch (SQLException e)
		{
			html = StackTracePage.exceptionToHTML(sc, e);
		}
		
		// write response headers
		resp.setContentType("text/html"); // uses MIME type
		resp.setContentLength(html.length());
		
		// get the response's writer / output stream
		PrintWriter out = resp.getWriter();
		
		// write the response data
	    out.println(html);
	}
}
