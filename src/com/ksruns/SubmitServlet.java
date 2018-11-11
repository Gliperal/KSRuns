package com.ksruns;

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
		JSONObject data = Util.readJSONData(req);
		Response response = new Response();
		
		try
		{
			if (!data.has("request"))
				response.setStatus(ResponseStatus.missing_type);
			else if (data.get("request").equals("submit-level"))
				submitLevel(data, response);
			else if (data.get("request").equals("submit-category"))
				submitCategory(data, response);
			else if (data.get("request").equals("submit-run"))
				submitRun(data, response);
			else if (data.get("request").equals("verify-run"))
			{
				boolean verifyOrReject = false;
				try
				{
					if (data.has("verify"))
						verifyOrReject = data.getBoolean("verify");
				} catch (JSONException e) {}
				processRun(data, response, verifyOrReject);
			}
			else if (data.get("request").equals("pending-run"))
				pending(data, response);
			else if (data.get("request").equals("allRuns"))
				allRunsIncludingPending(data, response);
			else
				response.setStatus(ResponseStatus.unknown_type);
		}
		catch (SQLException e)
		{
			response.setStatus(ResponseStatus.sql_error);
		}
		
		// Send response back to servlet
		if (!response.hasStatus())
			response.setStatus(ResponseStatus.success);
		response.writeToServletResponse(resp);
	}
	
	private boolean checkLogin(JSONObject data, Response response)
	{
		// validate session
		if (
				!data.has("user")
				|| !data.has("sessionID")
				|| !(data.get("user") instanceof String)
				|| !(data.get("sessionID") instanceof String)
		)
		{
			response.setStatus(ResponseStatus.missing_info, "Missing username and/or sessionID.");
			return false;
		}
		else if (!JuniSQL.verifySession(data.getString("user"), data.getString("sessionID")))
		{
			response.setStatus(ResponseStatus.bad_session_id);
			return false;
		}
		return true;
	}
	
	private void submitLevel(JSONObject data, Response response) throws SQLException
	{
		// validate session
		if(!checkLogin(data, response))
			return;
		
		// process pending level
		if (
				!data.has("code") || !(data.get("code") instanceof String) ||
				!data.has("name") || !(data.get("name") instanceof String) ||
				!data.has("author") || !(data.get("author") instanceof String)
		)
			response.setStatus(ResponseStatus.missing_info);
		String description = null;
		if (data.has("description") && data.get("description") instanceof String)
			description = data.getString("description");
		String download = null;
		if (data.has("download") && data.get("download") instanceof String)
			download = data.getString("download");
		
		// Add it
		boolean success = JuniSQL.addPendingLevel(data.getString("code"), data.getString("author"), data.getString("name"), description, download);
		if (!success)
			response.setStatus(ResponseStatus.duplicate_level);
	}
	
	private void submitCategory(JSONObject data, Response response) throws SQLException
	{
		// validate session
		if(!checkLogin(data, response))
			return;
		
		// process pending category
		// TODO
	}
	
	private void submitRun(JSONObject data, Response response) throws SQLException
	{
		// validate session
		if(!checkLogin(data, response))
			return;
		
		// process pending run
		if (!data.has("levelID") || !data.has("categoryID") || !data.has("time") || !data.has("date"))
			response.setStatus(ResponseStatus.missing_info);
		else
		{
			String video = data.has("video") ? data.getString("video") : null;
			java.util.Date date = null;
			try
			{
				int playerID = JuniSQL.getPlayerID(data.getString("user"));
				if (playerID == -1)
					response.setStatus(ResponseStatus.bad_player);
				else
				{
					date = new SimpleDateFormat("yyyy-MM-dd").parse(data.getString("date"));
					JuniSQL.addPendingRun(playerID, data.getInt("levelID"), data.getInt("categoryID"), data.getInt("time"), new java.sql.Date(date.getTime()), video);
				}
			}
			catch (JSONException | ParseException e)
			{
				response.setStatus(ResponseStatus.bad_format);
			}
		}
	}
	
	private void processRun(JSONObject data, Response response, boolean verify) throws SQLException
	{
		if (
				!data.has("user")
				|| !data.has("sessionID")
				|| !(data.get("user") instanceof String)
				|| !(data.get("sessionID") instanceof String)
		)
			response.setStatus(ResponseStatus.missing_info, "Missing username and/or sessionID.");
		else if (!JuniSQL.verifySession(data.getString("user"), data.getString("sessionID"), 1))
			response.setStatus(ResponseStatus.bad_permissions);
		else
		{
			try
			{
				int pendingRunID = data.getInt("pendingRunID");
				boolean success = false;
				if (verify)
					success = JuniSQL.verifyPendingRun(pendingRunID);
				else
					success = JuniSQL.rejectPendingRun(pendingRunID);
				if (success)
				{
					if (verify)
						response.setStatus(ResponseStatus.success, "Run successfully verified.");
					else
						response.setStatus(ResponseStatus.success, "Run successfully rejected.");
				}
				else
					response.setStatus(ResponseStatus.bad_info, "Invalid run ID.");
			}
			catch(Exception e)
			{
				response.setStatus(ResponseStatus.bad_info, "Bad or missing pendingRunID.");
			}
		}
	}
	
	private void pending(JSONObject data, Response response) throws SQLException
	{
		if (
				!data.has("user")
				|| !data.has("sessionID")
				|| !(data.get("user") instanceof String)
				|| !(data.get("sessionID") instanceof String)
		)
			response.setStatus(ResponseStatus.missing_info, "Missing username and/or sessionID.");
		else if (!JuniSQL.verifySession(data.getString("user"), data.getString("sessionID"), 1))
			response.setStatus(ResponseStatus.bad_permissions);
		else
		{
			JSONArray pendingRuns = HennaSQL.fetchPendingRuns();
			response.setStatus(ResponseStatus.success, "Runs retrieved successfully.");
			response.put("runs", pendingRuns);
		}
	}
	
	private void allRunsIncludingPending(JSONObject data, Response response) throws SQLException
	{
		if (
				!data.has("user")
				|| !data.has("sessionID")
				|| !(data.get("user") instanceof String)
				|| !(data.get("sessionID") instanceof String)
		)
			response.setStatus(ResponseStatus.missing_info, "Missing username and/or sessionID.");
		else if (!JuniSQL.verifySession(data.getString("user"), data.getString("sessionID"), 1))
			response.setStatus(ResponseStatus.bad_permissions);
		else
		{
			if (data.has("levelCode") && data.get("levelCode") instanceof String)
			{
				JSONArray allRuns = HennaSQL.fetchRunsByCategory(data.getString("levelCode"), true, true);
				response.setStatus(ResponseStatus.success, "Runs retrieved successfully.");
				response.put("runs", allRuns);
			}
			else
				response.setStatus(ResponseStatus.missing_info, "Missing level code.");
		}
	}
}
