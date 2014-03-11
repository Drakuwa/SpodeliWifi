package com.app.wifipass.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.wifipass.R;
import com.app.wifipass.pojos.AccessPoint;
import com.app.wifipass.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Milena on 2/24/14.
 */
public class AdapterShowAllAps extends BaseAdapter {

    private ArrayList<AccessPoint> mItems;
    private Context mContext;

    public AdapterShowAllAps(Context context, ArrayList<AccessPoint> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public AccessPoint getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AccessPoint item = getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(com.app.wifipass.R.layout.custom_layout, null);

            Holder holder = new Holder();
            holder.mAPName = (TextView) convertView.findViewById(com.app.wifipass.R.id.textViewAP);
            holder.mPass = (TextView) convertView.findViewById(com.app.wifipass.R.id.textViewPassword);
            holder.mNew = (TextView) convertView.findViewById(com.app.wifipass.R.id.text_view_new);
            Typeface typeface = Utils.getTypeface(mContext);

            holder.mAPName.setTypeface(typeface);
            holder.mPass.setTypeface(typeface);
            holder.mNew.setTypeface(typeface);
            convertView.setTag(holder);
        }

        Holder holder = (Holder) convertView.getTag();
        holder.mAPName.setText(item.getName());
        holder.mPass.setText(item.getPassword());

        if (item.isLast()) {
            holder.mAPName.setTextColor(Color.parseColor("#4B4D3E"));
            holder.mPass.setTextColor(Color.parseColor("#696B58"));
            holder.mNew.setText(R.string.latest);
        } else {
            holder.mAPName.setTextColor(Color.parseColor("#AAABA2"));
            holder.mPass.setTextColor(Color.parseColor("#BEBFB8"));
            holder.mNew.setText("");
        }
        return convertView;
    }

    private class Holder {
        TextView mAPName;
        TextView mPass;
        TextView mNew;
    }
}
