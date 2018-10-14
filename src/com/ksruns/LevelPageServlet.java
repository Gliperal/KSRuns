package com.ksruns;

import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class LevelPageServlet extends HttpServlet
{
	private static final long serialVersionUID = 3119710684211144807L;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, java.io.IOException
	{
		// Ready servlet context
		ServletContext sc = getServletContext();
		
		// Ready properties file
		Properties.initialize(sc);
		
		// Build html
		String html;
		try
		{
			// read request data
			String query = req.getQueryString();
			System.out.println("query = " + query);
			System.out.println("contextPath = " + req.getContextPath());
			System.out.println("servletPath = " + req.getServletPath());
			System.out.println("pathInfo = " + req.getPathInfo());
			
			// Get path info
			String pathInfo = req.getPathInfo();
			
			// If it's just '/'
			if (pathInfo.length() == 1)
			{
				// ===== NO LEVEL CODE =====
				html = Util.inputStreamToString(sc.getResourceAsStream(Properties.getPage("Redirect")));
				
				// Redirect to homepage
				html = html.replaceAll("=LOCATION=", "/");
				html = Util.insertPaths(html);
			}
			else
			{
				// Fetch the level info
				String levelCode = pathInfo.substring(1);
				Level level = HennaSQL.fetchLevel(levelCode);
				
				if (level == null)
				{
					// ===== UNMATCHED LEVEL CODE =====
					// TODO
					html = "<html><body>Couldn't find that level code.</body></html>";
				}
				else
				{
					// ===== REGULAR SERVICE =====
					// Fetch the categories
					JSONArray categoriesJSON = HennaSQL.fetchCategoriesJSON(levelCode);
					
					// Fetch the runs and package into JSON
					JSONArray runsJSON = HennaSQL.fetchRunsByCategory(levelCode, true, false);
					/*
					JSONArray runsJSON = new JSONArray();
					int numberOfCategories = categoriesJSON.length();
					if (numberOfCategories == 0)
					{
						// Only any%
						categoriesJSON.put(Category.defaultAny().toJSON());
						numberOfCategories = 1;
					}
					for(int category = 1; category <= numberOfCategories; category++)
					{
						JSONObject categoryJSON = new JSONObject();
						categoryJSON.put("id", category);
						categoryJSON.put("data", HennaSQL.fetchRunsJSON(levelCode, category, true));
						runsJSON.put(categoryJSON);
					}
					*/
					
					// Build the response HTML
					html = Util.inputStreamToString(sc.getResourceAsStream(Properties.getPage("LevelPage")));

					html = html.replaceAll("=SCRIPTS/LOGINBAR=", Properties.getScript("LoginBar"));
					html = html.replaceAll("=SCRIPTS/RUNUTIL=", Properties.getScript("RunUtil"));
					html = html.replaceAll("=SCRIPTS/VERIFICATION=", Properties.getScript("Verification"));
					html = html.replaceAll("=CSS/LOGINBAR=", Properties.getStyle("LoginBar"));
					html = html.replaceAll("=LEVELCODE=", "" + levelCode);
					html = html.replaceAll("=RUNSJSON=", runsJSON.toString());
					html = html.replaceAll("=CATEGORIESJSON=", categoriesJSON.toString());
					html = html.replaceAll("=WORLDFOLDER=", level.worldFolder());
					html = html.replaceAll("=LEVELICON=", "" + level.iconPath());
					LevelBackdrop lb = level.backdrop();
					html = html.replaceAll("=LEVELBACKDROP=", lb.path());
					html = html.replaceAll("=BACKDROPPOSITION=", lb.position());
					html = Util.insertPaths(html);
				}
			}
		} catch(Exception e)
		{
			html = StackTracePage.exceptionToHTML(sc, e);
		}
		
		// write response headers
		resp.setContentType("text/html"); // uses MIME type
		resp.setContentLength(html.length());
		
		// get the response's writer / output stream
		PrintWriter out = resp.getWriter();
		
		// write the response data
	    out.println(html);
	}
}
