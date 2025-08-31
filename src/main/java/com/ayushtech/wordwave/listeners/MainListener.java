package com.ayushtech.wordwave.listeners;

import com.ayushtech.wordwave.game.CrosswordGameHandler;
import com.ayushtech.wordwave.util.ChannelService;
import com.ayushtech.wordwave.util.LeaderBoardHandler;
import com.ayushtech.wordwave.util.LevelAppendService;
import com.ayushtech.wordwave.util.MetricService;
import com.ayushtech.wordwave.util.UserService;
import com.ayushtech.wordwave.util.UtilService;
import com.ayushtech.wordwave.util.VotingService;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MainListener extends ListenerAdapter {

    private ChannelService channelService;
    private final long vote_notifs_channel = 1409215253790720050l;

    public MainListener() {
        super();
        channelService = ChannelService.getInstance();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        long channelId = event.getChannel().getIdLong();
        if (channelId == vote_notifs_channel) {
            String voter_id = event.getMessage().getContentDisplay();
            VotingService.getInstance().voteUser(event.getJDA(), voter_id);
            return;
        }

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

        switch (commandName) {
            case "enable":
                channelService.handleEnableCommand(event);
                return;
            case "disable":
                channelService.handleDisableCommand(event);
                return;
            case "disable_all_channels":
                channelService.handleDisableAllCommand(event);
                return;
            default:
                break;
        }

        if (channelService.isChannelDisabled(event.getChannelIdLong())) {
            event.reply("Commands in this channel is disabled!").setEphemeral(true).queue();
            return;
        }

        switch (commandName) {
            case "crossword":
                CrosswordGameHandler.getInstance().handleCrosswordSlashCommand(event);
                return;
            case "help" :
                UserService.getInstance().handleHelpCommand(event);
                return;
            case "balance":
                UserService.getInstance().handleBalanceCommand(event);
                return;
            case "vote":
                VotingService.getInstance().handleVoteCommand(event);
                return;
            case "daily":
                UserService.getInstance().handleDailyCommand(event);
                return;
            case "leaderboards":
                String leaderboardSubcommandName = event.getSubcommandName();
                if (leaderboardSubcommandName.equals("levels")) {
                    LeaderBoardHandler.getInstance().handleLevelLeaderboardCommand(event);
                }
                return;
            case "botinfo":
                String subcommandName = event.getSubcommandName();
                if (subcommandName.equals("gc")) {
                    event.deferReply().queue();
                    Runtime.getRuntime().gc();
                    event.getHook().sendMessage("Requested for Garbage Collection").queue();
                } else if (subcommandName.equals("memory")) {
                    event.deferReply().queue();
                    long totalMemory = Runtime.getRuntime().totalMemory();
                    long freeMemory = Runtime.getRuntime().freeMemory();
                    long usedMemory = totalMemory - freeMemory;
                    event.getHook()
                            .sendMessage(String.format(
                                    "Memory Used : %d\nAvailable Free Memory: %d MB\nTotal Memory in JVM : %d MB",
                                    usedMemory / (1024 * 1024), freeMemory / (1024 * 1024),
                                    totalMemory / (1024 * 1024)))
                            .queue();
                } else {
                    MetricService.getInstance().handleMetricCommand(event);
                }
                break;
            case "add_words":
                UtilService.getInstance().handleAddWordCommand(event);
                break;
            case "remove_words":
                UtilService.getInstance().handleRemoveWordCommand(event);
                break;
            case "extra_words":
                CrosswordGameHandler.getInstance().handleExtraWordCommand(event);
                break;
            case "view_level":
                CrosswordGameHandler.getInstance().handleViewLevelCommand(event);
                break;
            case "add_level":
                LevelAppendService.getInstance().handleLevelAddCommand(event);
                break;
            default:
                break;
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        MetricService.getInstance().registerButtonInteraction(event);

        String buttonId = event.getComponentId();
        if (buttonId.startsWith("newCrossword")) {
            CrosswordGameHandler.getInstance().handleCrosswordButton(event);
            return;
        } else if (buttonId.startsWith("dailyCrossword")) {
            CrosswordGameHandler.getInstance().handleDailyCrosswordButton(event);
            return;
        } else if (buttonId.startsWith("quitCrossword")) {
            CrosswordGameHandler.getInstance().handleCrosswordQuitButton(event);
            return;
        } else if (buttonId.startsWith("cancelCrossword")) {
            CrosswordGameHandler.getInstance().handleCrosswordCancelButton(event);
            return;
        } else if (buttonId.startsWith("hintCrossword")) {
            CrosswordGameHandler.getInstance().handleHintButton(event);
            return;
        } else if (buttonId.startsWith("shuffleCrossword")) {
            CrosswordGameHandler.getInstance().handleShuffleButton(event);
            return;
        } else if (buttonId.startsWith("extraWords")) {
            CrosswordGameHandler.getInstance().handleExtraWordButton(event);
            return;
        } else if (buttonId.startsWith("claimExtraWords")) {
            UserService.getInstance().claimExtraWordCoins(event);
            return;
        } else if (buttonId.startsWith("cancelThenNewCrossword")) {
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
