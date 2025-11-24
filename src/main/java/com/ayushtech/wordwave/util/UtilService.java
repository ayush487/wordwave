package com.ayushtech.wordwave.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.ayushtech.wordwave.dbconnectivity.UserDao;
import com.ayushtech.wordwave.game.CrosswordGameHandler;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UtilService {

	private static UtilService instance = null;
	private Map<Character, String> emojiMap;
	private Map<Character, String> defaultEmojiMap;
	private final String bar1empty = Emoji.fromCustom("bar1empty", 1195296826132287541l, false).getAsMention();
	private final String bar1half = Emoji.fromCustom("bar1half", 1195297050993115246l, true).getAsMention();
	private final String bar1full = Emoji.fromCustom("bar1full", 1195297246464442368l, true).getAsMention();
	private final String bar1max = Emoji.fromCustom("bar1max", 1195297353591173201l, true).getAsMention();
	private final String bar2empty = Emoji.fromCustom("bar2empty", 1195297658567413790l, false).getAsMention();
	private final String bar2half = Emoji.fromCustom("bar2half", 1195297926734426162l, true).getAsMention();
	private final String bar2full = Emoji.fromCustom("bar2full", 1195298061522587659l, true).getAsMention();
	private final String bar2max = Emoji.fromCustom("bar2max", 1195298660800528434l, true).getAsMention();
	private final String bar3empty = Emoji.fromCustom("bar3empty", 1195298974429618207l, false).getAsMention();
	private final String bar3half = Emoji.fromCustom("bar3half", 1195299147499192362l, true).getAsMention();
	private final String bar3full = Emoji.fromCustom("bar3full", 1195299364759941131l, true).getAsMention();
	private String guildEventWebhookUrl = "";
	private String wordAdderWebhookUrl = "";
	private String wordRemovedWebhookUrl = "";

	private UtilService() {
		this.emojiMap = new HashMap<Character, String>();
		this.defaultEmojiMap = new HashMap<Character, String>();
		setEmojis();
	}

	public static UtilService getInstance() {
		if (instance == null) {
			instance = new UtilService();
		}
		return instance;
	}

	public void handleAddWordCommand(SlashCommandInteractionEvent event) {
		event.deferReply().queue();
		String word = event.getOption("word").getAsString();
		try {
			boolean isAdded = UserDao.getInstance().addWord(word);
			if (isAdded) {
				CrosswordGameHandler.getInstance().addWordIntoWordSet(word);
				event.getHook().sendMessage(String.format("**%s** added into database", word)).queue();
				String webhookMessage = String.format("Word Added : **%s**     By : <@%s>", word,
						event.getUser().getId());
				sendMessageToWebhook(wordAdderWebhookUrl, webhookMessage);
			} else {
				event.getHook().sendMessage(String.format("**%s** already exist in database!", word)).queue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			event.getHook().sendMessage("Something went wrong!").queue();
			return;
		}
	}

	public void handleRemoveWordCommand(SlashCommandInteractionEvent event) {
		event.deferReply().queue();
		String word = event.getOption("word").getAsString();
		try {
			boolean isRemoved = UserDao.getInstance().removeWord(word);
			if (isRemoved) {
				CrosswordGameHandler.getInstance().removeWordFromWordSet(word);
				event.getHook().sendMessage(String.format("**%s** removed from database", word)).queue();
				String webhookMessage = String.format("Word Removed : **%s**    By : <@%s>", word,
						event.getUser().getId());
				sendMessageToWebhook(wordRemovedWebhookUrl, webhookMessage);
			} else {
				event.getHook().sendMessage(String.format("**%s** is not in database", word)).queue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			event.getHook().sendMessage("Something went wrong!").queue();
			return;
		}
	}

	public void notifyGuildJoin(GuildJoinEvent event) {
		String guildName = event.getGuild().getName();
		String msg = String.format("Wordwave:capital_abcd: joined server - %s", guildName);
		sendMessageToWebhook(guildEventWebhookUrl, msg);
	}

	public void notifyGuildLeave(GuildLeaveEvent event) {
		String guildName = event.getGuild().getName();
		String msg = String.format("Wordwave:capital_abcd: leaved server - %s", guildName);
		sendMessageToWebhook(guildEventWebhookUrl, msg);
	}

	public void sendMessageToWebhook(String url, String message) {
		OkHttpClient client = new OkHttpClient();
		String jsonInputString = String.format("{\"content\" : \"%s\"}", message);
		RequestBody body = RequestBody.create(jsonInputString, MediaType.parse("application/json; charset=utf-8"));
		Request request = new Request.Builder().url(url).post(body).build();
		try {
			client.newCall(request).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getEmoji(char c) {
		return this.emojiMap.get(c);
	}

	public String getDate() {
		return String.format("%d-%d-%d", Calendar.getInstance().get(Calendar.DATE),
				Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.YEAR));
	}

	public String getProgressBar(int fill) {
		int progressBarFillAmount = Math.round(fill / 10.0f) * 10;
		String progressBar;
		switch (progressBarFillAmount) {
			case 0:
				progressBar = bar1empty + bar2empty + bar2empty + bar2empty + bar3empty;
				break;
			case 10:
				progressBar = bar1half + bar2empty + bar2empty + bar2empty + bar3empty;
				break;
			case 20:
				progressBar = bar1full + bar2empty + bar2empty + bar2empty + bar3empty;
				break;
			case 30:
				progressBar = bar1max + bar2half + bar2empty + bar2empty + bar3empty;
				break;
			case 40:
				progressBar = bar1max + bar2full + bar2empty + bar2empty + bar3empty;
				break;
			case 50:
				progressBar = bar1max + bar2max + bar2half + bar2empty + bar3empty;
				break;
			case 60:
				progressBar = bar1max + bar2max + bar2full + bar2empty + bar3empty;
				break;
			case 70:
				progressBar = bar1max + bar2max + bar2max + bar2half + bar3empty;
				break;
			case 80:
				progressBar = bar1max + bar2max + bar2max + bar2full + bar3empty;
				break;
			case 90:
				progressBar = bar1max + bar2max + bar2max + bar2max + bar3half;
				break;
			case 100:
				progressBar = bar1max + bar2max + bar2max + bar2max + bar3full;
				break;
			default:
				progressBar = bar1empty + bar2empty + bar2empty + bar2empty + bar3empty;
				break;
		}
		return progressBar;
	}

	private void setEmojis() {
		this.emojiMap.put('a', ":regional_indicator_a:");
		this.emojiMap.put('b', ":regional_indicator_b:");
		this.emojiMap.put('c', ":regional_indicator_c:");
		this.emojiMap.put('d', ":regional_indicator_d:");
		this.emojiMap.put('e', ":regional_indicator_e:");
		this.emojiMap.put('f', ":regional_indicator_f:");
		this.emojiMap.put('g', ":regional_indicator_g:");
		this.emojiMap.put('h', ":regional_indicator_h:");
		this.emojiMap.put('i', ":regional_indicator_i:");
		this.emojiMap.put('j', ":regional_indicator_j:");
		this.emojiMap.put('k', ":regional_indicator_k:");
		this.emojiMap.put('l', ":regional_indicator_l:");
		this.emojiMap.put('m', ":regional_indicator_m:");
		this.emojiMap.put('n', ":regional_indicator_n:");
		this.emojiMap.put('o', ":regional_indicator_o:");
		this.emojiMap.put('p', ":regional_indicator_p:");
		this.emojiMap.put('q', ":regional_indicator_q:");
		this.emojiMap.put('r', ":regional_indicator_r:");
		this.emojiMap.put('s', ":regional_indicator_s:");
		this.emojiMap.put('t', ":regional_indicator_t:");
		this.emojiMap.put('u', ":regional_indicator_u:");
		this.emojiMap.put('v', ":regional_indicator_v:");
		this.emojiMap.put('w', ":regional_indicator_w:");
		this.emojiMap.put('x', ":regional_indicator_x:");
		this.emojiMap.put('y', ":regional_indicator_y:");
		this.emojiMap.put('z', ":regional_indicator_z:");
		this.emojiMap.put('-', "‎:black_large_square:");
		this.emojiMap.put('+', ":white_medium_square:");
		this.emojiMap.put('A', "<:a_:1333904071349899334>");
		this.emojiMap.put('B', "<:b_:1333904093084913694>");
		this.emojiMap.put('C', "<:c_:1333904107928420423>");
		this.emojiMap.put('D', "<:d_:1333904124189736970>");
		this.emojiMap.put('E', "<:e_:1333904138202910720>");
		this.emojiMap.put('F', "<:f_:1333904155525386355>");
		this.emojiMap.put('G', "<:g_:1333904174722584597>");
		this.emojiMap.put('H', "<:h_:1333904189700706465>");
		this.emojiMap.put('I', "<:i_:1333904207572504688>");
		this.emojiMap.put('J', "<:j_:1333904225511407688>");
		this.emojiMap.put('K', "<:k_:1333904248173498479>");
		this.emojiMap.put('L', "<:l_:1333904265541976095>");
		this.emojiMap.put('M', "<:m_:1333904287360745482>");
		this.emojiMap.put('N', "<:n_:1333904306327257119>");
		this.emojiMap.put('O', "<:o_:1333904324375347332>");
		this.emojiMap.put('P', "<:p_:1333904339676303360>");
		this.emojiMap.put('Q', "<:q_:1333904362447306772>");
		this.emojiMap.put('R', "<:r_:1333904381715677348>");
		this.emojiMap.put('S', "<:s_:1333904399801782312>");
		this.emojiMap.put('T', "<:t_:1333904413235875872>");
		this.emojiMap.put('U', "<:u_:1333904441861996554>");
		this.emojiMap.put('V', "<:v_:1333904459503501342>");
		this.emojiMap.put('W', "<:w_:1333904475722878986>");
		this.emojiMap.put('X', "<:x_:1333904491912892506>");
		this.emojiMap.put('Y', "<:y_:1333904505691177044>");
		this.emojiMap.put('Z', "<:z_:1333904517216997398>");
		this.defaultEmojiMap.put('a', "\uD83C\uDDE6");
		this.defaultEmojiMap.put('b', "\uD83C\uDDE7");
		this.defaultEmojiMap.put('c', "\uD83C\uDDE8");
		this.defaultEmojiMap.put('d', "\uD83C\uDDE9");
		this.defaultEmojiMap.put('e', "\uD83C\uDDEA");
		this.defaultEmojiMap.put('f', "\uD83C\uDDEB");
		this.defaultEmojiMap.put('g', "\uD83C\uDDEC");
		this.defaultEmojiMap.put('h', "\uD83C\uDDED");
		this.defaultEmojiMap.put('i', "\uD83C\uDDEE");
		this.defaultEmojiMap.put('j', "\uD83C\uDDEF");
		this.defaultEmojiMap.put('k', "\uD83C\uDDF0");
		this.defaultEmojiMap.put('l', "\uD83C\uDDF1");
		this.defaultEmojiMap.put('m', "\uD83C\uDDF2");
		this.defaultEmojiMap.put('n', "\uD83C\uDDF3");
		this.defaultEmojiMap.put('o', "\uD83C\uDDF4");
		this.defaultEmojiMap.put('p', "\uD83C\uDDF5");
		this.defaultEmojiMap.put('q', "\uD83C\uDDF6");
		this.defaultEmojiMap.put('r', "\uD83C\uDDF7");
		this.defaultEmojiMap.put('s', "\uD83C\uDDF8");
		this.defaultEmojiMap.put('t', "\uD83C\uDDF9");
		this.defaultEmojiMap.put('u', "\uD83C\uDDFA");
		this.defaultEmojiMap.put('v', "\uD83C\uDDFB");
		this.defaultEmojiMap.put('w', "\uD83C\uDDFC");
		this.defaultEmojiMap.put('x', "\uD83C\uDDFD");
		this.defaultEmojiMap.put('y', "\uD83C\uDDFE");
		this.defaultEmojiMap.put('z', "\uD83C\uDDFF");
	}

	public void setGuildEventWebhookUrl(String url) {
		this.guildEventWebhookUrl = url;
	}

	public void setWordAdderWebhookUrl(String url) {
		this.wordAdderWebhookUrl = url;
	}

	public void setWordRemovedWebhookUrl(String url) {
		this.wordRemovedWebhookUrl = url;
	}

	public String getDefaultEmoji(char c) {
		return this.defaultEmojiMap.get(c);
	}

}
