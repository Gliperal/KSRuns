package com.ksruns;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Test
{
	@org.junit.Test
	public void testFetchLevels()
	{
		ArrayList<Level> allLevels = new LevelsServlet().fetchAllLevels();
		assertEquals(Info.numberOfLevels, allLevels.size());
		for(Level l : allLevels)
		{
			if(l.hasAuthor("LPChip"))
				assertTrue(l.hasName("Learning to Jump"));
		}
	}
	
	@org.junit.Test
	public void testLevelsQuery()
	{
		String query = "BADONKABONK";	// TODO
		
		JSONArray results = new LevelsServlet().queryToJSON(query);
		
		assertEquals(Info.numberOfLevels, results.length());
		
		for(int i = 0; i < results.length(); i++)
		{
			JSONObject level = results.getJSONObject(i);
			String author = level.has("author") ? level.getString("author") : null;
			String name = level.getString("name");
			if(author != null && author.equals("LPChip"))
				assertEquals("Learning to Jump", name);
		}
	}

	@org.junit.Test
	public void testRunsQuery() throws SQLException
	{
		String query = "?level=8&category=4";
		
		JSONArray results = new RunsServlet().queryToJSON(query);
		
		assertEquals(6, results.length());
		
		for(int i = 0; i < results.length(); i++)
		{
			JSONObject run = results.getJSONObject(i);
			String player = run.getString("player");
			int time = run.getInt("runTime");
			if(player.equals("Lychrel"))
				assertEquals(1179, time);
		}
	}
}
