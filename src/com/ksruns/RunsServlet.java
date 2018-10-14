package com.ksruns;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

//@WebServlet("TODO")
public class RunsServlet extends HttpServlet
{
	private static final long serialVersionUID = 2674941701807134103L;
	
	protected JSONArray queryToJSON(String query) throws SQLException
	{
		// TODO Parse query string
		int levelID, categoryID;
		try
		{
			int i = query.indexOf("level=");
			levelID = Integer.parseInt(query.substring(i+6, i+7));
			i = query.indexOf("category=");
			categoryID = Integer.parseInt(query.substring(i+9, i+10));
		}
		catch(NumberFormatException e)
		{
			levelID = 8;
			categoryID = 4;
		}
		
		return HennaSQL.fetchRunsJSON(levelID, categoryID, false);
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, java.io.IOException
	{
		// read request data
		String query = req.getQueryString();

		String returnJSON;
		try
		{
			JSONArray levelsArray = queryToJSON(query);
			returnJSON = levelsArray.toString();
		} catch (SQLException e)
		{
			returnJSON = "";
		}
		
		// write response headers
		resp.setContentType("application/json"); // uses MIME type
		resp.setContentLength(returnJSON.length());
		
		// get the response's writer / output stream
		PrintWriter out = resp.getWriter();
		
		// write the response data
	    out.println(returnJSON);
	}
}
