package com.kt.restful.service;
//package com.kt.restful.service;
//
//import javax.naming.*;
//import javax.sql.*;
//import java.sql.*;
//
//public class DBConnector3 {
//	private static DBConnector3 dbConnector;
//	private Connection con;
//
//	private DBConnector3() {
//		try {
//			dbConnect();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void dbConnect()
//	{
//		try
//		{
//			Context ctx = new InitialContext();
//
//			while(true) {
//				try {
//					DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/postgres");
//
//					if (ds != null) 
//					{
//						con = ds.getConnection();
//					}
//
//					if(con != null)     break;
//				} catch(Exception e) {
//					System.out.println("! DB Connection Fail..." + e);
//					try { Thread.sleep(1000); } catch(InterruptedException ie) {}
//				}
//			}
//		}
//		catch(Exception e) 
//		{
//			e.printStackTrace();
//		}
//	}
//
//	public static DBConnector3 getInstance() {
//		if(dbConnector == null) {
//			dbConnector = new DBConnector3();
//		}
//
//		return dbConnector;
//	}
//
//	public int executeUpdate(String query) {
//		Statement stmt=null;
//		try {
//			int count;
//			stmt = getConnect().createStatement();
//			count = stmt.executeUpdate(query);
//
//			return count;
//		} catch (SQLException e) {
//		} catch (Exception e) {
//		} finally {
//			try {
//				if( stmt!=null)
//					stmt.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			} finally {
//				stmt =null;
//			}
//		}
//
//		return -1;
//	}
//
//	public ResultSet executeQuery(String query) {
//		Statement stmt=null;
//		try {
//			ResultSet rs = null;
//			stmt = getConnect().createStatement();
//			rs = stmt.executeQuery(query);
//
//			return rs;
//		} catch (SQLException e) {
//		} catch (Exception e) {
//		} finally {
//			try {
//				if( stmt!=null)
//					stmt.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			} finally {
//				stmt =null;
//			}
//		}
//
//		return null;
//	}
//
//	public Connection getConnect() {
//		try {
//			if(!isConnected()) {
//				dbConnect();
//			}
//		} catch (Exception e) {
//		}
//
//		return this.con;
//	}
//
//	public boolean isConnected() {
//		try {
//			if(con == null || con.isClosed()) {
//				return false;
//			}
//		} catch(SQLException e) {
//
//			return false;
//		} catch (Exception e) {
//		}
//
//		return true;
//	}
//}
