package com.ayushtech.wordwave.util;

import java.awt.Color;
import java.time.LocalDateTime;

import com.ayushtech.wordwave.dbconnectivity.UserDao;
import com.ayushtech.wordwave.dbconnectivity.VoterDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class VotingService {
  private static VotingService votingService = null;

  private static String VOTER_WEBHOOK_URL;

  private VotingService() {

  }

  public static synchronized VotingService getInstance() {
    if (votingService == null) {
      votingService = new VotingService();
    }
    return votingService;
  }

  public void voteUser(MessageReceivedEvent event) {
    var voterUser = event.getMessage().getMentions().getMembers().get(0);
    String voter_id = voterUser.getId();
    String dmMsg;
    boolean isWeekend = isWeekend();
    if (isWeekend) {
      dmMsg = ":heart: **|** Thanks for voting us on [top.gg](<https://top.gg/bot/1331327187881562173/vote>)\n:money_with_wings: **|** You received 200 :coin: as reward!\n:fireworks: **|** Since it's a weekend, you receive 200 :coin: extra!";
      UserDao.getInstance().addCoins(Long.parseLong(voter_id), 400);
    } else {
      UserDao.getInstance().addCoins(Long.parseLong(voter_id), 200);
      dmMsg = ":heart: **|** Thanks for voting us on [top.gg](<https://top.gg/bot/1331327187881562173/vote>)\n:money_with_wings: **|** You received 200 :coin: as reward!";
    }
    event.getJDA().retrieveUserById(voter_id).queue(voter -> {
      voter.openPrivateChannel()
          .flatMap(channel -> channel.sendMessage(dmMsg))
          .queue();
      UtilService.getInstance().sendMessageToWebhook(VOTER_WEBHOOK_URL,
          "Rewards sent to User `" + voter.getName() + "`, id : `" +
              voter_id + "`");
    });
    VoterDao.getInstance().addVoter(Long.parseLong(voter_id));
  }

  public void handleVoteCommand(SlashCommandInteractionEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Vote for WordWave");
    eb.setThumbnail("https://cdn.discordapp.com/avatars/1331327187881562173/03e2e756928139570753af66e97651bd.png");
    eb.setDescription("Vote for WordWave on top.gg\n[here](https://top.gg/bot/1331327187881562173/vote)");
    eb.addField("Rewards", "> Each vote gets you 200 :coin:\n> You will get double rewards during weekends", false);
    eb.setFooter("You can vote every 12 hours");
    eb.setColor(Color.GREEN);
    event.replyEmbeds(eb.build())
        .addActionRow(Button.link("https://top.gg/bot/1331327187881562173/vote", "Top.gg"))
        .queue();
  }

  private boolean isWeekend() {
    String day = LocalDateTime.now().minusMinutes(330l).getDayOfWeek().name();
    return (day.equals("SATURDAY") || day.equals("SUNDAY"));
  }

  public static void setVotingWebhookUrl(String webhookUrl) {
    VOTER_WEBHOOK_URL = webhookUrl;
  }

}
