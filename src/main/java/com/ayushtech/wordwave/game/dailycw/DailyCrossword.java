package com.ayushtech.wordwave.game.dailycw;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.ayushtech.wordwave.dbconnectivity.LevelsDao;
import com.ayushtech.wordwave.dbconnectivity.UserDao;
import com.ayushtech.wordwave.game.CorrectWordResponse;
import com.ayushtech.wordwave.game.CrosswordGame;
import com.ayushtech.wordwave.game.CrosswordPointer;
import com.ayushtech.wordwave.game.Level;
import com.ayushtech.wordwave.util.UtilService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class DailyCrossword extends CrosswordGame {

    private CrosswordPointer sunPosition;
    private int sunCollected = 0;
    private String date;

    public DailyCrossword(long userId, Level level, MessageChannel channel, String date) {
        super(userId, level, channel, false);
        this.date = date;
        Optional<DailyCrosswordData> dailyOptional = LevelsDao.getInstance().getDailyData(userId,
                date);
        if (dailyOptional.isPresent()) {
            DailyCrosswordData levelData = dailyOptional.get();
            currentLevel.removeEnterredWords(levelData.enterredWords().split(","));
            currentLevel.setUnsolvedGrid(levelData.unsolvedGrid());
            String[] tempEnterredWords = levelData.enterredWords().split(",");
            String[] tempExtraWords = levelData.extraWords().split(",");
            for (String e : tempEnterredWords) {
                this.enterredWords.add(e);
            }
            for (String e : tempExtraWords) {
                this.extraWords.add(e);
            }
            this.usedHint = levelData.usedHint();
            this.sunCollected = levelData.sun();
        }
        this.sendGameEmbed();
    }

    @Override
    public void updateGame(CorrectWordResponse response) {
        this.currentLevel.updateUnsolvedGrid(response);
        if (response.levelCompleted()) {
            sunPosition = new CrosswordPointer(-1, -1);
            completeThisLevel();
            this.channel
                    .sendMessage(
                            "You completed Today's Crossword! :tada:\n:sun_with_face: collected `" + sunCollected + "`")
                    .addActionRow(Button.primary("newCrossword_" + userId, "Play Next Level")).queue();
        } else {
            if (checkIfSunObtained(response)) {
                sunCollected++;
                sunPosition = getNewSunPositon();
            }
            updateEmbed();
            checkIfWordCompleted();
        }
    }

    @Override
    public void quitGame(ButtonInteractionEvent event) {
        super.quitGame(event);
        CompletableFuture.runAsync(() -> storeGameDataIntoDatabase());
    }

    @Override
    public void cancelGame() {
        super.cancelGame();
        CompletableFuture.runAsync(() -> storeGameDataIntoDatabase());
    }

    private void storeGameDataIntoDatabase() {
        String enterredWords = this.enterredWords.stream().collect(Collectors.joining(","));
        String extraWords = this.extraWords.stream().collect(Collectors.joining(","));
        char[][] unsolvedGrid = this.currentLevel.getGridUnsolved();
        StringBuilder sb = new StringBuilder();
        for (char[] across : unsolvedGrid) {
            for (char e : across) {
                sb.append(e);
            }
            sb.append(":");
        }
        String levelData = sb.toString();
        DailyCrosswordData d = new DailyCrosswordData(userId, this.date, levelData, enterredWords, extraWords, usedHint,
                sunCollected);
        LevelsDao.getInstance().saveDailyLevelData(d);
    }

    private boolean checkIfSunObtained(CorrectWordResponse response) {
        if (response.isAcross()) {
            if (response.y() == sunPosition.i()) {
                if (sunPosition.j() >= response.x() && sunPosition.j() <= response.x() + response.word().length())
                    return true;
            }
        } else {
            if (response.x() == sunPosition.j()) {
                if (sunPosition.i() >= response.y() && sunPosition.i() <= response.y() + response.word().length())
                    return true;
            }
        }
        return false;
    }

    @Override
    protected void completeThisLevel() {
        var embed = getEmbed((byte) 0, "Daily Crossword");
        this.channel.editMessageEmbedsById(messageId, embed)
                .setActionRow(Button.success("complete", "Daily Crossword Completed").asDisabled()).queue();
        UserDao.getInstance().updateUserLastDailyDate(userId);
        // TODO : Database Update about Sun Collections
    }

    @Override
    protected MessageEmbed getBeginningEmbed(Level level) {
        this.sunPosition = getNewSunPositon();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Daily Crossword");
        eb.setDescription(getGridFormated());
        eb.setColor(Color.orange);
        StringBuilder sb = new StringBuilder(level.getAllowedLetters());
        sb.append(String.format("\nMinimum Word Size : `%d`", level.getMinWordSize()));
        sb.append(String.format("\nMaximum Word Size : `%d`", level.getMaxWordSize()));
        sb.append(String.format("\n\n:sun_with_face: : `%d`", sunCollected));
        eb.addField("__Allowed Letters__", sb.toString(), false);
        return eb.build();
    }

    @Override
    protected MessageEmbed getEmbed(byte status, String footerText) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Daily Crossword");
        eb.setDescription(getGridFormated());
        Color color = status == 0 ? Color.green : status == 1 ? Color.red : Color.orange;
        eb.setColor(color);
        StringBuilder sb = new StringBuilder(currentLevel.getAllowedLetters());
        sb.append(String.format("\nMinimum Word Size : `%d`", currentLevel.getMinWordSize()));
        sb.append(String.format("\nMaximum Word Size : `%d`", currentLevel.getMaxWordSize()));
        sb.append(String.format("\n\n:sun_with_face: : `%d`", sunCollected));
        eb.addField("__Allowed Letters__", sb.toString(), false);
        if (footerText != null)
            eb.setFooter(footerText);
        return eb.build();
    }

    @Override
    protected String getGridFormated() {
        char[][] gridUnsolved = currentLevel.getGridUnsolved();
        StringBuilder gridFormatted = new StringBuilder();
        int height = gridUnsolved.length;
        int width = gridUnsolved[0].length;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i == sunPosition.i() && j == sunPosition.j()) {
                    gridFormatted.append("<:blank_sun:1345284481245773834>");
                } else {
                    gridFormatted.append(UtilService.getInstance().getEmoji(gridUnsolved[i][j]));
                }
            }
            gridFormatted.append("\n");
        }
        return gridFormatted.toString();
    }

    @Override
    public boolean activateHint() {
        List<CrosswordPointer> emptyPositionList = new ArrayList<CrosswordPointer>();
        for (int i = 0; i < currentLevel.getColumns(); i++) {
            for (int j = 0; j < currentLevel.getRows(); j++) {
                if (currentLevel.getGridUnsolved()[i][j] == '+') {
                    emptyPositionList.add(new CrosswordPointer(i, j));
                }
            }
        }
        if (emptyPositionList.size() == 0) {
            return false;
        } else {
            int hintPosition = random.nextInt(emptyPositionList.size());
            var pointer = emptyPositionList.get(hintPosition);
            if (pointer.i() == sunPosition.i() && pointer.j() == sunPosition.j()) {
                sunCollected++;
                if (emptyPositionList.size() > 1) {
                    emptyPositionList.remove(hintPosition);
                    int newSunPositionIndex = random.nextInt(emptyPositionList.size());
                    this.sunPosition = emptyPositionList.get(newSunPositionIndex);
                } else {
                    this.sunPosition = new CrosswordPointer(-1, -1);
                }
            }
            currentLevel.unlockLetter(pointer.i(), pointer.j());
            updateEmbed();
            usedHint = true;
            checkIfWordCompleted();
            return true;
        }
    }

    private CrosswordPointer getNewSunPositon() {
        List<CrosswordPointer> emptyPositionList = new ArrayList<CrosswordPointer>();
        for (int i = 0; i < currentLevel.getColumns(); i++) {
            for (int j = 0; j < currentLevel.getRows(); j++) {
                if (currentLevel.getGridUnsolved()[i][j] == '+') {
                    emptyPositionList.add(new CrosswordPointer(i, j));
                }
            }
        }
        var pointer = emptyPositionList.get(random.nextInt(emptyPositionList.size()));
        return new CrosswordPointer(pointer.i(), pointer.j());
    }
}
