package com.ksruns;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONObject;

public class Run
{
	private int id = -1;
	private String level = null;
	private String category = null;
	private int time;
	private String player;
	private String playerLink;
	private Date date;
	private String video;
	
	private Run() {}
	
	public Run(int id, Level level, Category category, Integer time, String player, String playerLink, Date date, String video)
	{
		this.id = id;
		this.level = level.worldFolder();
		this.category = category.getDescriptor();
		if (time == null)
			this.time = -1;
		else
			this.time = time;
		this.player = player;
		this.playerLink = playerLink;
		this.date = date;
		this.video = video;
	}
	
	public Run(Integer time, String player, String playerLink, Date date, String video)
	{
		if (time == null)
			this.time = -1;
		else
			this.time = time;
		this.player = player;
		this.playerLink = playerLink;
		this.date = date;
		this.video = video;
	}

	public JSONObject toJSON()
	{
		JSONObject returnObject = new JSONObject();
		returnObject.put("runTime", time);
		returnObject.put("player", Util.htmlEscape(player));
		returnObject.put("playerLink", Util.htmlEscape(playerLink));
		returnObject.put("date", date);
		returnObject.put("video", Util.htmlEscape(video));
		if (level != null)
			returnObject.put("level", level);
		if (category != null)
			returnObject.put("category", category);
		if (id != -1)
			returnObject.put("id", id);
		return returnObject;
	}
	
	public boolean hasPlayer(String testPlayer)
	{
		return testPlayer.equals(player);
	}
	
	public boolean hasTime(int testTime)
	{
		return time == testTime;
	}
	
	@Deprecated
	public String toTableRow()
	{
		String html = "<tr><td>";
		html += player;
		html += "</td><td>";
		html += time / 3600 + ":" + (time / 60) % 60 + ":" + time % 60;
		html += "</td><td>";
		html += date;
		html += "</td></tr>";
		return html;
	}
	
	public boolean betterTimeThan(Run that)
	{
		if (time == -1)
			return false;
		else if (that.time == -1)
			return true;
		else
			return time < that.time;
	}
	
	public static ArrayList<Run> runsFromSQLResult(ResultSet sqlResult, String layout, boolean bestTimesPerPlayer) throws SQLException
	{
		int column_id = layout.indexOf('i') + 1;
		int column_levelAuthor = layout.indexOf('a') + 1;
		int column_levelName = layout.indexOf('n') + 1;
		int column_category = layout.indexOf('c') + 1;
		int column_difficulty = layout.indexOf('C') + 1;
		int column_time = layout.indexOf('t') + 1;
		int column_player = layout.indexOf('p') + 1;
		int column_playerLink = layout.indexOf('P') + 1;
		int column_date = layout.indexOf('d') + 1;
		int column_video = layout.indexOf('v') + 1;
		
		ArrayList<Run> runs = new ArrayList<Run>();
		while(sqlResult.next())
		{
			Run r = new Run();
			if (column_id != 0)
				r.id = sqlResult.getInt(column_id);
			if (column_levelAuthor != 0 && column_levelName != 0)
				r.level = new Level(-1, null, sqlResult.getString(column_levelAuthor), sqlResult.getString(column_levelName), null, null).worldFolder();
			if (column_category != 0 && column_difficulty != 0)
				r.category = new Category(-1, sqlResult.getString(column_category), sqlResult.getString(column_difficulty)).getDescriptor();
			if (column_time != 0)
				r.time = sqlResult.getObject(column_time) == null ? -1 : sqlResult.getInt(column_time);
			if (column_player != 0)
				r.player = sqlResult.getString(column_player);
			if (column_playerLink != 0)
				r.playerLink = sqlResult.getString(column_playerLink);
			if (column_date != 0)
				r.date = sqlResult.getDate(column_date);
			if (column_video != 0)
				r.video = sqlResult.getString(column_video);
			runs.add(r);
		}
		
		if (bestTimesPerPlayer)
		{
			ArrayList<String> players = new ArrayList<String>();
			ArrayList<Run> bestRuns = new ArrayList<Run>();
			for (Run nextRun : runs)
			{
				int index = players.indexOf(nextRun.player);
				if (index == -1)
				{
					bestRuns.add(nextRun);
					players.add(nextRun.player);
				}
				else
				{
					Run currentRun = bestRuns.get(index);
					if (nextRun.betterTimeThan(currentRun))
					{
						bestRuns.remove(index);
						bestRuns.add(nextRun);
						players.add(nextRun.player);
					}
				}
			}
			return bestRuns;
		}
		else
		{
			return runs;
		}
	}
}
