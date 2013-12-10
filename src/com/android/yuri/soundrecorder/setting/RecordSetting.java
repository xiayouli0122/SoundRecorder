package com.android.yuri.soundrecorder.setting;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.yuri.soundrecorder.R;
import com.android.yuri.soundrecorder.SoundRecorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * 给录音键添加一个设置菜单
 * <p>1.设置录音的音频输出格式
 * <p>2.设置录音文件的保存路径
 * @author xiayouli 2012-12-20
 *
 */
public class RecordSetting extends Activity implements OnItemClickListener{
	private static final String TAG = "RecordSetting";
	
	private ListView formatListView;
	//added by yuri.xia for cr0000144 begin
	private ListView dirListView;
	private ArrayList<HashMap<String, String>> dirList = new ArrayList<HashMap<String, String>>();
	private static final int BROWSER_REQUEST_CODE = 0x345;
	//added by yuri.xia for cr0000144 end
	
	private SimpleAdapter simpleAdapter;
	
	private ArrayList<HashMap<String, String>> formatList = new ArrayList<HashMap<String, String>>();
	
	private static final String TITLE = "title";
	private static final String CONTENT = "content";
	
	private static final int FORMAT = 0x00;
	private static final int DIRECTORY = 0x01;
	
	private int choiceItem = 0;
	
	private SharedPreferences sharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.menu_setting);
		
		setTitle(R.string.menu_setting);
		
		formatListView = (ListView)findViewById(R.id.format_list);
		
		initList(FORMAT);
		formatListView.setAdapter(simpleAdapter);
		formatListView.setOnItemClickListener(this);
		
		//added by yuri.xia for cr0000144 begin
		dirListView = (ListView)findViewById(R.id.dir_list);
		initList(DIRECTORY);
		dirListView.setAdapter(simpleAdapter);
		dirListView.setOnItemClickListener(this);
		//added by yuri.xia for cr0000144 end
		
		sharedPreferences = this.getSharedPreferences(SoundRecorder.SHARED_NAME, Context.MODE_PRIVATE);
	}
		

	public void initList(int type) {
		HashMap<String, String> map;
		switch (type) {
		case FORMAT:
			map = new HashMap<String, String>();
			map.put(TITLE,this.getResources().getString(R.string.audio_format));
			map.put(CONTENT,this.getResources().getString(R.string.audio_formate_tip));
			formatList.add(map);
			simpleAdapter = new SimpleAdapter(this, formatList,
					R.layout.item_list, new String[] { TITLE, CONTENT },
					new int[] { R.id.text1, R.id.text2 });
			break;
		//added by yuri.xia for cr0000144 begin
		case DIRECTORY:
			map = new HashMap<String, String>();
			map.put(TITLE,this.getResources().getString(R.string.file_save_to));
			map.put(CONTENT,SoundRecorder.file_save_path);
			dirList.add(map);
			simpleAdapter = new SimpleAdapter(this, dirList,
					R.layout.item_list, new String[] { TITLE, CONTENT },
					new int[] { R.id.text1, R.id.text2 });
			break;
		//added by yuri.xia for cr0000144 end
		default:
			break;
		}
	}
	
	//added by yuri.xia for cr0000144 begin
	public void updateList(String content){
		dirList.clear();
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(TITLE,this.getResources().getString(R.string.file_save_to));
		map.put(CONTENT, content);
		dirList.add(map);
		simpleAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			String path = bundle.getString("RESULT_PATH");
			updateList(path);
			//save
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(SoundRecorder.FILE_PATH, path);
			editor.commit();
		}else if(resultCode == RESULT_CANCELED){
			//do nothing
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	//added by yuri.xia for cr0000144 end
	
	@Override
	public void onItemClick(AdapterView<?> view, View v, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.format_list:
			//read last save choice
			choiceItem = sharedPreferences.getInt(SoundRecorder.AUDIO_FORMAT, 0);
			
			new AlertDialog.Builder(this)
				.setTitle(R.string.audio_format)
				.setSingleChoiceItems(new String[]{"AMR","3GPP"}, choiceItem, new OnClickListener() {
					
					public void onClick(DialogInterface dialog, int witch) {
						SharedPreferences.Editor editor = sharedPreferences.edit();
						//saved
						editor.putInt(SoundRecorder.AUDIO_FORMAT, witch);	
						editor.commit();
						
						if (witch == 0) {
							SoundRecorder.audio_extension = SoundRecorder.AMR_EXTENSION;
						}else if (witch == 1) {
							SoundRecorder.audio_extension = SoundRecorder.THREEGPP_EXTENSION;
						}
						
						dialog.dismiss();
					}
				})
				.setNegativeButton(android.R.string.cancel, null).show();
			break;
			//added by yuri.xia for cr0000144 begin
		case R.id.dir_list:
			Intent intent = new Intent();
			intent.setClass(RecordSetting.this, FileSelectDialog.class);
			startActivityForResult(intent, BROWSER_REQUEST_CODE);
			break;
			//added by yuri.xia for cr0000144 end
		default:
			break;
		}
		
	}

}
