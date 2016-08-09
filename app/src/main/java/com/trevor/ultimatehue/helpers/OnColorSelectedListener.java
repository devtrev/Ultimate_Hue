package com.trevor.ultimatehue.helpers;

import com.trevor.ultimatehue.HueColor;

/**
 * Created by nemo on 9/19/15.
 */
public interface OnColorSelectedListener {
    void onColorSelected(HueColor hueColor);
    //void onColorSelected(CycleHueColor hueColor);
    void onEffectSelected(Effect effect);
}
