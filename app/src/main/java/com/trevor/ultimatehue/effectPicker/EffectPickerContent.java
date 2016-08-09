package com.trevor.ultimatehue.effectPicker;

import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.helpers.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nemo on 5/28/16.
 */
public class EffectPickerContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<EffectItem> ITEMS = new ArrayList<EffectItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, EffectItem> ITEM_MAP = new HashMap<String, EffectItem>();

    // This is where the list of possible colors are populated
    static {

        // Add some sample items.
        addItem(new EffectItem(Constants.COMMON_EFFECT_COLOR_TYPE, "Common Effect", R.mipmap.ac_red_moon, Constants.DETAIL_TYPE_EFFECT));
        addItem(new EffectItem(Constants.SEASONAL_EFFECT_COLOR_TYPE, "Seasonal Effect", R.mipmap.ac_sun_noon, Constants.DETAIL_TYPE_EFFECT));
    }

    private static void addItem(EffectItem item) {
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
    public static class EffectItem {
        public final String id;
        public final String name;
        public final int image;
        public final int detailType;

        public EffectItem(String id, String name, int image, int detailType) {
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
