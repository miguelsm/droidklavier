package org.droidklavier.db;

import android.util.Log;

import org.droidklavier.BuildConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBClient {

  private static final String TAG = "DBClient";

  private String HOST;
  private final int PORT = 5432;
  private final String DATABASE;
  private final String USER;
  private final String PASS;

  private Connection connection;

  public DBClient(String host, String db, String user, String pass) {
    HOST = host;
    DATABASE = db;
    USER = user;
    PASS = pass;
  }

  public void setHost(String host) {
    HOST = host;
  }

  public ResultSet executeQuery(String query) {
    if (BuildConfig.DEBUG) {
      Log.d(TAG, query);
    }

    ResultSet rs = null;

    try {
      Class.forName("org.postgresql.Driver");
      String conn = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE; //, USER, PASS;
      connection = DriverManager.getConnection(conn, USER, PASS);
      if (connection == null) {
        Log.e(TAG, "FAILED to get connection: " + conn + "," + USER + "," + PASS);
        System.out.println("FAILED to get connection: " + conn+ "," + USER + "," + PASS);
      }
      Statement statement = connection.createStatement();
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
