package com.layla.cloudder.utils;

import java.util.ArrayList;

import com.layla.cloudder.R;
import com.layla.cloudder.utils.DBFile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MySimpleAdapter extends ArrayAdapter<DBFile> {
	LayoutInflater inflater;
	ArrayList<DBFile> data;

	public MySimpleAdapter(LayoutInflater inflater, int textViewResourceId, ArrayList<DBFile> data) {
		super(inflater.getContext(), textViewResourceId);
		this.inflater=inflater;
		this.data = data;
	}

	public static class ViewHolder {
		public TextView textView;
		public TextView isDirView;
		public TextView pathToRootView;
		public ImageView iconView;
		public TextView serviceView;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder holder;

		if (v == null) {
			LayoutInflater mLayoutinflater = inflater;
			v = mLayoutinflater.inflate(R.layout.list_entry, null);
			holder = new ViewHolder();
			holder.textView = (TextView) v.findViewById(R.id.name_entry);
			holder.isDirView = (TextView) v.findViewById(R.id.is_dir);
			holder.pathToRootView = (TextView) v.findViewById(R.id.path_to_root);
			holder.iconView = (ImageView) v.findViewById(R.id.image_entry);
			holder.serviceView = (TextView) v.findViewById(R.id.service);
			v.setTag(holder);
		}
		else
			holder = (ViewHolder) v.getTag();


		final DBFile file = (DBFile) data.get(position) ;

		if(file != null) {
			if (file.isDir()) {
				holder.iconView.setImageResource(inflater.getContext().getResources().getIdentifier("folder48", "drawable","com.layla.cloudder"));
				holder.textView.setText(file.getName());
				holder.isDirView.setText(file.isDir()+"");
				holder.pathToRootView.setText(file.getPathToRoot());
			} else {
				holder.textView.setText(file.getName());
				holder.isDirView.setText(file.isDir()+"");
				holder.pathToRootView.setText(file.getPathToRoot());
				holder.serviceView.setText(file.getDestiny());
				
				//icons have the same name as extension
				//if there's not a icon with same name
				//getIdentifier will return 0
				//in that case, use "_blank" icon
				int resourceID = inflater.getContext().getResources().getIdentifier(file.getExtension(), "drawable","com.layla.cloudder");
				if (resourceID == 0)
					holder.iconView.setImageResource(
							inflater.getContext().getResources().getIdentifier("_blank", "drawable","com.layla.cloudder"));
				else
					holder.iconView.setImageResource(resourceID);

			}
		}
		return v;
	}
}
