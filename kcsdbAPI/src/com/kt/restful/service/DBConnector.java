package com.kt.restful.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnector {
	private static DBConnector dbConnector;
	private Connection con;

	private DBConnector() {
		try {
			dbConnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dbConnect()
	{
		try
		{
			Class.forName("com.edb.Driver");
			con = DriverManager.getConnection("jdbc:edb://192.168.70.201:5444/kcsdb","enterprisedb","ppas.123");
		}
		catch(Exception e) 
		{
			System.out.println("! DB Connection Fail..." + e);
			try { Thread.sleep(1000); } catch(InterruptedException ie) {}
		}
	}

	public static DBConnector getInstance() {
		if(dbConnector == null) {
			dbConnector = new DBConnector();
		}

		return dbConnector;
	}

	public int executeUpdate(String query) {
		Statement stmt=null;
		try {
			int count;
			stmt = getConnect().createStatement();
			count = stmt.executeUpdate(query);

			return count;
		} catch (SQLException e) {
		} catch (Exception e) {
		} finally {
			try {
				if( stmt!=null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				stmt =null;
			}
		}

		return -1;
	}

	public ResultSet executeQuery(String query) {
		Statement stmt=null;
		try {
			ResultSet rs = null;
			stmt = getConnect().createStatement();
			rs = stmt.executeQuery(query);

			return rs;
		} catch (SQLException e) {
		} catch (Exception e) {
		} finally {
			try {
				if( stmt!=null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				stmt =null;
			}
		}

		return null;
	}

	public Connection getConnect() {
		try {
			if(!isConnected()) {
				dbConnect();
			}
		} catch (Exception e) {
		}

		return this.con;
	}

	public boolean isConnected() {
		try {
			if(con == null || con.isClosed()) {
				return false;
			}
		} catch(SQLException e) {

			return false;
		} catch (Exception e) {
		}

		return true;
	}
}
