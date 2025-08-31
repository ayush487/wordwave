package com.ayushtech.wordwave.dbconnectivity;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class VoterDao {

  private static VoterDao voterDao = null;

  private VoterDao() {
  }

  public static synchronized VoterDao getInstance() {
    if (voterDao == null) {
      voterDao = new VoterDao();
    }
    return voterDao;
  }

  public void addVoter(long voterId) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(String.format("INSERT INTO vote_data (voter_id, time) values (%d, %d);", voterId,
          System.currentTimeMillis()));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}

