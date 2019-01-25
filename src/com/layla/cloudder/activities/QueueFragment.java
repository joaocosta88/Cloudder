package com.layla.cloudder.activities;

import com.actionbarsherlock.app.SherlockListFragment;
import com.layla.cloudder.R;
import com.layla.cloudder.utils.MySimpleAdapter;
import com.layla.cloudder.utils.Utils;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

public class QueueFragment extends SherlockListFragment {

	private LayoutInflater inflater;
	private View view;
	private ListView lv;
	private static final int CONTEXT_MENU_SYNC_ID = 5;
	private static final int CONTEXT_MENU_REMOVE_ID = 6;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		this.inflater = inflater;
		if (Utils.queueSize > 0) {
			view = inflater.inflate(R.layout.list, null);
		}
		else 
			view = inflater.inflate(R.layout.empty_queue, null);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		lv = getListView();
		
		FragmentsManager.queueAdapter = new MySimpleAdapter (inflater, lv.getId(), FragmentsManager.queueList);
		setListAdapter(FragmentsManager.queueAdapter);
		
		FragmentsManager.reloadQueueFragData();

		if (FragmentsManager.queueList.size()>0) {
			registerForContextMenu(getListView());
			lv = getListView();
			
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo)	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_MENU_SYNC_ID, 0, "Sync file");
		menu.add(0, CONTEXT_MENU_REMOVE_ID, 0, "Remove File");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		String target = (String) ((TextView) info.targetView.findViewById(R.id.path_to_root)).getText();

		if (item.getItemId() == CONTEXT_MENU_SYNC_ID) {
			Utils.syncOneFile(target);
			return true;
		}
		else if (item.getItemId() == CONTEXT_MENU_REMOVE_ID) {
			Utils.deleteFileFromQueue(target);
			FragmentsManager.reloadQueueFragData();
			return true;
		}
		return false;
	}
}
