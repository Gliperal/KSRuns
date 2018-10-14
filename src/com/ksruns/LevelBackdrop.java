package com.ksruns;

public class LevelBackdrop
{
	private String backdrop;
	private String position;
	
	public LevelBackdrop(String filePath)
	{
		backdrop = filePath;
		position = "center top";
	}
	
	public LevelBackdrop(String filePath, String position)
	{
		backdrop = filePath;
		this.position = position;
	}

	public String path()
	{
		return backdrop;
	}

	public String position()
	{
		return position;
	}
}
