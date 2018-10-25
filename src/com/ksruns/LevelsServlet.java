package com.ksruns;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

//@WebServlet("TODO")
public class LevelsServlet extends HttpServlet
{
	private static final long serialVersionUID = 2647215789001455460L;

	protected ArrayList<Level> fetchAllLevels()
	{
		try
		{
			// Load the JDBC driver for ConnectorJ
			try
			{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			// TODO put all this stuff into SQL class
			// Obtain the connection
			Connection connection = HennaSQL.databaseConnection();
			
			// Make the query
			String selectSQL = "SELECT ID, Code, Author, Name, Description FROM Levels WHERE Verified = 1";
			PreparedStatement statement = connection.prepareStatement(selectSQL);
			ResultSet result = statement.executeQuery();

			// Package the results into an ArrayList
			ArrayList<Level> levels = new ArrayList<Level>();
			while(result.next())
			{
				levels.add(new Level(
						result.getInt(1), // ID
						result.getString(2), // Code
						result.getString(3), // Author
						result.getString(4), // Name
						result.getString(5), // Description
						null
				));
			}
			return levels;
		} catch (SQLException e)
		{
			// TODO
			e.printStackTrace();
			return null;
		}
	}
	
	protected JSONArray queryToJSON(String query)
	{
		// TODO: Parse query string
		
		// retrieve levels from database
		ArrayList<Level> levels = fetchAllLevels();
		if(levels == null)
			return null;
		
		// build JSON
		JSONArray returnArray = new JSONArray();
		for(Level level : levels)
			returnArray.put(level.toJSON());
		
		return returnArray;
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, java.io.IOException
	{
		// read request data
		String query = req.getQueryString();
		
		JSONArray levelsArray = queryToJSON(query);
		String returnJSON;
		if(levelsArray == null)
			// TODO Handle error
			returnJSON = "[{\"author\":\"Drakkan\",\"name\":\"Training Grounds\",\"description\":\"Liu-Fox awaits you !&nbsp&nbsp&nbsp&nbsp&nbsp&nbspV2 FINAL\",\"id\":1},{\"author\":\"Drakkan\",\"name\":\"Frozen Mountain 2\",\"description\":\"\",\"id\":2},{\"author\":\"LPChip\",\"name\":\"Learning to Jump\",\"description\":\"This is the first Knytt Stories Trickjump map, where its all about jumping.\",\"id\":3},{\"author\":\"Nifflas\",\"name\":\"A Strange Dream\",\"description\":\"That night, she has a strange dream...\",\"id\":4},{\"author\":\"Nifflas\",\"name\":\"An Underwater Adventure\",\"description\":\"...in an underwater laboratory.\",\"id\":5}]";
		else
			returnJSON = levelsArray.toString();
		
		// write response headers
		resp.setContentType("application/json"); // uses MIME type
		resp.setContentLength(returnJSON.length());
		
		// get the response's writer / output stream
		PrintWriter out = resp.getWriter();
		
		// write the response data
	    out.println(returnJSON);
	}
}
