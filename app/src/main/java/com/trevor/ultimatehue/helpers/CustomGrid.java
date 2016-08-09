package com.trevor.ultimatehue.helpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trevor.ultimatehue.HueColor;
import com.trevor.ultimatehue.R;

import java.util.List;

/**
 * Created by nemo on 9/19/15.
 */
public class CustomGrid extends BaseAdapter {
    private Context mContext;
    private List<HueColor> colorList;
    //private TypedArray imageid;
    private int imageId;

    /*public CustomGrid(Context c,String[] color,int[] Imageid ) {
          mContext = c;
          this.Imageid = Imageid;
          this.color = color;
    }*/

    public CustomGrid(Context c, List<HueColor> colorList) {
        mContext = c;
        this.colorList = colorList;
    }

    @Override
    public int getCount() {
        if(colorList == null)
            return 0;
        else
            return colorList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //
        // Note that you can't load this view inside another Scrollable view
        //

        try {
            if (colorList == null) {
                TypedArray images = mContext.getResources().obtainTypedArray(imageId);
                if (convertView == null) {
                    grid = inflater.inflate(R.layout.grid_color_item, null);
                    TextView textView = (TextView) grid.findViewById(R.id.grid_text);

                    textView.setText("Error - No Colors Found");

                    images.recycle();

                } else {
                    grid = (View) convertView;
                }
            } else {
                if (convertView == null) {
                    grid = inflater.inflate(R.layout.grid_color_item, null);
                    TextView textView = (TextView) grid.findViewById(R.id.grid_text);
                    ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);

                    //imageView.setImageResource(colorList.get(position).getImageId());
                    imageView.setImageResource(mContext.getResources().getIdentifier(colorList.get(position).getImageId(), "mipmap", mContext.getPackageName()));
                    imageView.setSoundEffectsEnabled(true);
                    textView.setText(colorList.get(position).getName());

                } else {
                    grid = (View) convertView;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return grid;
    }
}
