package com.ayushtech.wordwave.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

public class MetricService {

	private static MetricService instance = null;
	private Map<String, AtomicLong> commandMetricMap;
	private long startingInstance;

	private MetricService() {
		startingInstance = System.currentTimeMillis();
		commandMetricMap = new HashMap<>(37);
		loadCommandMetricMap();
	}

	public static MetricService getInstance() {
		if (instance == null) {
			instance = new MetricService();
		}
		return instance;
	}

	public void handleMetricCommand(SlashCommandInteractionEvent event) {
		event.deferReply().queue();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("WordWave Metrics");
		eb.setColor(Color.yellow);
		eb.setDescription("**Start Time : **" + TimeFormat.RELATIVE.atTimestamp(startingInstance));
		eb.addField("__Slash Commands__",
				String.format("**Crossword** : `%d`\n**Balance** : `%d`\n**Daily** : `%d`\n**Extra Words** : `%d`",
						commandMetricMap.get("slash_crossword").get(), commandMetricMap.get("slash_balance").get(),
						commandMetricMap.get("slash_daily").get(), commandMetricMap.get("slash_extra_words").get()),
				false);
		eb.addField("__Buttons__", String.format("**Crossword** : %d", commandMetricMap.get("button_crossword").get()),
				false);
		eb.addField("__Text Commands__",
				String.format("**Crossword** : `%d`", commandMetricMap.get("text_crossword").get()), false);
		event.getHook().sendMessageEmbeds(eb.build()).queue();
	}

	public void registerSlashCommands(SlashCommandInteractionEvent event) {
		CompletableFuture.runAsync(() -> {
			String commandName = event.getName();
			switch (commandName) {
			case "crossword": {
				commandMetricMap.get("slash_crossword").incrementAndGet();
				return;
			}
			case "balance": {
				commandMetricMap.get("slash_balance").incrementAndGet();
				return;
			}
			case "daily": {
				commandMetricMap.get("slash_daily").incrementAndGet();
				return;
			}
			case "extra_words": {
				commandMetricMap.get("slash_extra_words").incrementAndGet();
				return;
			}
			default: {
			}
			}
		});
	}

	public void registerButtonInteraction(ButtonInteractionEvent event) {
		CompletableFuture.runAsync(() -> {
			String buttonId = event.getComponentId();
			if (buttonId.startsWith("newCrossword")) {
				commandMetricMap.get("button_crossword").incrementAndGet();
				return;
			}
		});
	}

	public void registerTextCommand(String message) {
		CompletableFuture.runAsync(() -> {
			if (message.startsWith("!crossword")) {
				commandMetricMap.get("text_crossword").incrementAndGet();
				return;
			}
		});
	}

	private void loadCommandMetricMap() {
		commandMetricMap.put("slash_crossword", new AtomicLong());
		commandMetricMap.put("slash_daily", new AtomicLong());
		commandMetricMap.put("slash_extra_words", new AtomicLong());
		commandMetricMap.put("slash_balance", new AtomicLong());
		commandMetricMap.put("button_crossword", new AtomicLong());
		commandMetricMap.put("text_crossword", new AtomicLong());
	}
}
