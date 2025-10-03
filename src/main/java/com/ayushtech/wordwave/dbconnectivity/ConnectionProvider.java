package com.ayushtech.wordwave.dbconnectivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ConnectionProvider {
  private static Connection connection = null;

  public static Connection getConnection() {
    if (connection == null) {
      try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(
            DBInfo.url,
            DBInfo.user,
            DBInfo.password);
        resetConnectionEveryHour(true);
        return connection;
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    } else {
      return connection;
    }
  }

  private static void resetConnectionEveryHour(boolean firstTime) {
    if (!firstTime) {
      try {
        connection.close();
        connection = DriverManager.getConnection(
            DBInfo.url,
            DBInfo.user,
            DBInfo.password);
      } catch (SQLException e) {
        connection = null;
        e.printStackTrace();
      }
    }
    CompletableFuture.delayedExecutor(60, TimeUnit.HOURS)
        .execute(() -> ConnectionProvider.resetConnectionEveryHour(false));
  }
}
