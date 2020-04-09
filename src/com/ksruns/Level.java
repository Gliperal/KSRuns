package com.ksruns;

import org.json.JSONObject;

public class Level
{
	private int id;
	private String code;
	private String author;
	private String name;
	private String description;
	private String download;
	
	public Level(int id, String code, String author, String name, String description, String download)
	{
		this.id = id;
		this.code = code;
		this.author = author;
		this.name = name;
		this.description = description;
		this.download = download;
	}
	
	public JSONObject toJSON()
	{
		JSONObject returnObject = new JSONObject();
		returnObject.put("id", id);
		returnObject.put("code", Util.htmlEscape(code));
		returnObject.put("author", Util.htmlEscape(author));
		returnObject.put("name", Util.htmlEscape(name));
		returnObject.put("description", Util.htmlEscape(description));
		return returnObject;
	}
	
	public String toString()
	{
		return "Level with ID " + id;
	}
	
	public boolean hasAuthor(String testAuthor)
	{
		if (author == null)
			return false;
		else
			return author.equals(testAuthor);
	}
	
	public boolean hasName(String testName)
	{
		if (name == null)
			return false;
		else
			return name.equals(testName);
	}
	
	public String worldFolder()
	{
		return Util.htmlEscape(author) + " - " + Util.htmlEscape(name);
	}
	
	public String iconPath()
	{
		return Properties.getImage("LevelIcon") + id + ".png";
	}
	
	public static String iconPath_default()
	{
		return Properties.getImage("LevelIcon") + ".png";
	}
	
	/*
	public String backdropPath()
	{
		return Properties.getImage("LevelBackdrop") + id + ".png";
	}
	*/
	
	public LevelBackdrop backdrop()
	{
		return Properties.getLevelBackdrop(id);
	}
	
	/*
	@Deprecated
	public static String backdropPath_default()
	{
		int count = Properties.getIntUhhh("Image_LevelBackdropDefault_count");
		if (count <= 0)
			return Properties.getImage("LevelBackdropDefault") + ".png";
		return Properties.getImage("LevelBackdropDefault") + new Random().nextInt(count) + ".png";
	}
	*/
}
