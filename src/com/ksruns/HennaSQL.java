package com.ksruns;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class HennaSQL
{
	private static final String runsQuery_levelID =
			"SELECT Runs.RunTime, Players.Name, Players.Link, Runs.Date, Runs.Video " +
			"FROM Runs " +
			"RIGHT JOIN Players " +
			"ON Players.ID=Runs.PlayerID " +
			"WHERE Runs.LevelID=? AND Runs.CategoryID=? AND Runs.Verified=1 " +
			"ORDER BY Runs.RunTime, Runs.Date";
	private static final String runsQuery_levelCode =
			"SELECT Runs.RunTime, Players.Name, Players.Link, Runs.Date, Runs.Video " +
			"FROM Runs " +
			"RIGHT JOIN Players " +
			"ON Players.ID=Runs.PlayerID " +
			"RIGHT JOIN Levels " +
			"ON Levels.ID=Runs.LevelID " +
			"WHERE Levels.Code=? AND Runs.CategoryID=? AND Runs.Verified=1 " +
			"ORDER BY Runs.RunTime, Runs.Date";
	private static final String categoriesQuery_levelID =
			"SELECT ID, Category, Victory, Difficulty, Cheats, TimingStart, TimingEnd, MoreRules " +
			"FROM Categories " +
			"WHERE LevelID=?";
	private static final String categoriesQuery_levelCode =
			"SELECT Categories.ID, Categories.Category, Categories.Victory, Categories.Difficulty, Categories.Cheats, Categories.TimingStart, Categories.TimingEnd, Categories.MoreRules " +
			"FROM Categories " +
			"LEFT JOIN Levels " +
			"ON Categories.LevelID=Levels.ID " +
			"WHERE Levels.Code=?";
	private static final String levelQuery =
			"SELECT ID, Author, Name, Description, Download " +
			"FROM Levels " +
			"WHERE Code=? AND Verified=1";
	private static final String allCategoriesQuery =
			"SELECT Levels.ID, Levels.Author, Levels.Name, Categories.ID, Categories.Category, Categories.Difficulty " +
			"FROM Levels " +
			"LEFT JOIN Categories " +
			"ON Levels.ID = Categories.LevelID " +
			"ORDER BY Levels.ID";
	private static final String pendingRunsQuery =
			"SELECT Runs.ID, Runs.RunTime, Players.Name, Players.Link, Runs.Date, Runs.Video, " +
					"Levels.Author, Levels.Name, " +
					"Categories.Category, Categories.Difficulty " +
			"FROM Runs " +
			"LEFT JOIN Players " +
			"ON Players.ID=Runs.PlayerID " +
			"LEFT JOIN Levels " +
			"ON Levels.ID=Runs.LevelID " +
			"LEFT JOIN Categories " +
			"ON Categories.LevelID=Runs.LevelID AND Categories.ID=Runs.CategoryID " + 
			"WHERE Levels.Verified=1 AND (Runs.Verified IS NULL OR Runs.Verified=0)";
	private static final String pendingRunsQuery_levelCode =
			"SELECT Runs.ID, Runs.RunTime, Players.Name, Players.Link, Runs.Date, Runs.Video " +
			"FROM Runs " +
			"RIGHT JOIN Players " +
			"ON Players.ID=Runs.PlayerID " +
			"RIGHT JOIN Levels " +
			"ON Levels.ID=Runs.LevelID " +
			"WHERE Levels.Code=? AND Levels.Verified=1 AND CategoryID=? AND (Runs.Verified IS NULL OR Runs.Verified=0)";
	
	// TODO make this private again
	static Connection databaseConnection() throws SQLException
	{
		// Load the JDBC driver for ConnectorJ
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// Obtain the connection
		SQLPasswords.henna();
		return DriverManager.getConnection("jdbc:mysql://localhost/ksdata", "Henna", SQLPasswords.henna());
	}
	
	@Deprecated
	protected static JSONArray fetchRunsJSON(int levelID, int categoryID, boolean includeObsolete) throws SQLException
	{
		PreparedStatement statement = databaseConnection().prepareStatement(runsQuery_levelID);
		statement.setInt(1, levelID);
		statement.setInt(2, categoryID);
		ResultSet sqlResult = statement.executeQuery();
		ArrayList<Run> runsList = Run.runsFromSQLResult(sqlResult, "tpPdv", !includeObsolete);
		JSONArray jsonResult = new JSONArray();
		for(Run r : runsList)
			jsonResult.put(r.toJSON());
		return jsonResult;
	}
	
	protected static JSONArray fetchRunsJSON(String levelCode, int categoryID, boolean includeObsolete, boolean includePending) throws SQLException
	{
		PreparedStatement statement = databaseConnection().prepareStatement(runsQuery_levelCode);
		statement.setString(1, levelCode);
		statement.setInt(2, categoryID);
		ResultSet sqlResult = statement.executeQuery();
		ArrayList<Run> runsList = Run.runsFromSQLResult(sqlResult, "tpPdv", !includeObsolete);
		
		JSONArray jsonResult = new JSONArray();
		for(Run r : runsList)
			jsonResult.put(r.toJSON());
		
		if (includePending)
		{
			statement = databaseConnection().prepareStatement(pendingRunsQuery_levelCode);
			statement.setString(1, levelCode);
			statement.setInt(2, categoryID);
			sqlResult = statement.executeQuery();
			ArrayList<Run> pendingRunsList = Run.runsFromSQLResult(sqlResult, "itpPdv", !includeObsolete);
			
			for(Run r : pendingRunsList)
			{
				JSONObject runJSON = r.toJSON();
				runJSON.put("pending", "true");
				jsonResult.put(runJSON);
			}
		}
		
		return jsonResult;
	}
	
	private static ArrayList<Category> fetchCategories(PreparedStatement statement) throws SQLException
	{
		ResultSet result = statement.executeQuery();
		ArrayList<Category> categories = new ArrayList<Category>();
		while(result.next())
		{
			categories.add(new Category(
					result.getInt(1),		// ID
					result.getString(2),	// Category
					result.getString(3),	// Victory
					result.getString(4),	// Difficulty
					result.getInt(5) != 0,	// Cheats
					result.getString(6),	// TimingStart
					result.getString(7),	// TimingEnd
					result.getString(8)		// MoreRules
			));
		}
		return categories;
	}
	
	protected static JSONArray fetchCategoriesJSON(int levelID) throws SQLException
	{
		PreparedStatement statement = databaseConnection().prepareStatement(categoriesQuery_levelID);
		statement.setInt(1, levelID);
		ArrayList<Category> categories = fetchCategories(statement);
		JSONArray result = new JSONArray();
		for(Category c : categories)
			result.put(c.toJSON());
		if (result.length() == 0)
			// Only any%
			result.put(Category.defaultAny().toJSON());
		return result;
	}
	
	protected static JSONArray fetchCategoriesJSON(String levelCode) throws SQLException
	{
		PreparedStatement statement = databaseConnection().prepareStatement(categoriesQuery_levelCode);
		statement.setString(1, levelCode);
		ArrayList<Category> categories = fetchCategories(statement);
		JSONArray result = new JSONArray();
		for(Category c : categories)
			result.put(c.toJSON());
		if (result.length() == 0)
			// Only any%
			result.put(Category.defaultAny().toJSON());
		return result;
	}
	
	protected static Level fetchLevel(String code) throws SQLException
	{
		PreparedStatement statement = databaseConnection().prepareStatement(levelQuery);
		statement.setString(1, code);
		ResultSet result = statement.executeQuery();
		if (result.next())
		{
			Level l = new Level(
					result.getInt(1),		// ID
					code,
					result.getString(2),	// Author
					result.getString(3),	// Name
					result.getString(4),	// Description
					result.getString(5)		// Download
			);
			return l;
		}
		return null;
	}
	
	public static boolean levelExists(String code) throws SQLException
	{
		PreparedStatement statement = databaseConnection().prepareStatement(levelQuery);
		statement.setString(1, code);
		ResultSet result = statement.executeQuery();
		return result.next();
	}
	
	public static JSONArray fetchCategoriesByLevelJSON() throws SQLException
	{
		PreparedStatement statement = databaseConnection().prepareStatement(allCategoriesQuery);
		ResultSet result = statement.executeQuery();
		JSONArray returnArray = new JSONArray();
		int levelID;
		int lastLevelID = 0;
		while (result.next())
		{
			levelID = result.getInt(1);
			if (levelID != lastLevelID)
			{
				lastLevelID = levelID;
				JSONObject level = new JSONObject();
				level.put("id", levelID);
				level.put("name", Util.htmlEscape(result.getString(2) + " - " + result.getString(3)));
				JSONArray categories = new JSONArray();
				level.put("categories", categories);
				returnArray.put(level);
			}
			Category category = new Category(result.getInt(4), result.getString(5), result.getString(6));
			JSONObject lastLevel = (JSONObject) returnArray.get(returnArray.length()-1);
			JSONArray categories = (JSONArray) lastLevel.get("categories");
			categories.put(category.toJSON());
		}
		return returnArray;
	}
	
	protected static JSONArray fetchPendingRuns() throws SQLException
	{
		PreparedStatement statement = databaseConnection().prepareStatement(pendingRunsQuery);
		ResultSet sqlResult = statement.executeQuery();
		/*
		JSONArray returnJSON = new JSONArray();
		while(result.next())
		{
			Level level = new Level(
					-1,
					null,
					result.getString(7),	// Author
					result.getString(8),	// Name
					null,
					null
			);
			Category category = new Category(
					-1,
					result.getString(9),	// Category
					result.getString(10)		// Difficulty
			);
			int runTime = result.getObject(2) == null ? -1 : result.getInt(2);
			Run r = new Run(
					result.getInt(1),		// Pending Run ID
					level,
					category,
					runTime,
					result.getString(3),	// Player
					result.getString(4),	// Player link
					result.getDate(5),		// Date
					result.getString(6)		// Video
			);
			returnJSON.put(r.toJSON());
		}
		return returnJSON;
		*/
		ArrayList<Run> runsList = Run.runsFromSQLResult(sqlResult, "itpPdvancD", false);
		JSONArray jsonResult = new JSONArray();
		for(Run r : runsList)
			jsonResult.put(r.toJSON());
		return jsonResult;
	}
	
	public static JSONArray fetchRunsByCategory(String levelCode, boolean includeObsolete, boolean includePending) throws SQLException
	{
		// Fetch the categories
		JSONArray categoriesJSON = HennaSQL.fetchCategoriesJSON(levelCode);
		int numberOfCategories = categoriesJSON.length();
		
		// Retrieve runs for each category
		JSONArray runsJSON = new JSONArray();
		for(int category = 1; category <= numberOfCategories; category++)
		{
			JSONObject categoryJSON = new JSONObject();
			categoryJSON.put("id", category);
			categoryJSON.put("data", HennaSQL.fetchRunsJSON(levelCode, category, true, includePending));
			runsJSON.put(categoryJSON);
		}
		
		return runsJSON;
	}
}
