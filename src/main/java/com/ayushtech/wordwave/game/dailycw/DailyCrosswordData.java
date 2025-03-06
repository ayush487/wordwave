package com.ayushtech.wordwave.game.dailycw;

public record DailyCrosswordData(long userId, String date, String unsolvedGrid, String enterredWords, String extraWords, boolean usedHint, int sun) {
  
}
