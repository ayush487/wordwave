package com.ayushtech.wordwave;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.ayushtech.wordwave.listeners.MainListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class WordwaveApplication {

	public static void main(String[] args) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("credential.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		final String BOT_TOKEN = properties.getProperty("BOT_TOKEN");

//		DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(BOT_TOKEN,
//				GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES);
//		builder.setActivity(Activity.playing("Waking Up!"));
//		builder.disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS);
//		builder.addEventListeners(new MainListener());
//		builder.build();

		JDA jda = JDABuilder
				.create(BOT_TOKEN, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES,
						GatewayIntent.DIRECT_MESSAGES)
				.addEventListeners(new MainListener()).setActivity(Activity.watching("my development")).build();

		jda.upsertCommand("enable", "Enable the bot commands in the following channel")
				.addOption(OptionType.CHANNEL, "channel", "Select channel").queue();

		jda.upsertCommand("disable", "Disable the bot commands in the following channel")
				.addOption(OptionType.CHANNEL, "channel", "Select channel").queue();

		jda.upsertCommand("disable_all_channels", "Disable the bot commands in all channels of server").queue();
//		jda.upsertCommand("crossword", "Start a crossword game in the current channel").queue();
	}

}
