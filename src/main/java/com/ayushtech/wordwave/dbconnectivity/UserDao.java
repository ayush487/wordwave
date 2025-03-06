package com.ayushtech.wordwave.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ayushtech.wordwave.util.UserRecord;
import com.ayushtech.wordwave.util.UtilService;

public class UserDao {
	private static UserDao instance = null;

	private UserDao() {
	}

	public static UserDao getInstance() {
		if (instance == null) {
			instance = new UserDao();
		}
		return instance;
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
					"UPDATE users set extra_words=0, coins = coins + 100 WHERE id=%d and extra_words >= 25;", userId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addDailyRewards(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			String todayDate = UtilService.getInstance().getDate();
			stmt.executeUpdate(String.format(
					"INSERT INTO users (id, coins, last_daily) VALUES (%d, %d, '%s') ON CONFLICT(id) DO UPDATE SET coins = coins + excluded.coins, last_daily = excluded.last_daily;",
					userId, 100, todayDate));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateUserLastDailyDate(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			String todayDate = UtilService.getInstance().getDate();
			stmt.executeUpdate("UPDATE users SET last_daily_crossword='" + todayDate + "' WHERE id=" + userId + ";");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public Optional<String> getUserLastDailyDate(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT last_daily FROM users WHERE id=" + userId + ";");
			if (rs.next()) {
				String lastDate = rs.getString("last_daily");
				return Optional.of(lastDate);
			} else {
				return Optional.empty();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	public boolean addWord(String word) throws SQLException {
		Connection conn = ConnectionProvider.getConnection();
		var stmt = conn.createStatement();
		var rs = stmt.executeQuery(String.format("Select * from wordlist where words='%s'", word));
		if (rs.next()) {
			return false;
		}
		stmt.executeUpdate(String.format("Insert INTO wordlist (words) values ('%s');", word));
		return true;

	}

	public boolean removeWord(String word) throws SQLException {
		Connection conn = ConnectionProvider.getConnection();
		var stmt = conn.createStatement();
		var rs = stmt.executeQuery(String.format("Select * from wordlist where words='%s'", word));
		if (rs.next()) {
			stmt.executeUpdate(String.format("Delete from wordlist where words='%s';", word));
			return true;
		}
		return false;

	}

	public List<UserRecord> getTopUsersBasedOnLevel(int limit) throws SQLException {
		Connection conn = ConnectionProvider.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT id,level FROM users ORDER BY level DESC LIMIT " + limit + ";");
		List<UserRecord> records = new ArrayList<>(limit);
		while (rs.next()) {
			records.add(new UserRecord(rs.getLong("id"), rs.getInt("level")-1));
		}
		return records;
	}

}
