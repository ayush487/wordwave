package com.ayushtech.wordwave.util;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class MetricService {

	private static MetricService instance = null;
	private MetricService() {}
	
	public static MetricService getInstance() {
		if (instance==null) {
			instance = new MetricService();
		}
		return instance;
	}
	
	public void registerSlashCommands(SlashCommandInteractionEvent event) {
		
	}
	
	public void registerButtonInteraction(ButtonInteractionEvent event) {
		
	}
	
	public void registerTextCommand(String message) {
		
	}
}
