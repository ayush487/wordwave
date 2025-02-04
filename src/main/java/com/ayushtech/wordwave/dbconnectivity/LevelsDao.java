package com.ayushtech.wordwave.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import com.ayushtech.wordwave.game.Level;

public class LevelsDao {

	private static LevelsDao instance = null;

	private LevelsDao() {
	}

	public static LevelsDao getInstance() {
		if (instance == null) {
			instance = new LevelsDao();
		}
		return instance;
	}

	public Level getUserCurrentLevel(long userId) throws SQLException {
		Connection conn = ConnectionProvider.getConnection();
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(String.format(
				"INSERT INTO users (id, level) SELECT %d, 1 WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = %d);",
				userId, userId));
		ResultSet rs = stmt.executeQuery(
				"SELECT * FROM levels JOIN users ON levels.level=users.level WHERE users.id=" + userId + ";");
		if (rs.next()) {
			return new Level(rs.getInt("level"), rs.getString("main_word"), rs.getString("words"),
					rs.getString("level_data"));
		}
		return null;
	}

	public void promoteUserLevel(long userId, int level) throws SQLException {
		Connection conn = ConnectionProvider.getConnection();
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(String.format("UPDATE users SET level=%d WHERE id=%d;", level + 1, userId));
	}

	public Set<String> getAllWords() {
		Connection connection = ConnectionProvider.getConnection();
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT words FROM wordlist;");
			Set<String> words = new HashSet<>();
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

	public int getUserBalance(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT coins FROM users where id=" + userId + ";");
			if (rs.next()) {
				return rs.getInt("coins");
			}
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public void deductUserBalance(long userId, int coins) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			stmt.executeUpdate(String.format("UPDATE users SET coins = coins - %d where id=%d;", coins, userId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getExtraWordsNumber(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format("SELECT extra_words FROM users WHERE id=%d", userId));
			if (rs.next()) {
				return rs.getInt("extra_words");
			}
			return 0;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void updateExtraWordCount(long userId, int count, boolean increment) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			stmt.executeUpdate(String.format("UPDATE users SET extra_words = extra_words %s %d WHERE id=%d;",
					increment ? "+" : "-", count, userId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void claimCoinsWithExtraWords(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format(
					"UPDATE users set extra_words=0, coins = coins + 25 WHERE id=%d and extra_words >= 25;", userId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
