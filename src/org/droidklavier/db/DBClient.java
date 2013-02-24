package org.droidklavier.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.droidklavier.BuildConfig;

import android.util.Log;

public class DBClient {

	private static final String TAG = "DBClient";

	private final String HOST;
	private final int PORT = 5432;
	private final String DATABASE;
	private final String USER;
	private final String PASS;

	private Connection connection;
	private Statement statement;

	public DBClient(String host, String db, String user, String pass) {

		HOST = host;
		DATABASE = db;
		USER = user;
		PASS = pass;
	}

	public ResultSet executeQuery(String query) {

		if (BuildConfig.DEBUG) {
			Log.d(TAG, query);
		}

		ResultSet rs = null;

		try {

			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection("jdbc:postgresql://"
					+ HOST + ":" + PORT + "/" + DATABASE, USER, PASS);
			statement = connection.createStatement();
			rs = statement.executeQuery(query);

		} catch (Exception e) {

			disconnect();
			e.printStackTrace();
		}

		return rs;
	}

	protected void disconnect() {

		try {

			if (connection != null && !connection.isClosed()) {
				connection.close();
			}

		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

}
