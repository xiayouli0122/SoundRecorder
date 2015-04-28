package com.android.yuri.soundrecorder;

import java.io.File;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;

public class RecorderList extends ListActivity implements OnItemClickListener, MultiChoiceModeListener,
					LoaderCallbacks<Cursor>{
	private static final String TAG = "RecorderList";
	
	private ListView mListView;
	private String mRecorderFolder;
	private SharedPreferences sp;
	private RecorderListAdapter mAdapter;
	
	private static final String[] PROJECTION = {
		MediaStore.Audio.Media._ID,
		MediaStore.Audio.Media.DURATION,MediaColumns.DATA,
		MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.SIZE,
		MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.DISPLAY_NAME
	};
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recordlist_main);
		final ActionBar bar = getActionBar();
		int flags = ActionBar.DISPLAY_HOME_AS_UP;
		int change = bar.getDisplayOptions() ^ flags;
		bar.setDisplayOptions(change, flags);
		
		sp = getSharedPreferences(SoundRecorder.SHARED_NAME, MODE_PRIVATE);
		String record_path = sp.getString(SoundRecorder.FILE_PATH, null);
		if (null == record_path) {
			mRecorderFolder = "";
		}else {
			int lastindex = record_path.lastIndexOf("/");
			mRecorderFolder = record_path.substring(lastindex + 1, record_path.length());
		}
		Log.d(TAG, "mRecorderFolder:" + mRecorderFolder);
		
		mListView = getListView();
		mListView.setOnItemClickListener(this);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mListView.setMultiChoiceModeListener(this);
		mListView.setEmptyView(findViewById(R.id.tv_empty));
		
		mAdapter = new RecorderListAdapter(getApplicationContext(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		mListView.setAdapter(mAdapter);
		
		getLoaderManager().initLoader(0, null, this);
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (android.R.id.home == item.getItemId()) {
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.list_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_delete:
			final List<String> selectedPathList = mAdapter.getSelectedPathList();
			
			new AlertDialog.Builder(this)
				.setTitle("确定删除？")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage("将删除" + selectedPathList.size() + "个录音。")
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						File file = null;
						for(String path : selectedPathList){
							file = new File(path);
							if (file.exists()) {
								file.delete();
							}
							
							String where = MediaColumns.DATA + "=?";
							String[] whereArgs = new String[] { path };
							ContentResolver cr = getContentResolver();
							try {
								cr.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where, whereArgs);
							} catch (Exception e) {
								// TODO: handle exception
								Log.e(TAG, "Error in delete file in media store:" + e.toString());
							}
						}
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create().show();
			
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// TODO Auto-generated method stub
		mAdapter.unSelectedAll();
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		// TODO Auto-generated method stub
		mAdapter.setSelected(position, checked);
		mAdapter.notifyDataSetChanged();
		
		int selectCount = mAdapter.getSelectedItemCount();
		mode.setTitle("已选中 " + selectCount);
	}
	
	public void setSubTitle(){
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cursor = mAdapter.getCursor();
		cursor.moveToPosition(position);
		String url = cursor.getString(cursor.getColumnIndex(MediaColumns.DATA));
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(new File(url));
		intent.setDataAndType(uri, "audio/*");
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		String artist = getApplicationContext().getString(R.string.audio_db_artist_name);
		String selection = MediaStore.Audio.Media.ARTIST + "=?";
		String[] selectionArgs = {artist};
		
		return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, selection, selectionArgs,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO Auto-generated method stub
		mAdapter.changeCursor(data);
		mAdapter.initSelectArray();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		mAdapter.changeCursor(null);
	}
}
