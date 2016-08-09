package com.trevor.ultimatehue.helpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trevor.ultimatehue.R;

import java.util.List;

/**
 * Created by nemo on 9/19/15.
 */
public class CustomEffectGrid extends BaseAdapter {
    private Context mContext;
    private String[] color;
    private List<Effect> effectList;
    //private TypedArray imageid;
    private int imageId;

    public CustomEffectGrid(Context c, List<Effect> effectList) {
        mContext = c;
        this.effectList = effectList;
    }

    @Override
    public int getCount() {
        if(effectList == null)
            return color.length;
        else
            return effectList.size();
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

        if (effectList == null) {
            TypedArray images = mContext.getResources().obtainTypedArray(imageId);
            if (convertView == null) {
                grid = new View(mContext);
                grid = inflater.inflate(R.layout.grid_color_item, null);
                TextView textView = (TextView) grid.findViewById(R.id.grid_text);
                ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);


                textView.setText(color[position]);
                imageView.setImageResource(images.getResourceId(position, -1));

                images.recycle();

        } else {
            grid = (View) convertView;
        }
    }  else {
            if (convertView == null) {
                grid = new View(mContext);
                grid = inflater.inflate(R.layout.grid_color_item, null);
                TextView textView = (TextView) grid.findViewById(R.id.grid_text);
                ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);

                textView.setText(effectList.get(position).getName());
                int imageId = mContext.getResources().getIdentifier(effectList.get(position).getImageId(), "mipmap", mContext.getPackageName());
                if(imageId != 0)
                    imageView.setImageResource(imageId);
                else // If image ID was not found then set to unknown image
                    imageView.setImageResource(mContext.getResources().getIdentifier("ic_unknown", "mipmap", mContext.getPackageName()));
            } else {
                grid = (View) convertView;
            }
    }

        return grid;
    }
}
