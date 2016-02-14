package com.example.kevin.japanesememoryapp;

/**
 * Created by kevin on 2016/02/14.
 */
public class Kanji {
    private int id;
    private String kanji;
    private String furigana;
    private String meaning;
    private byte difficulty;

    public Kanji(int id, String kanji, String furigana, String meaning, byte difficulty) {
        this.id = id;
        this.kanji = kanji;
        this.furigana = furigana;
        this.meaning = meaning;
        this.difficulty = difficulty;
    }
    public int getKanjiID() {
        return this.id;
    }

    public String getKanji() {
        return this.kanji;
    }
    public String getFurigana() {
        return this.furigana;
    }
    public String getMeaning() {
        return this.meaning;
    }
    public byte getDifficulty() {
        return this.difficulty;
    }
    public void incrementDifficulty() {
        this.difficulty++;
    }
    public void decrementDifficulty() {
        this.difficulty--;
    }
}
