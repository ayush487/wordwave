package com.ayushtech.wordwave.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ayushtech.wordwave.dbconnectivity.ChannelsDao;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class ChannelService {

	private Set<Long> disabledChannels;
	private static ChannelService instance = null;

	private ChannelService() {
		disabledChannels = new HashSet<>();
	}

	public static ChannelService getInstance() {
		if (instance == null) {
			instance = new ChannelService();
		}
		return instance;
	}

	public void loadDisabledChannels() {
		disabledChannels = ChannelsDao.getInstance().getAllDisabledChannels();
	}

	public void handleEnableCommand(SlashCommandInteractionEvent event) {
		event.deferReply().setEphemeral(true).queue();
		Member member = event.getMember();
		if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
			OptionMapping option = event.getOption("channel");
			if (option == null) {
				if (!disabledChannels.contains(event.getChannel().getIdLong())) {
					event.getHook().sendMessage("Commands are already enabled for this channel!").queue();
					return;
				}
				if (enableChannel(event.getChannel().getIdLong())) {
					event.getHook().sendMessage("Commands are enabled for this channel now!").setEphemeral(true)
							.queue();
				} else {
					event.getHook().sendMessage("Something went wrong!\nPlease try again").queue();
				}
			} else {
				GuildChannelUnion channelOption = option.getAsChannel();
				if (channelOption == null) {
					event.getHook().sendMessage("Mentioned channel is not a Message Channel").queue();
				} else {
					if (!disabledChannels.contains(channelOption.getIdLong())) {
						event.getHook().sendMessage("Commands are already enabled for this channel!").queue();
						return;
					}

					if (enableChannel(channelOption.getIdLong())) {
						event.getHook()
								.sendMessage("Commands are enabled for " + channelOption.getAsMention() + " now!")
								.setEphemeral(true).queue();

					} else {
						event.getHook().sendMessage("Something went wrong!\nPlease try again").queue();

					}
				}
			}
		} else {
			event.getHook().sendMessage("You need `Manage_Channel` permissions to use this command!").setEphemeral(true)
					.queue();
		}
	}

	public void handleDisableCommand(SlashCommandInteractionEvent event) {
		event.deferReply().setEphemeral(true).queue();
		Member member = event.getMember();
		if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
			OptionMapping option = event.getOption("channel");
			if (option == null) {
				if (disabledChannels.contains(event.getChannel().getIdLong())) {
					event.getHook().sendMessage("Command are already disabled for this channel").queue();
					return;
				}
				if (disableChannel(event.getChannel().getIdLong())) {
					event.getHook().sendMessage("Commands are disabled for this channel now!").setEphemeral(true)
							.queue();

				} else {
					event.getHook().sendMessage("Something went wrong!\nPlease try again").queue();
				}
			} else {
				GuildChannelUnion channelOption = option.getAsChannel();
				if (channelOption == null) {
					event.getHook().sendMessage("Mentioned channel is not a Message Channel").setEphemeral(true)
							.queue();
				} else {
					if (disabledChannels.contains(channelOption.getIdLong())) {
						event.getHook().sendMessage("Command are already disabled for this channel").queue();
						return;
					}
					if (disableChannel(channelOption.getIdLong())) {
						event.getHook()
								.sendMessage("Commands are disabled for " + channelOption.getAsMention() + " now!")
								.setEphemeral(true).queue();
					} else {
						event.getHook().sendMessage("Something went wrong!\nPlease try again").queue();
					}
				}
			}
		} else {
			event.getHook().sendMessage("You need `Manage_Channel` permissions to use this command!").setEphemeral(true)
					.queue();
		}
	}
	
	public void handleDisableAllCommand(SlashCommandInteractionEvent event) {
	    event.deferReply().setEphemeral(true).queue();
	    Member member = event.getMember();
	    if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
	      disableMultipleChannels(event.getGuild());
	      event.getHook().sendMessage("Commands are disabled in all channels.").setEphemeral(true).queue();
	    } else {
	      event.getHook().sendMessage("You need `Manage_Channel` permissions to use this command!").setEphemeral(true)
	          .queue();
	    }
	  }

	public boolean isChannelDisabled(long channelId) {
		return disabledChannels.contains(channelId);
	}

	public boolean disableChannel(long channelId) {
		boolean isChannelDisabled = disabledChannels.add(channelId);
		if (isChannelDisabled) {
			ChannelsDao.getInstance().addDisableChannel(channelId);
			return true;
		}
		return false;
	}

	public boolean enableChannel(long channelId) {
		if (disabledChannels.contains(channelId)) {
			boolean isChannelEnabled = ChannelsDao.getInstance().enableChannel(channelId);
			if (isChannelEnabled) {
				disabledChannels.remove(channelId);
				return true;
			}

		}
		return false;
	}

	public void disableMultipleChannels(Guild guild) {
		List<Long> channelIdList = guild.getChannels().stream().map(channel -> channel.getIdLong())
				.collect(Collectors.toList());
		ChannelsDao.getInstance().addDisableChannel(channelIdList);
		channelIdList.forEach(channelId -> disabledChannels.add(channelId));
	}

}
