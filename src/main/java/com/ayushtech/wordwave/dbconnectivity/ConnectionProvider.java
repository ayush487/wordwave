package com.ayushtech.wordwave.dbconnectivity;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionProvider {
  private static Connection connection = null;

  public static Connection getConnection() {
    if (connection == null) {
      try {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:wordwave.db");
        return connection;
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    } else {
      return connection;
    }
  }
}
