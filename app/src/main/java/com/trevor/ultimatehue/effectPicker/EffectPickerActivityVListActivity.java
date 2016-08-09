package com.trevor.ultimatehue.effectPicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.fragments.EffectsFragment;
import com.trevor.ultimatehue.helpers.Constants;

import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link EffectPickerActivityVDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class EffectPickerActivityVListActivity extends AppCompatActivity {

    public static final String TAG = EffectPickerActivityVListActivity.class.toString();
    public static final int COLOR_PICKER_ACTIVITY = 99;
    public static final int EFFECT_PICKER_ACTIVITY = 50;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorpickeractivityv_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        setTitle("Choose Category");

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        View recyclerView = findViewById(R.id.colorpickeractivityv_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.colorpickeractivityv_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(EffectPickerContent.ITEMS));
    }

    // Starts Activity to choose color/effect. Starts appropriate one based off of detailType passed in
    public void startForResult(Intent intent, int detailType) {
        Log.d(TAG, "startForResult() -  Starting color/effect picker activity");

        if(detailType == Constants.DETAIL_TYPE_COLOR)
            startActivityForResult(intent, COLOR_PICKER_ACTIVITY);
        else if (detailType == Constants.DETAIL_TYPE_EFFECT)
            startActivityForResult(intent, EFFECT_PICKER_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG , "Got result, now need to exit and return it");

        super.onActivityResult(requestCode, resultCode, data);

        setResult(EffectsFragment.EFFECT_PICKER_RESULT, data);
        finish();
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<EffectPickerContent.EffectItem> mValues;

        public SimpleItemRecyclerViewAdapter(List<EffectPickerContent.EffectItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.colorpickeractivityv_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            //holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).name);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(EffectPickerActivityVDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        EffectPickerActivityVDetailFragment fragment = new EffectPickerActivityVDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.colorpickeractivityv_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, EffectPickerActivityVDetailActivity.class);
                        intent.putExtra(EffectPickerActivityVDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        startForResult(intent, holder.mItem.detailType);
                        //context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            //public final TextView mIdView;
            public final TextView mContentView;
            public EffectPickerContent.EffectItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                //mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
