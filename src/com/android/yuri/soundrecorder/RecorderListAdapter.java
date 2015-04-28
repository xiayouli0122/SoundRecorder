package com.android.yuri.soundrecorder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class RecorderListAdapter extends CursorAdapter {
	private static final String TAG = "RecorderListAdapter";
	private final LayoutInflater mInflater;
	private static final int THEME_COLOR_DEFAULT = 0x7F33b5e5;
	//default is normal mode
	private SparseBooleanArray mCheckedArray;
	
	public RecorderListAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mInflater = LayoutInflater.from(context);
		mCheckedArray = new SparseBooleanArray();
	}
	
	public void initSelectArray(){
		unSelectedAll();
	}
	
	public void setSelected(int position){
		mCheckedArray.put(position, !isSelected(position));
	}
	
	public void setSelected(int position, boolean isSelected){
		mCheckedArray.put(position, isSelected);
	}
	
	public int getSelectedItemCount(){
		int selectedCount = 0;
		for (int i = 0; i < mCheckedArray.size(); i++) {
			if (mCheckedArray.valueAt(i)) {
				selectedCount ++;
			}
		}
		return selectedCount;
	}
	
	public List<String> getSelectedPathList(){
		List<String> list = new ArrayList<String>();
		Cursor cursor = getCursor();
		String path;
		for (int i = 0; i < mCheckedArray.size(); i++) {
			if (mCheckedArray.valueAt(i)) {
				cursor.moveToPosition(i);
				path = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.DATA));
				list.add(path);
			}
		}
		
		return list;
	}
	
	public void unSelectedAll(){
		for (int i = 0; i < getCount(); i++) {
			setSelected(i, false);
		}
		notifyDataSetChanged();
	}
	
	public void selectAll(){
		for (int i = 0; i < getCount(); i++) {
			setSelected(i, true);
		}
		notifyDataSetChanged();
	}
	
	public boolean isSelected(int position){
		return mCheckedArray.valueAt(position);
	}
	
	@Override
	protected void onContentChanged() {
		// TODO Auto-generated method stub
		super.onContentChanged();
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView nameView = (TextView) view.findViewById(R.id.tv_name);
		TextView timeView = (TextView) view.findViewById(R.id.tv_time);
		TextView dateView = (TextView) view.findViewById(R.id.tv_date);
		TextView sizeView = (TextView) view.findViewById(R.id.tv_size);
		
//		String name = cursor.getString(cursor.getColumnIndex(MediaColumns.TITLE));
		String name = cursor.getString(cursor.getColumnIndex(MediaColumns.DISPLAY_NAME));
		long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
		String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
		long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
		
		nameView.setText(name);
		timeView.setText(Utils.mediaTimeFormat(time));
		dateView.setText(artist);
		sizeView.setText(Utils.getFormatSize(size));
		
		if (isSelected(cursor.getPosition())) {
			view.setBackgroundColor(THEME_COLOR_DEFAULT);
		}else {
			view.setBackgroundColor(Color.TRANSPARENT);
		}
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.recordlist_item, parent, false);
	}

}
