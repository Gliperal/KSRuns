package com.ksruns;

import org.json.JSONObject;

public class Category
{
	private int id;
	private String descriptor;
	private String rules;
	private String rules_short;
	
	public Category(int id, String category, String difficulty)
	{
		// ID
		this.id = id;
		
		// Descriptor category
		if (category == null)
			category = "any%";
		
		// Descriptor difficulty and Rules
		if (difficulty == null || difficulty.equals("any"))
			descriptor = category;
		else if (difficulty.startsWith("<"))
			descriptor = category + " " + difficulty.substring(1);
		else if (difficulty.startsWith(">"))
			descriptor = category + " " + difficulty.substring(1);
		else
			descriptor = category + " " + difficulty;
		
		descriptor = descriptor.toLowerCase();
	}
	
	public Category(int id, String category, String winConditions, String difficulty, boolean cheatsAllowed, String timingStart, String timingEnd, String moreRules)
	{
		// ID
		this.id = id;
		
		// Descriptor category
		if (category == null)
			category = "any%";
		
		// Descriptor difficulty and Rules
		if (winConditions == null)
		{
			rules = "Play the game or something";
			rules_short = "Play the game or something.";
		}
		else if (winConditions.startsWith("WIN"))
		{
			if (winConditions.length() == 3 || winConditions.charAt(3) != ':')
			{
				rules = "Trigger the ending by touching a WIN object";
				rules_short = "Reach the ending";
			}
			else
			{
				rules = "Trigger the " + Util.csvToTextList(winConditions.substring(4), "or", "\"") + " ending by touching a WIN object";
				rules_short = "Trigger the " + Util.csvToTextList(winConditions.substring(4), "or", "\"") + " ending";
			}
		}
		else if (winConditions.startsWith("CUT:"))
		{
			rules = "Trigger the " + Util.csvToTextList(winConditions.substring(4), "or", "\"") + " cutscene";
			rules_short = "Trigger the " + Util.csvToTextList(winConditions.substring(4), "or", "\"") + " cutscene";
		}
		else
		{
			rules = winConditions;
			rules_short = winConditions;
		}
		
		if (moreRules != null && moreRules.startsWith(":"))
		{
			rules += ", " + moreRules.substring(1);
			rules_short += ", " + moreRules.substring(1);
			moreRules = null;
		}
		rules += ". ";
		
		if (difficulty == null || difficulty.equals("any"))
			descriptor = category;
		else if (difficulty.startsWith("<"))
		{
			rules += "The difficulty must be left on " + difficulty.substring(1) + ". ";
			rules_short += ", on " + difficulty.substring(1) + " difficulty";
			descriptor = category + " " + difficulty.substring(1);
		}
		else if (difficulty.startsWith(">"))
		{
			rules += "The difficulty must be changed to " + difficulty.substring(1) + " at the first option, and left there for the remainder of the run. ";
			rules_short += ", on " + difficulty.substring(1) + " difficulty";
			descriptor = category + " " + difficulty.substring(1);
		}
		else
		{
			rules += "The level must be played on " + difficulty + " difficulty. ";
			rules_short += ", on " + difficulty + " difficulty";
			descriptor = category + " " + difficulty;
		}
		rules_short += ". ";
		
		if (moreRules != null)
		{
			rules += moreRules + " ";
			rules_short += moreRules + " ";
		}
		
		if (!cheatsAllowed)
			rules += "Use of cheats is disallowed. ";
		else
			rules_short += "Use of cheats is allowed. ";
		
		rules += "Time starts ";
		if (timingStart == null)
			rules += "on first gameplay input";
		else
			rules += timingStart;
		rules += " and ends ";
		if (timingEnd == null)
		{
			if (winConditions == null)
				rules += "whenever you want it to! ^o^";
			else
			{
				if (winConditions.startsWith("WIN"))
					rules += "when the player touches the final WIN object and the screen begins to fade to white.";
				else if (winConditions != null && winConditions.startsWith("CUT"))
					rules += "when the player touches the final SHIFT object and the screen begins to fade to white.";
				else
					rules += "whenever you want it to! ^o^";
			}
		}
		else
			rules += timingEnd + ".";
		
		descriptor = descriptor.toLowerCase();
	}
	
	public JSONObject toJSON()
	{
		JSONObject returnObject = new JSONObject();
		returnObject.put("id", id);
		returnObject.put("category", Util.htmlEscape(descriptor));
		if (rules != null)
		{
			returnObject.put("rules", Util.htmlEscape(rules));
			returnObject.put("rulesBrief", Util.htmlEscape(rules_short));
		}
		return returnObject;
	}
	
	public String getDescriptor()
	{
		return Util.htmlEscape(descriptor);
	}
	
	public static Category defaultAny()
	{
		return new Category(1, "any%", "WIN", null, false, null, null, null);
	}
}
