package com.ayushtech.wordwave.game;

public record CorrectWordResponse(boolean isCorrect,String word, boolean isAcross, int x, int y, boolean levelCompleted) {
}
