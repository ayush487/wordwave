package com.ayushtech.wordwave.game;

import java.awt.Color;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.ayushtech.wordwave.dbconnectivity.LevelsDao;
import com.ayushtech.wordwave.util.UtilService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CrosswordGameHandler {

	private static CrosswordGameHandler instance = null;

	private Map<Long, CrosswordGame> gameMap = new HashMap<>();
	private Set<String> allWordList;

	private CrosswordGameHandler() {
		allWordList = LevelsDao.getInstance().getAllWords();
	}

	public static CrosswordGameHandler getInstance() {
		if (instance == null) {
			instance = new CrosswordGameHandler();
		}
		return instance;
	}

	public void handleCrosswordSlashCommand(SlashCommandInteractionEvent event) {
		long userId = event.getUser().getIdLong();
		if (gameMap.containsKey(userId)) {
			event.reply("You already have a active game!\nDo you want to start a new one ?").setEphemeral(true)
					.setActionRow(Button.primary("cancelThenNewCrossword_" + userId, "Start a new game"),
							Button.primary("cancelCrossword_" + userId, "Cancel Older Game"))
					.queue();
			return;
		}
		event.reply("Starting game!").queue();
		try {
			var level = LevelsDao.getInstance().getUserCurrentLevel(userId);
			var game = new CrosswordGame(userId, level, event.getChannel());
			gameMap.put(userId, game);
			CompletableFuture.delayedExecutor(10, TimeUnit.MINUTES).execute(() -> {
				if (!gameMap.containsKey(userId))
					return;
				int gameHashCode = game.hashCode();
				int currentRunningGameHashCode = gameMap.get(userId).hashCode();
				if (gameHashCode == currentRunningGameHashCode) {
					game.cancelGame();
					gameMap.remove(userId);
				}
			});
		} catch (SQLException e) {
			event.getChannel().sendMessage("Something went wrong!\nPlease try again").queue();
			e.printStackTrace();
		}

	}

	public void handleCrosswordTextCommand(MessageReceivedEvent event) {
		long authorId = event.getAuthor().getIdLong();
		if (gameMap.containsKey(authorId)) {
			event.getChannel().sendMessage("You already have a active game!\nDo you want to start a new one ?")
					.setActionRow(Button.primary("cancelThenNewCrossword_" + authorId, "Start a new game"),
							Button.primary("cancelCrossword_" + authorId, "Cancel Older Game"))
					.queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
			return;
		}
		try {
			var level = LevelsDao.getInstance().getUserCurrentLevel(authorId);
			CrosswordGame game = new CrosswordGame(authorId, level, event.getChannel());
			gameMap.put(authorId, game);
			CompletableFuture.delayedExecutor(10, TimeUnit.MINUTES).execute(() -> {
				if (!gameMap.containsKey(authorId))
					return;
				int gameHashCode = game.hashCode();
				int currentRunningGameHashCode = gameMap.get(authorId).hashCode();
				if (gameHashCode == currentRunningGameHashCode) {
					game.cancelGame();
					gameMap.remove(authorId);
				}
			});
		} catch (SQLException e) {
			event.getChannel().sendMessage("Something went wrong!\nPlease try again").queue();
		}
	}

	public void handleCrosswordButton(ButtonInteractionEvent event) {
		event.deferReply(true).queue();
		String buttonOwnerId = event.getComponentId().split("_")[1];
		if (!buttonOwnerId.equals(event.getUser().getId())) {
			event.reply("This Button is not for you!").setEphemeral(true).queue();
			return;
		}
		long userId = event.getUser().getIdLong();
		if (gameMap.containsKey(userId)) {
			event.reply("You already have a active game!\\nDo you want to start a new one ?")
					.setActionRow(Button.primary("cancelThenNewCrossword_" + userId, "Start a new game"),
							Button.primary("cancelCrossword_" + userId, "Cancel Older Game"))
					.queue();
			return;
		}
		event.getHook().sendMessage("Starting game!").queue();
		try {
			Level userLevel = LevelsDao.getInstance().getUserCurrentLevel(userId);
			var game = new CrosswordGame(userId, userLevel, event.getChannel());
			gameMap.put(userId, game);
			CompletableFuture.delayedExecutor(10, TimeUnit.MINUTES).execute(() -> {
				if (!gameMap.containsKey(userId))
					return;
				int gameHashCode = game.hashCode();
				int currentRunningGameHashCode = gameMap.get(userId).hashCode();
				if (gameHashCode == currentRunningGameHashCode) {
					game.cancelGame();
					gameMap.remove(userId);
				}
			});
		} catch (SQLException e) {
			event.getChannel().sendMessage("Something went wrong!\nPlease try again").queue();
			e.printStackTrace();
		}
	}

	public void handleCrosswordQuitButton(ButtonInteractionEvent event) {
		var buttonOwnerId = event.getComponentId().split("_")[1];
		if (!buttonOwnerId.equals(event.getUser().getId())) {
			event.reply("This button is not for you!").setEphemeral(true).queue();
			return;
		}
		if (gameMap.containsKey(event.getUser().getIdLong())) {
			gameMap.get(event.getUser().getIdLong()).quitGame(event);
			gameMap.remove(event.getUser().getIdLong());
		} else {
			event.reply("No game found!").setEphemeral(true).queue();
		}
	}

	public void handleCancelThenNewCrosswordButton(ButtonInteractionEvent event) {
		String buttonOwnerId = event.getComponentId().split("_")[1];
		long userId = event.getUser().getIdLong();
		if (!buttonOwnerId.equals(event.getUser().getId())) {
			event.reply("This button is not for you!").setEphemeral(true).queue();
			return;
		}
		event.deferReply(true).setEphemeral(true).queue();
		if (gameMap.containsKey(userId)) {
			gameMap.get(userId).cancelGame();
			gameMap.remove(userId);
		}
		event.getHook().sendMessage("Starting game!").queue();
		try {
			Level userLevel = LevelsDao.getInstance().getUserCurrentLevel(userId);
			var game = new CrosswordGame(userId, userLevel, event.getChannel());
			gameMap.put(userId, game);
			CompletableFuture.delayedExecutor(10, TimeUnit.MINUTES).execute(() -> {
				int gameHashCode = game.hashCode();
				int currentRunningGameHashCode = gameMap.get(userId).hashCode();
				if (gameHashCode == currentRunningGameHashCode) {
					game.cancelGame();
					gameMap.remove(userId);
				}
			});
		} catch (SQLException e) {
			event.getChannel().sendMessage("Something went wrong!\nPlease try again").queue();
			e.printStackTrace();
		}
	}

	public void handleCrosswordCancelButton(ButtonInteractionEvent event) {
		String buttonOwnerId = event.getComponentId().split("_")[1];
		long userId = event.getUser().getIdLong();
		if (!buttonOwnerId.equals(event.getUser().getId())) {
			event.reply("This button is not for you!").setEphemeral(true).queue();
			return;
		}
		event.deferReply(true).queue();
		CompletableFuture.runAsync(() -> {
			if (gameMap.containsKey(userId)) {
				gameMap.get(userId).cancelGame();
				event.getHook().sendMessage("Older game is cancelled").queue();
				gameMap.remove(userId);
			} else {
				event.getHook().sendMessage("No old game found!").queue();
			}
		});
	}

	public void handleHintButton(ButtonInteractionEvent event) {
		String buttonOwnerId = event.getComponentId().split("_")[1];
		if (!buttonOwnerId.equals(event.getUser().getId())) {
			event.reply("This Button is not for you!").setEphemeral(true).queue();
			return;
		}
		event.deferEdit().queue();
		var game = gameMap.get(event.getUser().getIdLong());
		if (game.hasUsedHint()) {
			int userBalance = LevelsDao.getInstance().getUserBalance(event.getUser().getIdLong());
			if (userBalance < 100) {
				event.getHook().sendMessage("You dont have enough balance to use hint!").setEphemeral(true).queue();
				return;
			}
			if (game.activateHint()) {
				CompletableFuture.runAsync(() -> {
					LevelsDao.getInstance().deductUserBalance(event.getUser().getIdLong(), 100);
				});
			} else {
				event.getHook().sendMessage("No empty space left for hint").setEphemeral(true).queue();
			}
		} else {
			if (game.activateHint()) {
				event.editButton(Button.primary(event.getComponentId(), "💡 (100 🪙)")).queue();
			} else {
				event.getHook().sendMessage("No empty space left for hint").setEphemeral(true).queue();
			}
		}

	}

	public void handleShuffleButton(ButtonInteractionEvent event) {
		String buttonOwnerId = event.getComponentId().split("_")[1];
		if (!buttonOwnerId.equals(event.getUser().getId())) {
			event.reply("This Button is not for you!").setEphemeral(true).queue();
			return;
		}
		var game = gameMap.get(event.getUser().getIdLong());
		game.shuffleAllowedLetters(event);
	}

	public void handleExtraWordButton(ButtonInteractionEvent event) {
		event.deferReply(true).queue();
		long userId = event.getUser().getIdLong();
		int extraWordCount = LevelsDao.getInstance().getExtraWordsNumber(userId);
		extraWordCount = extraWordCount > 25 ? 25 : extraWordCount;
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Extra Words");
		StringBuilder sb = new StringBuilder();
		sb.append("Words : " + extraWordCount + "/25\n");
		sb.append(UtilService.getInstance().getProgressBar(extraWordCount * 4));
		eb.setDescription(sb.toString());
		eb.setColor(Color.green);
		if (gameMap.containsKey(userId)) {
			var game = gameMap.get(userId);
			StringBuilder wordlist = new StringBuilder("```\n");
			game.getExtraWords().forEach(w -> wordlist.append(w + "\n"));
			eb.addField("Current Level Extra Words", wordlist.append("```").toString(), false);
		}
		event.getHook().sendMessageEmbeds(eb.build())
				.addActionRow(extraWordCount >= 25 ? Button.success("claimExtraWords", "Claim")
						: Button.success("claimExtraWords", "Claim").asDisabled())
				.queue();
	}

	public void inspectAnswer(MessageReceivedEvent event) {
		long authorId = event.getAuthor().getIdLong();
		if (gameMap.containsKey(authorId)) {
			var game = gameMap.get(authorId);
			String message = event.getMessage().getContentRaw().toLowerCase();
			var response = game.checkWord(message);
			// If the word is correct for the crossword and first time answerred
			if (response.isCorrect()) {
				event.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue();
				if (event.isFromGuild() && event.getGuild().getSelfMember().hasPermission(event.getGuildChannel(),
						Permission.MESSAGE_MANAGE)) {
					event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
				}
				game.updateGame(response);
				if (response.levelCompleted()) {
					gameMap.remove(authorId);
					try {
						LevelsDao.getInstance().promoteUserLevel(authorId, game.getLevel());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			// If the word is not in the crossword
			else {
				// If the answer is an actual word
				if (allWordList.contains(message)) {
					CompletableFuture.runAsync(() -> {
						// If the word is already answerred
						if (game.isWordAnswerred(message)) {
							event.getMessage().addReaction(Emoji.fromUnicode("U+1F501")).queue();
							if (event.isFromGuild() && event.getGuild().getSelfMember()
									.hasPermission(event.getGuildChannel(), Permission.MESSAGE_MANAGE)) {
								event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
							}
						}
						// If the word is not answerred yet
						else {
							// If the word can be formed using provided letters
							if (game.isWordSuitable(message)) {
								game.addAnswerredWords(message);
								event.getMessage().addReaction(Emoji.fromUnicode("U+1F4DD")).queue();
								if (event.isFromGuild() && event.getGuild().getSelfMember()
										.hasPermission(event.getGuildChannel(), Permission.MESSAGE_MANAGE)) {
									event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
								}
								LevelsDao.getInstance().updateExtraWordCount(authorId, 1, true);
							}
						}
					});
				}
			}
		}
	}

	public void removeGame(long userId) {
		if (gameMap.containsKey(userId))
			gameMap.remove(userId);
	}

	public boolean isActiveGame(long userId, long channelId) {
		if (gameMap.containsKey(userId)) {
			var game = gameMap.get(userId);
			if (game.getChannelId() == channelId) {
				return true;
			}
			return false;
		}
		return false;
	}

}
