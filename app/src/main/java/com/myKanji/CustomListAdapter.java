package com.myKanji;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.myKanji.R;

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
        TextView lbl_difficulty = (TextView) rowView.findViewById(R.id.lbl_item_difficulty);

        lbl_id.setText("" + kanji.get(position).getKanjiID());
        lbl_kanji.setText(kanji.get(position).getKanji());
        lbl_furigana.setText(kanji.get(position).getFurigana());
        lbl_meaning.setText(kanji.get(position).getMeaning());
        lbl_difficulty.setText(kanji.get(position).getDifficulty() + "/9");

        return rowView;
    }
}
