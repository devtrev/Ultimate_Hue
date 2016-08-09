package com.trevor.ultimatehue.colorPicker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nemo on 5/28/16.
 */
public class ColorPickerContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<ColorItem> ITEMS = new ArrayList<ColorItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, ColorItem> ITEM_MAP = new HashMap<String, ColorItem>();

    // This is where the list of possible colors are populated
    static {

        // Add some sample items.
        addItem(new ColorItem(Constants.BASIC_COLOR_TYPE, "Basic Colors", R.mipmap.ac_antique, Constants.DETAIL_TYPE_COLOR));
        addItem(new ColorItem(Constants.ADVANCED_COLOR_TYPE, "Advanced Colors", R.mipmap.ac_cool_mint, Constants.DETAIL_TYPE_COLOR));
        //addItem(new ColorItem(Constants.COUNTRY_EFFECT_COLOR_TYPE, "Countries" , R.mipmap.ac_halogen, Constants.DETAIL_TYPE_EFFECT));
        addItem(new ColorItem(Constants.CUSTOM_COLOR_TYPE, "Custom Color", R.mipmap.ac_princess_pink, Constants.DETAIL_TYPE_COLOR));
    }

    private static void addItem(ColorItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class ColorItem {
        public final String id;
        public final String name;
        public final int image;
        public final int detailType;

        public ColorItem(String id, String name, int image, int detailType) {
            this.id = id;
            this.name = name;
            this.image = image;
            this.detailType = detailType;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
