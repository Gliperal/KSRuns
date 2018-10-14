package com.ksruns;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JuniSQL
{
	private static final String passwordQuery =
			"SELECT Salt, Password " +
			"FROM Login " +
			"WHERE Username=?";
	private static final String userCreateQuery =
			"INSERT INTO Login " +
			"(Username, Salt, Password, SessionID) " +
			"VALUES (?, ?, ?, ?)";
	private static final String userLoginQuery =
			"UPDATE Login " +
			"SET SessionID = ? " +
			"WHERE Username = ?";
	private static final String userLogoutQuery =
			"UPDATE Login " +
			"SET SessionID=NULL " +
			"WHERE Username = ?" +
			"AND SessionID = ?";
	private static final String permissionsQuery =
			"SELECT Permissions " +
			"FROM Login " +
			"WHERE Username = ?" +
			"AND SessionID = ?";
	private static final String addPendingRunQuery =
			"INSERT INTO Pending_runs " +
			"(LevelID, CategoryID, PlayerID, RunTime, Date, Video) " +
			"VALUES (?, ?, ?, ?, ?, ?)";
	private static final String playerIDQuery =
			"SELECT ID " +
			"FROM Players " +
			"WHERE Name = ?";
	private static final String verifyRunQuery0 =
			"SELECT * " + 
			"FROM Pending_runs " + 
			"WHERE ID = ?";
	private static final String verifyRunQuery1 =
			"INSERT INTO Runs " +
			"(LevelID, CategoryID, PlayerID, RunTime, Date, Video) " + 
			"SELECT LevelID, CategoryID, PlayerID, RunTime, Date, Video " + 
			"FROM Pending_runs " + 
			"WHERE ID = ?";
	private static final String verifyRunQuery2 =
			"DELETE FROM Pending_runs " +
			"WHERE ID = ?";
	
	private static Connection databaseConnection() throws SQLException
	{
		// Load the JDBC driver for ConnectorJ
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// Obtain the connection
		return DriverManager.getConnection("jdbc:mysql://localhost/ksdata", "Juni", SQLPasswords.juni());
	}
	
	protected static String[] fetchPasswordStuff(String username)
	{
		try
		{
			PreparedStatement statement = databaseConnection().prepareStatement(passwordQuery);
			statement.setString(1, username);
			ResultSet result = statement.executeQuery();
			String salt = null, encryptedPass = null;
			if (result.next())
			{
				salt = result.getString(1);
				encryptedPass = result.getString(2);
			}
			return new String[] {
					salt,
					encryptedPass
			};
		} catch (SQLException e)
		{
			// TODO
			e.printStackTrace();
			return null;
		}
	}
	
	protected static String createNewUser(String username, String salt, String password)
	{
		String sessionID = PasswordUtils.getSalt(16);
		try
		{
			PreparedStatement statement = databaseConnection().prepareStatement(userCreateQuery);
			statement.setString(1, username);
			statement.setString(2, salt);
			statement.setString(3, password);
			statement.setString(4, sessionID);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			// TODO
			e.printStackTrace();
			return null;
		}
		
		return sessionID;
	}

	protected static String loginUser(String username)
	{
		String sessionID = PasswordUtils.getSalt(16);
		try
		{
			PreparedStatement statement = databaseConnection().prepareStatement(userLoginQuery);
			statement.setString(2, username);
			statement.setString(1, sessionID);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			// TODO
			e.printStackTrace();
			return null;
		}
		
		return sessionID;
	}
	
	protected static boolean verifySession(String username, String sessionID)
	{
		try
		{
			PreparedStatement statement = databaseConnection().prepareStatement(permissionsQuery);
			statement.setString(1, username);
			statement.setString(2, sessionID);
			ResultSet result = statement.executeQuery();
			return result.next();
		}
		catch(SQLException e)
		{
			// TODO
			e.printStackTrace();
			return false;
		}
	}

	public static boolean verifySession(String username, String sessionID, int permissions)
	{
		try
		{
			PreparedStatement statement = databaseConnection().prepareStatement(permissionsQuery);
			statement.setString(1, username);
			statement.setString(2, sessionID);
			ResultSet result = statement.executeQuery();
			if (result.next())
			{
				// TODO Make this a binary flag check type thing
				return result.getInt(1) >= permissions;
			}
			else
				return false;
		}
		catch(SQLException e)
		{
			// TODO
			e.printStackTrace();
			return false;
		}
	}
	
	protected static boolean logoutUser(String username, String sessionID)
	{
		try
		{
			PreparedStatement statement = databaseConnection().prepareStatement(userLogoutQuery);
			statement.setString(1, username);
			statement.setString(2, sessionID);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			// TODO
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	protected static int addPendingRun(int playerID, int levelID, int categoryID, int time, Date date, String video)
	{
		try
		{
			PreparedStatement statement = databaseConnection().prepareStatement(addPendingRunQuery);
			statement.setInt(1, levelID);
			statement.setInt(2, categoryID);
			statement.setInt(3, playerID);
			statement.setInt(4, time);
			statement.setDate(5, date);
			statement.setString(6, video); // TODO does this work if video is null?
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			// TODO
			e.printStackTrace();
			return 1;
		}
		return 0;
	}
	
	protected static int getPlayerID(String username)
	{
		try
		{
			PreparedStatement statement = databaseConnection().prepareStatement(playerIDQuery);
			statement.setString(1, username);
			ResultSet result = statement.executeQuery();
			if (result.next())
				return result.getInt(1);
			else
				return -1;
		}
		catch(SQLException e)
		{
			// TODO
			e.printStackTrace();
			return -2;
		}
	}
	
	protected static boolean verifyPendingRun(int pendingRunID) throws SQLException
	{
		// First make sure there is a run with that ID to verify
		PreparedStatement statement = databaseConnection().prepareStatement(verifyRunQuery0);
		statement.setInt(1, pendingRunID);
		ResultSet pendingRun = statement.executeQuery();
		if (!pendingRun.next())
			return false;
		
		// Add that run to the runs table
		statement = databaseConnection().prepareStatement(verifyRunQuery1);
		statement.setInt(1, pendingRunID);
		statement.executeUpdate();
		
		// Delete it from the pending runs table
		statement = databaseConnection().prepareStatement(verifyRunQuery2);
		statement.setInt(1, pendingRunID);
		statement.executeUpdate();
		return true;
	}
	
	protected static boolean rejectPendingRun(int pendingRunID) throws SQLException
	{
		// First make sure there is a run with that ID to reject
		PreparedStatement statement = databaseConnection().prepareStatement(verifyRunQuery0);
		statement.setInt(1, pendingRunID);
		ResultSet pendingRun = statement.executeQuery();
		if (!pendingRun.next())
			return false;

		// Delete it from the pending runs table
		statement = databaseConnection().prepareStatement(verifyRunQuery2);
		statement.setInt(1, pendingRunID);
		statement.executeUpdate();
		return true;
	}
}