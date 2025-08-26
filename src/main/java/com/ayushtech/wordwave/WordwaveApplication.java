package com.ayushtech.wordwave;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.ayushtech.wordwave.dbconnectivity.DBInfo;
import com.ayushtech.wordwave.listeners.MainListener;
import com.ayushtech.wordwave.util.ChannelService;
import com.ayushtech.wordwave.util.UtilService;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class WordwaveApplication {

    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("credential.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        final String BOT_TOKEN = properties.getProperty("BOT_TOKEN");
        final String guildEventWebhook = properties.getProperty("SERVER_EVENT_WEBHOOK");
        final String wordAdderWebhook = properties.getProperty("WORD_ADDER_WEBHOOK");
        final String wordRemoverWebhook = properties.getProperty("WORD_REMOVER_WEBHOOK");
        final String dbUrl = properties.getProperty("DB_URL");
        final String dbUsername = properties.getProperty("DB_USERNAME");
        final String dbPassword = properties.getProperty("DB_PASSWORD");

        DBInfo.setData(dbUrl, dbUsername, dbPassword);
        UtilService.getInstance().setGuildEventWebhookUrl(guildEventWebhook);
        UtilService.getInstance().setWordAdderWebhookUrl(wordAdderWebhook);
        UtilService.getInstance().setWordRemovedWebhookUrl(wordRemoverWebhook);
        ChannelService.getInstance().loadDisabledChannels();

        DefaultShardManagerBuilder builder =
        DefaultShardManagerBuilder.createDefault(BOT_TOKEN,
        GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.DIRECT_MESSAGES);
        builder.setActivity(Activity.playing("/crossword"));
        builder.disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI,
        CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS);
        builder.addEventListeners(new MainListener());
        builder.build();
        
    }

}
