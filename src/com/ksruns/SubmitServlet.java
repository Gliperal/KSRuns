package com.ksruns;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//@WebServlet("TODO")
public class SubmitServlet extends HttpServlet
{
	private static final long serialVersionUID = -6049366651367094514L;
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, java.io.IOException
	{
		JSONObject response = new JSONObject();
		
		// read data from request
		String requestData = "";
		String line;
		BufferedReader br = req.getReader();
		while((line = br.readLine()) != null)
			requestData += line;
		JSONObject data = new JSONObject(requestData);
		
		if (!data.has("request"))
		{
			response.put("status", -1);
			response.put("message", "Missing request type.");
		}
		else if (data.get("request").equals("submit"))
			submit(data, response);
		else if (data.get("request").equals("verify"))
		{
			boolean verifyOrReject = false;
			try
			{
				if (data.has("verify"))
					verifyOrReject = data.getBoolean("verify");
			} catch (JSONException e) {}
			processRun(data, response, verifyOrReject);
		}
		else if (data.get("request").equals("pending"))
			pending(data, response);
		else if (data.get("request").equals("allRuns"))
			allRunsIncludingPending(data, response);
		else
		{
			response.put("status", -2);
			response.put("message", "Unknown request type.");
		}
		
		// JSON to string
		String returnJSON = response.toString();
		
		// write response headers
		resp.setContentType("application/json"); // uses MIME type
		resp.setContentLength(returnJSON.length());
		
		// get the response's writer / output stream
		PrintWriter out = resp.getWriter();
		
		// write the response data
	    out.println(returnJSON);
	}
	
	private void submit(JSONObject data, JSONObject response)
	{
		// validate session
		if (
				!data.has("user")
				|| !data.has("sessionID")
				|| !(data.get("user") instanceof String)
				|| !(data.get("sessionID") instanceof String)
		)
		{
			response.put("status", 1);
			response.put("message", "Missing username and/or sessionID.");
		}
		else if (!JuniSQL.verifySession(data.getString("user"), data.getString("sessionID")))
		{
			response.put("status", 2);
			response.put("message", "Invalid sessionID.");
		}
		else if (!data.has("levelID") || !data.has("categoryID") || !data.has("time") || !data.has("date"))
		{
			response.put("status", 3);
			response.put("message", "Missing information.");
		}
		else
		{
			String video = data.has("video") ? data.getString("video") : null;
			java.util.Date date = null;
			try
			{
				int playerID = JuniSQL.getPlayerID(data.getString("user"));
				if (playerID == -1)
				{
					response.put("status", 6);
					response.put("message", "No associated player profile.");
				}
				else if (playerID == -2)
				{
					response.put("status", 7);
					response.put("message", "SQL Error.");
				}
				else
				{
					date = new SimpleDateFormat("yyyy-MM-dd").parse(data.getString("date"));
					int status = JuniSQL.addPendingRun(playerID, data.getInt("levelID"), data.getInt("categoryID"), data.getInt("time"), new java.sql.Date(date.getTime()), video);
					if (status == 0)
					{
						response.put("status", 0);
						response.put("message", "Successfully added run to pending runs list.");
					}
					else
					{
						response.put("status", 5);
						response.put("message", "SQL Error.");
					}
				}
			}
			catch (JSONException | ParseException e)
			{
				response.put("status", 4);
				response.put("message", "Failed to parse date.");
			}
		}
	}
	
	private void processRun(JSONObject data, JSONObject response, boolean verify)
	{
		if (
				!data.has("user")
				|| !data.has("sessionID")
				|| !(data.get("user") instanceof String)
				|| !(data.get("sessionID") instanceof String)
		)
		{
			response.put("status", 1);
			response.put("message", "Missing username and/or sessionID.");
		}
		else if (!JuniSQL.verifySession(data.getString("user"), data.getString("sessionID"), 1))
		{
			response.put("status", 2);
			response.put("message", "Invalid permissions.");
		}
		else
		{
			int pendingRunID = -1;
			try
			{
				pendingRunID = data.getInt("pendingRunID");
			}
			catch(Exception e)
			{
				response.put("status", 3);
				response.put("message", "Bad or missing pendingRunID.");
				return;
			}
			try
			{
				boolean success = false;
				if (verify)
					success = JuniSQL.verifyPendingRun(pendingRunID);
				else
					success = JuniSQL.rejectPendingRun(pendingRunID);
				if (success)
				{
					response.put("status", 0);
					if (verify)
						response.put("message", "Run successfully verified.");
					else
						response.put("message", "Run successfully rejected.");
				}
				else
				{
					response.put("status", 4);
					response.put("message", "Invalid run ID.");
				}
			} catch (SQLException e)
			{
				response.put("status", 5);
				response.put("message", "SQL Error.");
			}
		}
	}
	
	private void pending(JSONObject data, JSONObject response)
	{
		if (
				!data.has("user")
				|| !data.has("sessionID")
				|| !(data.get("user") instanceof String)
				|| !(data.get("sessionID") instanceof String)
		)
		{
			response.put("status", 1);
			response.put("message", "Missing username and/or sessionID.");
		}
		else if (!JuniSQL.verifySession(data.getString("user"), data.getString("sessionID"), 1))
		{
			response.put("status", 2);
			response.put("message", "Invalid permissions.");
		}
		else
		{
			try
			{
				JSONArray pendingRuns = HennaSQL.fetchPendingRuns();
				response.put("status", 0);
				response.put("message", "Runs retrieved successfully.");
				response.put("runs", pendingRuns);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				response.put("status", 3);
				response.put("message", "SQL Error.");
			}
		}
	}
	
	private void allRunsIncludingPending(JSONObject data, JSONObject response)
	{
		if (
				!data.has("user")
				|| !data.has("sessionID")
				|| !(data.get("user") instanceof String)
				|| !(data.get("sessionID") instanceof String)
		)
		{
			response.put("status", 1);
			response.put("message", "Missing username and/or sessionID.");
		}
		else if (!JuniSQL.verifySession(data.getString("user"), data.getString("sessionID"), 1))
		{
			response.put("status", 2);
			response.put("message", "Invalid permissions.");
		}
		else
		{
			if (data.has("levelCode") && data.get("levelCode") instanceof String)
			{
				try
				{
					JSONArray allRuns = HennaSQL.fetchRunsByCategory(data.getString("levelCode"), true, true);
					response.put("status", 0);
					response.put("message", "Runs retrieved successfully.");
					response.put("runs", allRuns);
				}
				catch (SQLException e)
				{
					response.put("status", 3);
					response.put("message", "SQL Error.");
				}
			}
			else
			{
				response.put("status", 4);
				response.put("message", "Missing level code.");
			}
		}
	}
}
