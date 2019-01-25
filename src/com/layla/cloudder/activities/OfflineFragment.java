package com.layla.cloudder.activities;

import java.io.File;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.layla.cloudder.R;
import com.layla.cloudder.utils.MySimpleAdapter;
import com.layla.cloudder.utils.Utils;

public class OfflineFragment extends SherlockListFragment{
	private static final int CONTEXT_MENU_DELETE_ID = 3;
	private static final int CONTEXT_MENU_OPEN_ID = 4;
	private LayoutInflater inflater;
	private ListView lv;
	private View view;
	private boolean emptyFolder = true;
	private File f;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		this.inflater = inflater;

		f = new File(Utils.LOCAL_PATH);

		if (!f.exists())
			f.mkdir();

		if (f.listFiles() != null) {
			if (f.listFiles().length > 0) {
				view = inflater.inflate(R.layout.list, null);
				emptyFolder = false;
			}
			else 
				view = inflater.inflate(R.layout.empty_folder, null);
		}
		else 
			view = inflater.inflate(R.layout.empty_folder, null);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		lv = getListView();
		FragmentsManager.offlineAdapter = new MySimpleAdapter (inflater, lv.getId(), FragmentsManager.offlineList);
		setListAdapter(FragmentsManager.offlineAdapter);
		
		if (!emptyFolder) {
			
			Utils.openDir(Utils.LOCAL_PATH);
			registerForContextMenu(lv);

			//listener da lista
			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					String isDir = (String) ((TextView)v.findViewById(R.id.is_dir)).getText();
					String newPath = (String) ((TextView)v.findViewById(R.id.path_to_root)).getText();

					if (isDir.equals("true")) 
						Utils.openDir(newPath);
					else 
						Utils.openFile(new File(newPath));

				}
			});
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_MENU_DELETE_ID, 0, "Delete File");
		menu.add(0, CONTEXT_MENU_OPEN_ID, 0, "Open File");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		String target = (String) ((TextView) info.targetView.findViewById(R.id.path_to_root)).getText();
		//	String fileName = (String) ((TextView) info.targetView.findViewById(R.id.name_entry)).getText();

		if (item.getItemId() == CONTEXT_MENU_DELETE_ID) {
			Utils.deleteLocalFile(target);
			FragmentsManager.reloadOfflineFragData();
		}
		else if (item.getItemId() == CONTEXT_MENU_OPEN_ID) {
			Utils.openFile(new File(target));
			return true;
		}
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
