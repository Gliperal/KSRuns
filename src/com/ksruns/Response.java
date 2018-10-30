package com.ksruns;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class Response
{
	private JSONObject response;
	
	public Response()
	{
		response = new JSONObject();
	}
	
	private void setStatusCode(ResponseStatus status)
	{
		ResponseStatus[] stati = ResponseStatus.values();
		for (int i = 0; i < stati.length; i++)
			if (stati[i] == status)
			{
				response.put("status", i);
				return;
			}
	}
	
	public void setStatus(ResponseStatus status, String message)
	{
		setStatusCode(status);
		response.put("message", message);
	}
	
	public void setStatus(ResponseStatus status)
	{
		setStatusCode(status);
		switch(status)
		{
		case success:
			response.put("message", "Success.");
			break;
		case sql_error:
			response.put("message", "SQL Error.");
			break;
		case missing_type:
			response.put("message", "Missing request type.");
			break;
		case unknown_type:
			response.put("message", "Unknown request type.");
			break;
		case bad_session_id:
			response.put("message", "Invalid sessionID.");
			break;
		case missing_info:
			response.put("message", "Missing information.");
			break;
		case bad_info:
			response.put("message", "Invalid information.");
			break;
		case bad_player:
			response.put("message", "No associated player profile.");
			break;
		case bad_format:
			response.put("message", "Failed to parse data.");
			break;
		case bad_permissions:
			response.put("message", "Invalid permissions.");
			break;
		case duplicate_level:
			response.put("message", "A level already exists with that code.");
		}
	}
	
	public void writeToServletResponse(HttpServletResponse resp) throws IOException
	{
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

	public void put(String key, JSONArray value)
	{
		response.put(key, value);
	}

	public boolean hasStatus()
	{
		return response.has("status") && response.get("status") != null;
	}
}
