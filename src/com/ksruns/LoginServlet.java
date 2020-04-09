package com.ksruns;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

//@WebServlet("TODO")
public class LoginServlet extends HttpServlet
{
	private static final long serialVersionUID = -1504375762086903160L;
	
	private JSONObject create(String username, String password)
	{
		JSONObject response = new JSONObject();
		
		// make database call
		String[] data = JuniSQL.fetchPasswordStuff(username);
		if (data == null)
		{
			response.put("status", 1);
			response.put("message", "SQL Error");
			return response;
		}
		if (data[0] != null)
		{
			response.put("status", 2);
			response.put("message", "User already exists.");
			return response;
		}
		
		// create salt
		String salt = PasswordUtils.getSalt(30);
		
		// hash password
		String hashPass = PasswordUtils.generateSecurePassword(password, salt);
		
		// create user in database and return session ID
		String sessionID = JuniSQL.createNewUser(username, salt, hashPass);
		if (sessionID == null)
		{
			response.put("status", 1);
			response.put("message", "SQL Error");
			return response;
		}
		else
		{
			response.put("status", 0);
			response.put("message", "User created successfully.");
			response.put("sessionID", sessionID);
			return response;
		}
	}
	
	private JSONObject login(String username, String password)
	{
		JSONObject response = new JSONObject();
		
		// make database call
		String[] data = JuniSQL.fetchPasswordStuff(username);
		if (data == null)
		{
			response.put("status", 1);
			response.put("message", "SQL Error");
			return response;
		}
		if (data[0] == null)
		{
			response.put("status", 2);
			response.put("message", "User does not exist.");
			return response;
		}
		
		// check password
		if (!PasswordUtils.verifyUserPassword(password, data[1], data[0]))
		{
			response.put("status", 3);
			response.put("message", "Bad password.");
			return response;
		}
		
		// create session ID
		String sessionID = JuniSQL.loginUser(username);
		
		// build return JSON
		response.put("status", 0);
		response.put("message", "Login successful.");
		response.put("username", username);
		response.put("sessionID", sessionID);
		return response;
	}
	
	private JSONObject logout(String username, String sessionID)
	{
		JSONObject response = new JSONObject();
		if (JuniSQL.logoutUser(username, sessionID))
		{
			response.put("status", 0);
			response.put("message", "Logout successful.");
		}
		else
		{
			response.put("status", 1);
			response.put("message", "SQL Error");
		}
		return response;
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, java.io.IOException
	{
		// read data from request
		String requestData = "";
		String line;
		BufferedReader br = req.getReader();
		while((line = br.readLine()) != null)
			requestData += line;
		JSONObject data = new JSONObject(requestData);
		
		String request = data.has("request") ? data.getString("request") : null;
		String username = data.has("user") ? data.getString("user") : null;
		String password = data.has("password") ? data.getString("password") : null;
		String sessionID = data.has("sessionID") ? data.getString("sessionID") : null;
		
		// do the thing
		JSONObject response;
		switch(request)
		{
		case "create":
			response = create(username, password);
			break;
		case "login":
			response = login(username, password);
			break;
		case "logout":
			response = logout(username, sessionID);
			break;
		default:
			response = new JSONObject();
			response.put("status", 4);
			response.put("message", "No request specified.");
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
}
