package com.ayushtech.wordwave.util;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ayushtech.wordwave.dbconnectivity.UserDao;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class LeaderBoardHandler {
  private static LeaderBoardHandler leaderBoardHandler;

  private LeaderBoardHandler() {
  }

  public static LeaderBoardHandler getInstance() {
    if (leaderBoardHandler == null) {
      leaderBoardHandler = new LeaderBoardHandler();
    }
    return leaderBoardHandler;
  }

  private String[] spaceArray = {
      "",
      " ",
      "  ",
      "   ",
      "    ",
      "     ",
      "      ",
      "       ",
      "        ",
      "         ",
      "          ",
      "           ",
      "            ",
      "             ",
      "              ",
      "               ",
      "                ",
      "                 ",
      "                  ",
      "                   ",
      "                    ",
      "                     "
  };

  public void handleLevelLeaderboardCommand(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    JDA jda = event.getJDA();
    int optInt = 5;
    OptionMapping optSize = event.getOption("size");
    if (optSize != null) {
      optInt = optSize.getAsInt();
    }
    int lbSize = optInt >= 25 ? 25 : (optInt <= 5) ? 5 : optInt;
    CompletableFuture.runAsync(() -> {
      List<UserRecord> levelLeaderBoardData;
      try {
        levelLeaderBoardData = UserDao.getInstance().getTopUsersBasedOnLevel(lbSize);
      } catch (SQLException e) {
        e.printStackTrace();
        event.getHook().sendMessage("Something went wrong!").queue();
        return;
      }
      String leaderboard = getLevelLeaderboard(jda, levelLeaderBoardData);
      event.getHook().sendMessage(leaderboard).queue();
    });
  }

  private String getLevelLeaderboard(JDA jda, List<UserRecord> levelLeaderBoardData) {
    StringBuffer sb = new StringBuffer();
    sb.append("```sql\n");
    sb.append("Top " + levelLeaderBoardData.size() + " players (Based on Levels)\n");
    for (int i = 1; i <= levelLeaderBoardData.size(); i++) {
      String userTagName = jda.retrieveUserById(levelLeaderBoardData.get(i - 1).id())
          .map(user -> user.getName())
          .complete();
      int spaces = (18 - userTagName.length()) > 1 ? (18 - userTagName.length()) : 1;
      sb.append(
          "\n" + i + ". " + userTagName + spaceArray[spaces] + levelLeaderBoardData.get(i - 1).userData());
    }
    sb.append("\n```");
    return sb.toString();
  }
}
