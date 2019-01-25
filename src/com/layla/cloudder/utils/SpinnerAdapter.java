package com.layla.cloudder.utils;

import com.layla.cloudder.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SpinnerAdapter extends BaseAdapter {

	private Drawable[] mIcons;
	private String[] mTitles;
	private Context mContext;
	private LayoutInflater mInflator;
	private static final String TAG = "SpinnerAdapter";

	public SpinnerAdapter(Context context, Drawable[] icons, String[] titles){
		mContext = context;
		mInflator = (LayoutInflater)mContext.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mIcons = icons;
		mTitles = titles;
	}

	@Override
	public int getCount() {
		return mTitles.length;
	}

	@Override
	public Object getItem(int position) {
		return mTitles[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			convertView = mInflator.inflate(R.layout.navigation_list_item, parent, false);
			holder = new ViewHolder();
			holder.icon = (ImageView)convertView.findViewById(R.id.icon);
			holder.title = (TextView)convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		holder.title.setText(mTitles[position]);
		holder.icon.setImageDrawable(mIcons[position]);

		return convertView;
	}   

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			convertView = mInflator.inflate(R.layout.navigation_list_dropdown_item, parent, false);
			holder = new ViewHolder();
			holder.icon = (ImageView)convertView.findViewById(R.id.icon);
			holder.title = (TextView)convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		holder.title.setText(mTitles[position]);
		holder.icon.setImageDrawable(mIcons[position]);

		return convertView;
	}

	private class ViewHolder{
		public ImageView icon;
		public TextView title;
	}
}

