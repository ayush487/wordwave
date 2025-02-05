package com.ayushtech.wordwave.util;

import java.awt.Color;

import com.ayushtech.wordwave.dbconnectivity.LevelsDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class UserService {

	private static UserService instance = null;

	private UserService() {
	}

	public static UserService getInstance() {
		if (instance == null) {
			instance = new UserService();
		}
		return instance;
	}

	public void handleBalanceCommand(SlashCommandInteractionEvent event) {
		event.deferReply().queue();
		var user = event.getUser();
		int coins = LevelsDao.getInstance().getUserBalance(user.getIdLong());
		EmbedBuilder eb = new EmbedBuilder();
		eb.setThumbnail(user.getAvatarUrl());
		eb.setTitle(user.getName());
		eb.setColor(Color.yellow);
		eb.setDescription(String.format("**Balance** : %d :coin:", coins));
		event.getHook().sendMessageEmbeds(eb.build()).queue();
	}

	public void claimExtraWordCoins(ButtonInteractionEvent event) {
		event.editButton(Button.success("claimed", "Claimed").asDisabled()).queue();
		long userId = event.getUser().getIdLong();
		LevelsDao.getInstance().claimCoinsWithExtraWords(userId);
		event.getHook().sendMessage("100 :coin: added to your balance").setEphemeral(true).queue();
	}
}
