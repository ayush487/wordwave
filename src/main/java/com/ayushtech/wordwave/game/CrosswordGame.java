package com.ayushtech.wordwave.game;

import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.ayushtech.wordwave.dbconnectivity.LevelsDao;
import com.ayushtech.wordwave.dbconnectivity.UserDao;
import com.ayushtech.wordwave.util.UtilService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CrosswordGame {

	protected static Random random = new Random();

	protected long userId;
	private final int asciA = 97;
	protected final int levelNumber;
	protected boolean usedHint;
	protected final Level currentLevel;
	protected long messageId;
	protected final MessageChannel channel;
	protected Set<String> enterredWords;
	private final int[] letterCounts = new int[26];
	protected List<String> extraWords;

	// Variable to store the current word being formed
	private StringBuilder currentWord = new StringBuilder();
	private boolean[] appendedLetters;

	public CrosswordGame(long userId, Level level, MessageChannel channel, boolean startInstantly) {
		this.userId = userId;
		this.levelNumber = level.getLevel();
		this.channel = channel;
		this.currentLevel = level;
		this.usedHint = false;
		appendedLetters = new boolean[level.getAllowedLetterList().size()];
		this.enterredWords = new HashSet<String>();
		this.extraWords = new ArrayList<String>();
		for (char c : level.getAllowedLetterList()) {
			letterCounts[(int) c - asciA]++;
		}
		if (startInstantly)
			sendGameEmbed();
	}

	protected void sendGameEmbed() {
		this.channel.sendMessageEmbeds(getBeginningEmbed(currentLevel)).setComponents(getComponents())
				.queue(message -> this.messageId = message.getIdLong());
		;
	}

	public Collection<? extends LayoutComponent> getComponents() {
		Collection<ActionRow> components = new ArrayList<>();
		List<ActionRow> letterRows = getLetterRows();
		components.addAll(letterRows);
		components.add(ActionRow.of(
				Button.primary("submitWord_" + userId, "Submit"),
				Button.primary("hintCrossword_" + userId, usedHint ? "💡 (100 🪙)" : "💡 (Free)")));
		components.add(ActionRow.of(
				Button.danger("quitCrossword_" + userId, "Quit"),
				Button.primary("extraWords", "Extra Words")));
		return components;
	}

	protected MessageEmbed getBeginningEmbed(Level level) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(String.format("Level %d", levelNumber));
		eb.setDescription(getGridFormated());
		eb.setColor(Color.yellow);
		StringBuilder sb = new StringBuilder(level.getAllowedLetters());
		sb.append(String.format("\nMinimum Word Size : `%d`", level.getMinWordSize()));
		sb.append(String.format("\nMaximum Word Size : `%d`", level.getMaxWordSize()));
		eb.addField("__Allowed Letters__", sb.toString(), false);
		return eb.build();
	}

	public void updateGame(CorrectWordResponse response) {
		this.currentLevel.updateUnsolvedGrid(response);
		if (response.levelCompleted()) {
			completeThisLevel();
			String messageToSend = levelNumber == 0 ? "Daily Level Completed! :tada:"
					: "You completed Level " + levelNumber + " :tada:";
			this.channel.sendMessage(messageToSend)
					.addActionRow(Button.primary("newCrossword_" + userId, "Play Next Level")).queue();
		} else {
			updateEmbed(null);
			checkIfWordCompleted();
		}
	}

	public void quitGame(ButtonInteractionEvent event) {
		var embed = getEmbed((byte) 1, "Game quit!");
		event.editMessageEmbeds(embed).setActionRow(
				levelNumber == 0 ? Button.primary("dailyCrossword", "Start again")
						: Button.primary("newCrossword_" + userId, "Start New Game"))
				.queue();
		CompletableFuture.runAsync(() -> {
			UserDao.getInstance().updateExtraWordCount(userId, extraWords.size(), false);
		});
	}

	public void cancelGame() {
		var embed = getEmbed((byte) 1, "Game cancelled!");
		this.channel.editMessageEmbedsById(messageId, embed)
				.setActionRow(Button.danger("cancelled", "Cancelled").asDisabled()).queue();
		CompletableFuture.runAsync(() -> {
			UserDao.getInstance().updateExtraWordCount(userId, extraWords.size(), false);
		});
	}

	protected void completeThisLevel() {
		var embed = getEmbed((byte) 0, "Level Completed!");
		this.channel.editMessageEmbedsById(messageId, embed)
				.setActionRow(Button.success("complete", "Level Completed").asDisabled()).queue();
		try {
			LevelsDao.getInstance().promoteUserLevel(userId, levelNumber);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void updateEmbed(String footerText) {
		var embed = getEmbed((byte) 2, footerText);
		this.channel.editMessageEmbedsById(messageId, embed)
				.setComponents(getComponents())
				.queue();
	}

	protected String getGridFormated() {
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

	protected MessageEmbed getEmbed(byte status, String footerText) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(String.format("Level %d", levelNumber));
		eb.setDescription(getGridFormated());
		Color color = status == 0 ? Color.green : status == 1 ? Color.red : Color.yellow;
		eb.setColor(color);
		StringBuilder sb = new StringBuilder("**__Allowed Letters__ : ** " + currentLevel.getAllowedLetters());
		sb.append("\n**__Current Word__ : " + currentWord.toString().toUpperCase() + "**\n");
		// StringBuilder sb = new StringBuilder("**" + this.currentWord.toString().toUpperCase() + "**");
		sb.append(String.format("\nMinimum Word Size : `%d`", currentLevel.getMinWordSize()));
		sb.append(String.format("\nMaximum Word Size : `%d`", currentLevel.getMaxWordSize()));
		eb.addField("Logs", sb.toString(), false);
		
		// eb.addField("__Current Word__", sb.toString(), false);
		if (footerText != null)
			eb.setFooter(footerText);
		return eb.build();
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
			updateEmbed(null);
			usedHint = true;
			checkIfWordCompleted();
			return true;
		}
	}

	public CorrectWordResponse checkWord(String word) {
		var res = currentLevel.checkWord(word);
		if (res.isCorrect()) {
			enterredWords.add(word);
		}
		return res;
	}

	public long getChannelId() {
		return this.channel.getIdLong();
	}

	public int getLevel() {
		return this.levelNumber;
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

	protected void checkIfWordCompleted() {
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

	private List<ActionRow> getLetterRows() {
		var allowedLetters = this.currentLevel.getAllowedLetterList();
		List<ActionRow> letterRows = new ArrayList<>();

		for (int i = 0; i < allowedLetters.size(); i += 5) {
			List<Button> buttons = new ArrayList<>();
			for (int j = i; j < i + 5 && j < allowedLetters.size(); j++) {
				char letter = allowedLetters.get(j);
				if (appendedLetters[j]) {
					buttons.add(Button.primary("appendLetter_" + userId + "_" + j,
							Emoji.fromUnicode(UtilService.getInstance().getDefaultEmoji(letter))).asDisabled());
				} else {
					buttons.add(Button.primary("appendLetter_" + userId + "_" + j,
							Emoji.fromUnicode(UtilService.getInstance().getDefaultEmoji(letter))));
				}
			}
			letterRows.add(ActionRow.of(buttons));
		}
		return letterRows;
	}

	public boolean appendLetterToCurrentWord(String indexStr) {
		int index = Integer.parseInt(indexStr);
		if (appendedLetters[index]) {
			return false;
		}
		appendedLetters[index] = true;
		char c = currentLevel.getAllowedLetterList().get(index);
		currentWord.append(c);
		return true;
	}

	public void resetCurrentWord() {
		for (int i=0;i<appendedLetters.length;i++) {
			appendedLetters[i] = false;
		}
		currentWord.setLength(0);
	}

	public String getCurrentWord() {
		return currentWord.toString();
	}
}
