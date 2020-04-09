package com.ksruns;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

public class SQLStuff
{
	@org.junit.Test
	public void testFetchRuns()
	{
//		ClassForDoingSQLStuff.fetchRunsJSON(levelID, categoryID, includeObsolete)
//		ClassForDoingSQLStuff.fetchRunsJSON(levelCode, categoryID, includeObsolete)
	}
	
	private void assertMachineCategories(JSONArray machine, boolean includeRules)
	{
		int numCategories = machine.length();
		assertEquals(5, numCategories);
		for (int i = 0; i < numCategories; i++)
		{
			JSONObject category = (JSONObject) machine.get(i);
			if (category.getInt("id") == 1)
			{
				assertEquals("any% easy", category.get("category"));
				if (includeRules)
				{
					assertTrue(category.has("rules"));
					assertTrue(category.has("rulesBrief"));
				}
			}
		}
	}
	
	@org.junit.Test
	public void testFetchCategories() throws SQLException
	{
		JSONArray categories = HennaSQL.fetchCategoriesJSON(8);
		assertMachineCategories(categories, true);
		categories = HennaSQL.fetchCategoriesJSON("machine");
		assertMachineCategories(categories, true);
	}
	
	@org.junit.Test
	public void testFetchAllCategories() throws SQLException
	{
		JSONArray categoriesByLevel = HennaSQL.fetchCategoriesByLevelJSON();
		int numLevels = categoriesByLevel.length();
		assertEquals(Info.numberOfLevels, numLevels);
		for (int i = 0; i < numLevels; i++)
		{
			JSONObject level = categoriesByLevel.getJSONObject(i);
			if(level.get("id").equals(8))
			{
				assertEquals("Nifflas - The Machine", level.get("name"));
				JSONArray categories = level.getJSONArray("categories");
				assertMachineCategories(categories, false);
			}
		}
	}
}
