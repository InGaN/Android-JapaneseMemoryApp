package com.myKanji;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kevin on 2016/02/14.
 */
public class Kanji implements Parcelable {
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

    public Kanji(Parcel in) {
        this.id = in.readInt();
        this.kanji = in.readString();
        this.furigana = in.readString();
        this.meaning = in.readString();
        this.difficulty = in.readByte();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(kanji);
        dest.writeString(furigana);
        dest.writeString(meaning);
        dest.writeByte(difficulty);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Kanji createFromParcel(Parcel in) {
            return new Kanji(in);
        }
        public Kanji[] newArray(int size) {
            return new Kanji[size];
        }
    };

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
    public void changeDifficulty(int modifier) {
        if (difficulty + modifier < 0)
            difficulty = 0;
        else if (difficulty + modifier > 9)
            difficulty = 9;
        else
            difficulty += modifier;
    }
}
