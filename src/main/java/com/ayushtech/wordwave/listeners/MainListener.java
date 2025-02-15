package com.ayushtech.wordwave.listeners;

import com.ayushtech.wordwave.game.CrosswordGameHandler;
import com.ayushtech.wordwave.util.ChannelService;
import com.ayushtech.wordwave.util.MetricService;
import com.ayushtech.wordwave.util.UserService;
import com.ayushtech.wordwave.util.UtilService;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MainListener extends ListenerAdapter {

	private ChannelService channelService;

	public MainListener() {
		super();
		channelService = ChannelService.getInstance();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot() || channelService.isChannelDisabled(event.getChannel().getIdLong())) {
			return;
		}
		String message = event.getMessage().getContentRaw();
		MetricService.getInstance().registerTextCommand(message);

		if (CrosswordGameHandler.getInstance().isActiveGame(event.getAuthor().getIdLong(),
				event.getChannel().getIdLong())) {
			CrosswordGameHandler.getInstance().inspectAnswer(event);
		}

		if (message.startsWith("!crossword")) {
			CrosswordGameHandler.getInstance().handleCrosswordTextCommand(event);
			return;
		}

	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		MetricService.getInstance().registerSlashCommands(event);
		String commandName = event.getName();

		if (commandName.equals("enable")) {
			channelService.handleEnableCommand(event);
			return;
		}

		else if (commandName.equals("disable")) {
			channelService.handleDisableCommand(event);
			return;
		}

		else if (commandName.equals("disable_all_channels")) {
			channelService.handleDisableAllCommand(event);
			return;
		}

		if (channelService.isChannelDisabled(event.getChannelIdLong())) {
			event.reply("Commands in this channel is disabled!").setEphemeral(true).queue();
			return;
		}

		if (commandName.equals("crossword")) {
			CrosswordGameHandler.getInstance().handleCrosswordSlashCommand(event);
			return;
		}

		else if (commandName.equals("balance")) {
			UserService.getInstance().handleBalanceCommand(event);
			return;
		}

		else if (commandName.equals("daily")) {
			UserService.getInstance().handleDailyCommand(event);
			return;
		}

		else if (commandName.equals("botinfo")) {
			String subcommandName = event.getSubcommandName();
			if (subcommandName.equals("gc")) {
				event.deferReply().queue();
				Runtime.getRuntime().gc();
				event.getHook().sendMessage("Requested for Garbage Collection").queue();
				return;
			} else if (subcommandName.equals("memory")) {
				event.deferReply().queue();
				long totalMemory = Runtime.getRuntime().totalMemory();
				long freeMemory = Runtime.getRuntime().freeMemory();
				long usedMemory = totalMemory - freeMemory;
				event.getHook()
						.sendMessage(String.format(
								"Memory Used : %d\nAvailable Free Memory: %d MB\nTotal Memory in JVM : %d MB",
								usedMemory / (1024 * 1024), freeMemory / (1024 * 1024), totalMemory / (1024 * 1024)))
						.queue();
				return;
			} else {
				MetricService.getInstance().handleMetricCommand(event);
				return;
			}
		}

		else if (commandName.equals("add_words")) {
			UtilService.getInstance().handleAddWordCommand(event);
			return;
		}

		else if (commandName.equals("remove_words")) {
			UtilService.getInstance().handleRemoveWordCommand(event);
			return;
		}

		else if (commandName.equals("extra_words")) {
			CrosswordGameHandler.getInstance().handleExtraWordCommand(event);
			return;
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {

		MetricService.getInstance().registerButtonInteraction(event);

		String buttonId = event.getComponentId();
		if (buttonId.startsWith("newCrossword")) {
			CrosswordGameHandler.getInstance().handleCrosswordButton(event);
			return;
		}

		else if (buttonId.startsWith("quitCrossword")) {
			CrosswordGameHandler.getInstance().handleCrosswordQuitButton(event);
			return;
		}

		else if (buttonId.startsWith("cancelCrossword")) {
			CrosswordGameHandler.getInstance().handleCrosswordCancelButton(event);
			return;
		}

		else if (buttonId.startsWith("hintCrossword")) {
			CrosswordGameHandler.getInstance().handleHintButton(event);
			return;
		}

		else if (buttonId.startsWith("shuffleCrossword")) {
			CrosswordGameHandler.getInstance().handleShuffleButton(event);
			return;
		}

		else if (buttonId.startsWith("extraWords")) {
			CrosswordGameHandler.getInstance().handleExtraWordButton(event);
			return;
		}

		else if (buttonId.startsWith("claimExtraWords")) {
			UserService.getInstance().claimExtraWordCoins(event);
			return;
		}

		else if (buttonId.startsWith("cancelThenNewCrossword")) {
			CrosswordGameHandler.getInstance().handleCancelThenNewCrosswordButton(event);
			return;
		}

	}

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		UtilService.getInstance().notifyGuildJoin(event);
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		UtilService.getInstance().notifyGuildLeave(event);
	}
}
