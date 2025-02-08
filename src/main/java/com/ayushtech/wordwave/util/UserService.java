package com.ayushtech.wordwave.util;

import java.awt.Color;
import java.util.Calendar;
import java.util.Optional;

import com.ayushtech.wordwave.dbconnectivity.UserDao;

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
		int coins = UserDao.getInstance().getUserBalance(user.getIdLong());
		EmbedBuilder eb = new EmbedBuilder();
		eb.setThumbnail(user.getAvatarUrl());
		eb.setTitle(user.getName());
		eb.setColor(Color.yellow);
		eb.setDescription(String.format("**Balance** : %d :coin:", coins));
		event.getHook().sendMessageEmbeds(eb.build()).queue();
	}

	public void handleDailyCommand(SlashCommandInteractionEvent event) {
		event.deferReply().queue();
		var user = event.getUser();
		Optional<String> optLastDailyDate = UserDao.getInstance().getUserLastDailyDate(user.getIdLong());
		if (optLastDailyDate.isEmpty() || isThisNotToday(optLastDailyDate.get())) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.green);
			eb.setTitle("Daily Rewards");
			eb.setDescription("100 :coin: is added to your account.");
			eb.setFooter(user.getName(), user.getAvatarUrl());
			event.getHook().sendMessageEmbeds(eb.build()).queue();
			UserDao.getInstance().addDailyRewards(user.getIdLong());
		} else {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.gray);
			eb.setTitle("Daily Rewards");
			eb.setDescription("You already have claimed daily rewards");
			eb.setFooter(user.getName(), user.getAvatarUrl());
			event.getHook().sendMessageEmbeds(eb.build()).queue();
		}
	}

	private boolean isThisNotToday(String lastDailyDate) {
		String currentDate = String.format("%d-%d-%d", Calendar.getInstance().get(Calendar.DATE),
				Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR));
		System.out.println(currentDate);
		return !currentDate.equals(lastDailyDate);
	}

	public void claimExtraWordCoins(ButtonInteractionEvent event) {
		event.editButton(Button.success("claimed", "Claimed").asDisabled()).queue();
		long userId = event.getUser().getIdLong();
		UserDao.getInstance().claimCoinsWithExtraWords(userId);
		event.getHook().sendMessage("100 :coin: added to your balance").setEphemeral(true).queue();
	}
}
