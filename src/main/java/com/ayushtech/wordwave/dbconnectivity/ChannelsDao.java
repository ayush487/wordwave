package com.ayushtech.wordwave.dbconnectivity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ayushtech.wordwave.util.ChannelService;

public class ChannelsDao {
	private static ChannelsDao instance = null;

	private ChannelsDao() {
	}

	public static ChannelsDao getInstance() {
		if (instance == null) {
			instance = new ChannelsDao();
		}
		return instance;
	}

	public Set<Long> getAllDisabledChannels() {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			var rs = stmt.executeQuery("SELECT channel FROM disabled_channels;");
			Set<Long> disabledChannelSet = new HashSet<Long>();
			while (rs.next()) {
				disabledChannelSet.add(rs.getLong("channel"));
			}
			return disabledChannelSet;
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	public boolean addDisableChannel(long channelId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO disabled_channels (channel) values (" + channelId + ");");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean enableChannel(long channelId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM disabled_channels WHERE channel=" + channelId + ";");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addDisableChannel(List<Long> channelIdList) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			stmt.executeUpdate(getQueryToAddMultipleChannels(channelIdList));
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private String getQueryToAddMultipleChannels(List<Long> channelIdList) {
		String query = channelIdList.stream().filter(cId -> !ChannelService.getInstance().isChannelDisabled(cId))
				.map(cid -> "(" + cid + ")")
				.collect(Collectors.joining(",", "Insert into disabled_channels (channel) values ", ";"));
		return query;
	}

}
