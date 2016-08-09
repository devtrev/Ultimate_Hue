package com.trevor.ultimatehue.effectPicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.trevor.ultimatehue.HueColor;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.factory.EffectsFactory;
import com.trevor.ultimatehue.fragments.AllLightsFragment;
import com.trevor.ultimatehue.fragments.EffectsFragment;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.Effect;
import com.trevor.ultimatehue.helpers.OnColorSelectedListener;

/**
 * An activity representing a single ColorPickerActivityV2 detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link EffectPickerActivityVListActivity}.
 */
public class EffectPickerActivityVDetailActivity extends AppCompatActivity implements OnColorSelectedListener {

    public static final String TAG = EffectPickerActivityVDetailActivity.class.toString();

    private String lightIdentifier;
    private boolean isGroup;

    private OnColorSelectedListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorpickeractivityv_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Try to get The Light that was passed in for updating so we can pass it back later
        try {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                lightIdentifier = "-1";
                isGroup = false;
            } else {
                lightIdentifier = extras.getString(Constants.LIGHT_IDENTIFIER);
                isGroup = extras.getBoolean(Constants.IS_GROUP);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error Trying get the light resource passed in");
            e.printStackTrace();

            lightIdentifier= "-1";
        }

        mListener = (OnColorSelectedListener) this;

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(EffectPickerActivityVDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(EffectPickerActivityVDetailFragment.ARG_ITEM_ID));
            EffectPickerActivityVDetailFragment fragment = new EffectPickerActivityVDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.colorpickeractivityv_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, EffectPickerActivityVListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        navigateUpTo(new Intent(this, EffectPickerActivityVListActivity.class));
    }

    @Override
    public void onColorSelected(HueColor hueColor) {
        Log.i(TAG, "onColorSelected");

        if(hueColor != null) {
            //openDatabase();
            //ColorFactory.updateTimesClicked(db, hueColor);
            //closeDatabase();

            Intent intent = new Intent(this.getApplicationContext(), AllLightsFragment.class);
            intent.putExtra(Constants.COLOR_TYPE, Constants.HUE_COLOR);
            intent.putExtra(Constants.NAME, hueColor.getName());
            intent.putExtra(Constants.HUE, hueColor.getHue());
            intent.putExtra(Constants.SATURATION, hueColor.getSaturation());
            intent.putExtra(Constants.BRIGHTNESS, hueColor.getBrightness());
            intent.putExtra(Constants.HUE_IMAGE, hueColor.getImageId());
            intent.putExtra(Constants.LIGHT_IDENTIFIER, lightIdentifier);
            setResult(AllLightsFragment.ALL_LIGHTS_ACTIVITY_RESULT, intent);

        } else {
            Intent intent = new Intent(this.getApplicationContext(), AllLightsFragment.class);
            intent.putExtra(Constants.COLOR_TYPE, Constants.HUE_COLOR);
            intent.putExtra(Constants.HUE, -1);
            intent.putExtra(Constants.SATURATION, -1);
            intent.putExtra(Constants.BRIGHTNESS, -1);
            intent.putExtra(Constants.HUE_IMAGE, -1);
            intent.putExtra(Constants.LIGHT_IDENTIFIER, lightIdentifier);
            setResult(AllLightsFragment.ALL_LIGHTS_ACTIVITY_RESULT, intent);
        }

        // Close the Color Picker View
        finish();
    }

    @Override
    public void onEffectSelected(Effect effect) {
        Log.i(TAG, "onEffectSelected");

        if(effect != null) {
            //openDatabase();
            //EffectsFactory.updateTimesClicked(db, effect);
            //closeDatabase();

            Intent intent = new Intent(this.getApplicationContext(), AllLightsFragment.class);
            intent.putExtra(Constants.EFFECT_NAME, effect.getName());
            intent.putExtra(Constants.HUE, effect.getHue());
            intent.putExtra(Constants.SATURATION, effect.getSaturation());
            intent.putExtra(Constants.BRIGHTNESS, effect.getBrightness());
            intent.putExtra(Constants.HUE_IMAGE, effect.getImageId());
            intent.putExtra(Constants.SLEEP, effect.getSleep());
            intent.putExtra(Constants.RANDOM_LIGHT, effect.getRandomLight());
            intent.putExtra(Constants.TRANSITION_TIME, effect.getTransitionTime());
            intent.putExtra(Constants.EFFECT_SOUND, effect.getSoundId());
            intent.putExtra(Constants.EFFECT_DESCRIPTION, effect.getDescription());
            setResult(EffectsFragment.EFFECT_PICKER_RESULT, intent);

        } else {
            Intent intent = new Intent(this.getApplicationContext(), AllLightsFragment.class);
            intent.putExtra(Constants.EFFECT_NAME, "NA");
            intent.putExtra(Constants.HUE, -1);
            intent.putExtra(Constants.SATURATION, -1);
            intent.putExtra(Constants.BRIGHTNESS, -1);
            intent.putExtra(Constants.HUE_IMAGE, -1);
            intent.putExtra(Constants.SLEEP, -1);
            intent.putExtra(Constants.EFFECT_SOUND, -1);
            intent.putExtra(Constants.EFFECT_DESCRIPTION, "");
            setResult(EffectsFragment.EFFECT_PICKER_RESULT, intent);
        }

        // Close the Effect Picker View
        finish();
    }
}
