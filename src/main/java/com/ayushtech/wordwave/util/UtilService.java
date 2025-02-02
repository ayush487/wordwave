package com.ayushtech.wordwave.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ayushtech.wordwave.dbconnectivity.LevelsDao;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UtilService {

	private static UtilService instance = null;
	private Map<Character, String> emojiMap;
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
	
	private UtilService() {
		this.emojiMap = new HashMap<Character, String>();
		setEmojis();
	}

	public static UtilService getInstance() {
		if (instance == null) {
			instance = new UtilService();
		}
		return instance;
	}

	public void claimExtraWordCoins(ButtonInteractionEvent event) {
		event.editButton(Button.success("claimed", "Claimed").asDisabled()).queue();
		long userId = event.getUser().getIdLong();
		LevelsDao.getInstance().claimCoinsWithExtraWords(userId);
		event.getHook().sendMessage("25 :coin: added to your balance").setEphemeral(true).queue();
	}

	public String getEmoji(char c) {
		return this.emojiMap.get(c);
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

	public void setGuildEventWebhookUrl(String url) {
		this.guildEventWebhookUrl = url;
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

	private void sendMessageToWebhook(String url, String message) {
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
		this.emojiMap.put('-', "<:blank:1223533175960109106>");
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
	}

}
