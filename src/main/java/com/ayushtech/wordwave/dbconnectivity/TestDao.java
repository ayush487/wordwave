package com.ayushtech.wordwave.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TestDao {
  public static void insertText(String text) {
    Connection connection = ConnectionProvider.getConnection();
    try {
      PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO randomtext(randomstrings) VALUES(?)");
      preparedStatement.setString(1, text);
      preparedStatement.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static List<String> getAllWords() {
    Connection connection = ConnectionProvider.getConnection();
    try {
      PreparedStatement preparedStatement = connection.prepareStatement("SELECT words FROM wordlist");
      ArrayList<String> words = new ArrayList<>();
      ResultSet rs = preparedStatement.executeQuery();
      while (rs.next()) {
        words.add(rs.getString("words"));
      }
      return words;
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  public static List<String> getCommonWords() {
    Connection connection = ConnectionProvider.getConnection();
    try {
      PreparedStatement preparedStatement = connection.prepareStatement("select words from commonwords;");
      ArrayList<String> words = new ArrayList<>();
      ResultSet rs = preparedStatement.executeQuery();
      while (rs.next()) {
        words.add(rs.getString("words"));
      }
      return words;
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }
}
