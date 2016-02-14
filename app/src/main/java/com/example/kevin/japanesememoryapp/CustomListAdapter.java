package com.example.kevin.japanesememoryapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kevin on 2016/02/14.
 */
public class CustomListAdapter extends ArrayAdapter<Kanji> {
    private Activity activity;
    private ArrayList<Kanji> kanji;

    public CustomListAdapter(Activity activity, ArrayList<Kanji> kanji) {
        super(activity, R.layout.kanji_list_item, kanji);
        this.activity = activity;
        this.kanji = kanji;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.kanji_list_item, null, true);

        TextView lbl_id = (TextView) rowView.findViewById(R.id.lbl_item_id);
        TextView lbl_kanji = (TextView) rowView.findViewById(R.id.lbl_item_kanji);
        TextView lbl_furigana = (TextView) rowView.findViewById(R.id.lbl_item_furigana);
        TextView lbl_meaning = (TextView) rowView.findViewById(R.id.lbl_item_meaning);

        lbl_id.setText("ID: " + kanji.get(position).getKanjiID());
        lbl_kanji.setText(kanji.get(position).getKanji());
        lbl_furigana.setText(kanji.get(position).getFurigana());
        lbl_meaning.setText(kanji.get(position).getMeaning());

        return rowView;
    }
}
