package com.ayushtech.wordwave.listeners;

import com.ayushtech.wordwave.game.CrosswordGameHandler;
import com.ayushtech.wordwave.util.MetricService;
import com.ayushtech.wordwave.util.UtilService;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MainListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
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
			event.reply("This command is not ready yet!").setEphemeral(true).queue();
		}

		else if (commandName.equals("disable")) {
			event.reply("This command is not ready yet!").setEphemeral(true).queue();
		}

		else if (commandName.equals("disable_all_channels")) {
			event.reply("This command is not ready yet!").setEphemeral(true).queue();
		}

		else if (commandName.equals("crossword")) {
			CrosswordGameHandler.getInstance().handleCrosswordSlashCommand(event);
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
			UtilService.getInstance().claimExtraWordCoins(event);
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
