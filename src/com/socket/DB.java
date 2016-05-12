/*
 * This java class establishes the database connection and also creates the table (if it does not exist).
 */
package com.socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {

	public Connection conn = null;
	public Statement stmnt = null;

	public DB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/SampleDDB";
			conn = DriverManager.getConnection(url, "root1", "");
			System.out.println("conn built");
		} catch (SQLException e) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				System.out.println("Creating database...");
				String url = "jdbc:mysql://localhost:3306/";
				conn = DriverManager.getConnection(url, "root", "");
				stmnt = conn.createStatement();
				String sql = "CREATE DATABASE SampleDDB";
				try {
					stmnt.executeUpdate(sql);
				} catch (Exception e1) {
					System.out.println("DB SampleDDB Exists!" + e1);
				}
				String createTable = "CREATE TABLE IF NOT EXISTS SampleDDB.demo (`value_id` int(11) PRIMARY KEY, `entity` int(10) NOT NULL, `attribute` smallint(5) NOT NULL,`store_id` smallint(5) NOT NULL,`entity_id_value` int(10) NOT NULL,`value` int(11) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
				stmnt.executeUpdate(createTable);
				System.out.println("Table created successfully...");
				url = "jdbc:mysql://localhost:3306/SampleDDB";
				conn = DriverManager.getConnection(url, "root1", "");
				System.out.println("conn built");
			} catch (SQLException | ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				System.out.println("Wamp not working,Obsolete PC found!");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public ResultSet runSql(String sql) throws SQLException {
		Statement sta = conn.createStatement();
		return sta.executeQuery(sql);
	}

	public boolean runSql2(String sql) throws SQLException {
		Statement sta = conn.createStatement();
		return sta.execute(sql);
	}

	@Override
	protected void finalize() throws Throwable {
		if (conn != null || !conn.isClosed()) {
			conn.close();
		}
	}
}