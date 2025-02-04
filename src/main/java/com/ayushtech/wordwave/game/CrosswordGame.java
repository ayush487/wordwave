package com.ayushtech.wordwave.game;

import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.ayushtech.wordwave.dbconnectivity.LevelsDao;
import com.ayushtech.wordwave.util.UtilService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CrosswordGame {

	private static Random random = new Random();

	private long userId;
	private final int asciA = 97;
	private final int levelNumber;
	private boolean usedHint;
	private final Level currentLevel;
	private long messageId;
	private final MessageChannel channel;
	private Set<String> enterredWords;
	private final int[] letterCounts = new int[26];
	private final List<String> extraWords;

	public CrosswordGame(long userId, Level level, MessageChannel channel) {
		this.userId = userId;
		this.levelNumber = level.getLevel();
		this.channel = channel;
		this.currentLevel = level;
		this.usedHint = false;
		this.enterredWords = new HashSet<String>();
		this.extraWords = new ArrayList<String>();
		for (char c : level.getAllowedLetterList()) {
			letterCounts[(int) c - asciA]++;
		}
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(String.format("Level %d", levelNumber));
		eb.setDescription(getGridFormated());
		eb.setColor(Color.yellow);
		StringBuilder sb = new StringBuilder(level.getAllowedLetters());
		sb.append(String.format("\nMinimum Word Size : `%d`", level.getMinWordSize()));
		sb.append(String.format("\nMaximum Word Size : `%d`", level.getMaxWordSize()));
		eb.addField("__Allowed Letters__", sb.toString(), false);
		this.channel.sendMessageEmbeds(eb.build())
				.addActionRow(
						Button.primary("shuffleCrossword_" + userId,
								Emoji.fromFormatted("<:refresh:1209076086185656340>")),
						Button.primary("hintCrossword_" + userId, "💡 (Free)"))
				.addActionRow(Button.danger("quitCrossword_" + userId, "Quit"),
						Button.primary("extraWords", "Extra Words"))
				.queue(message -> this.messageId = message.getIdLong());
	}

	public void updateGame(CorrectWordResponse response) {
		this.currentLevel.updateUnsolvedGrid(response);
		if (response.levelCompleted()) {
			completeThisLevel();
			this.channel.sendMessage("You completed Level " + levelNumber + " :tada:")
					.addActionRow(Button.primary("newCrossword_" + userId, "Play Next Level")).queue();
		} else {
			updateEmbed();
		}
	}

	public void quitGame(ButtonInteractionEvent event) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(String.format("Level %d", levelNumber));
		eb.setDescription(getGridFormated());
		eb.addField("__Allowed Letters__", this.currentLevel.getAllowedLetters(), false);
		eb.setColor(Color.red);
		eb.setFooter("Game quit!");
		event.editMessageEmbeds(eb.build()).setActionRow(Button.primary("newCrossword_" + userId, "Start New Game"))
				.queue();
		CompletableFuture.runAsync(() -> {
			LevelsDao.getInstance().updateExtraWordCount(userId, extraWords.size(), false);
		});
	}

	public void cancelGame() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(String.format("Level %d", levelNumber));
		eb.setDescription(getGridFormated());
		eb.addField("__Allowed Letters__", this.currentLevel.getAllowedLetters(), false);
		eb.setColor(Color.red);
		eb.setFooter("Game cancelled!");
		this.channel.editMessageEmbedsById(messageId, eb.build())
				.setActionRow(Button.danger("cancelled", "Cancelled").asDisabled()).queue();
		CompletableFuture.runAsync(() -> {
			LevelsDao.getInstance().updateExtraWordCount(userId, extraWords.size(), false);
		});
	}

	private void completeThisLevel() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(String.format("Level %d", levelNumber));
		eb.setDescription(getGridFormated());
		eb.addField("__Allowed Letters__", this.currentLevel.getAllowedLetters(), false);
		eb.setColor(Color.green);
		eb.setFooter("Level Completed!");
		this.channel.editMessageEmbedsById(messageId, eb.build())
				.setActionRow(Button.success("complete", "Level Completed").asDisabled()).queue();
	}

	private void updateEmbed() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(String.format("Level %d", levelNumber));
		eb.setDescription(getGridFormated());
		eb.setColor(Color.yellow);
		StringBuilder sb = new StringBuilder(currentLevel.getAllowedLetters());
		sb.append(String.format("\nMinimum Word Size : `%d`", currentLevel.getMinWordSize()));
		sb.append(String.format("\nMaximum Word Size : `%d`", currentLevel.getMaxWordSize()));

		eb.addField("__Allowed Letters__", sb.toString(), false);
		this.channel.editMessageEmbedsById(messageId, eb.build()).queue();
	}

	private String getGridFormated() {
		char[][] gridUnsolved = currentLevel.getGridUnsolved();
		StringBuilder gridFormatted = new StringBuilder();
		for (char[] column : gridUnsolved) {
			for (char cell : column) {
				gridFormatted.append(UtilService.getInstance().getEmoji(cell));
			}
			gridFormatted.append("\n");
		}
		return gridFormatted.toString();
	}

	public boolean activateHint() {
		List<CrosswordPointer> emptyPositionList = new ArrayList<CrosswordPointer>();
		for (int i = 0; i < currentLevel.getColumns(); i++) {
			for (int j = 0; j < currentLevel.getRows(); j++) {
				if (currentLevel.getGridUnsolved()[i][j] == '+') {
					emptyPositionList.add(new CrosswordPointer(i, j));
				}
			}
		}
		if (emptyPositionList.size() == 0) {
			return false;
		} else {
			var pointer = emptyPositionList.get(random.nextInt(emptyPositionList.size()));
			currentLevel.unlockLetter(pointer.i(), pointer.j());
			updateEmbed();
			usedHint = true;
			checkIfWordCompleted();
			return true;
		}
	}

	public CorrectWordResponse checkWord(String word) {
		var res = currentLevel.checkWord(word);
		if (res.isCorrect()) {
			enterredWords.add(word);
			checkIfWordCompleted();
		}
		return res;
	}

	public long getChannelId() {
		return this.channel.getIdLong();
	}

	public int getLevel() {
		return this.levelNumber;
	}

	public void shuffleAllowedLetters(ButtonInteractionEvent event) {
		currentLevel.shuffleAllowedLetters();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(String.format("Level %d", levelNumber));
		eb.setDescription(getGridFormated());
		eb.setColor(Color.yellow);
		StringBuilder sb = new StringBuilder(currentLevel.getAllowedLetters());
		sb.append(String.format("\nMinimum Word Size : `%d`", currentLevel.getMinWordSize()));
		sb.append(String.format("\nMaximum Word Size : `%d`", currentLevel.getMaxWordSize()));

		eb.addField("__Allowed Letters__", sb.toString(), false);
		event.editMessageEmbeds(eb.build()).queue();
	}

	public void addAnswerredWords(String word) {
		enterredWords.add(word);
	}

	public boolean isWordAnswerred(String word) {
		return enterredWords.contains(word);
	}

	public boolean isWordSuitable(String word) {
		if (word.length() < currentLevel.getMinWordSize() || word.length() > currentLevel.getMaxWordSize()) {
			return false;
		}
		int[] tempAllowedLetters = letterCounts.clone();
		for (int i = 0; i < word.length(); i++) {
			int c = (int) word.charAt(i) - asciA;
			if (tempAllowedLetters[c] <= 0) {
				return false;
			} else {
				tempAllowedLetters[c]--;
			}
		}
		extraWords.add(word);
		return true;
	}

	private void checkIfWordCompleted() {
		CompletableFuture.runAsync(() -> {
			boolean isLevelCompleted = currentLevel.checkExtraWordCompletion();
			if (isLevelCompleted) {
				CrosswordGameHandler.getInstance().removeGame(userId);
				completeThisLevel();
				this.channel.sendMessage("You completed Level " + levelNumber + " :tada:")
						.addActionRow(Button.primary("newCrossword_" + userId, "Play Next Level")).queue();
				try {
					LevelsDao.getInstance().promoteUserLevel(userId, levelNumber);
				} catch (SQLException e) {
				}
			}
		});
	}

	public List<String> getExtraWords() {
		return this.extraWords;
	}

	public boolean hasUsedHint() {
		return this.usedHint;
	}
}
