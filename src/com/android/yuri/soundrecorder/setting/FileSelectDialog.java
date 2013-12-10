package com.android.yuri.soundrecorder.setting;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.android.yuri.soundrecorder.R;
import com.android.yuri.soundrecorder.SoundRecorder;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 文件夹选择对话框
 * @author xiayouli
 * @deprecated 允许用户设置指定路径保存录音文件 
 */
public class FileSelectDialog extends ListActivity implements OnClickListener{
	private static final String TAG = "FileDialog";

	//不能用以下字符命名文件夹
	private static String[] errorStr = {":","<",">","*","\\","|","?","/"};
	
	private static final int CREATE = 0x01;
	private static final int SELECT = 0x02;
	
	private static final String ITEM_KEY = "key";
	private static final String ITEM_IMAGE = "image";

	//“/"路径下大部分是没有写权限的，所以尽量将根目录设置为"/sdcard/"
//	private static final String ROOT = "/";
	private static final String ROOT = "/sdcard/";
	private static final String DEFAULT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private String start_path = "";

	public static final String RESULT_PATH = "RESULT_PATH";

	public static final String SELECTION_MODE = "SELECTION_MODE";

	private List<String> path = null;
	private TextView myPath;
	private EditText mFileName;
	private ArrayList<HashMap<String, Object>> mList;

	private Button selectButton;

	private LinearLayout layoutSelect;
	private LinearLayout layoutCreate;
	private InputMethodManager inputManager;
	private String parentPath;
	private String currentPath = ROOT;

	private int selectionMode = SelectionMode.MODE_CREATE;

	private String[] formatFilter = null;

	private boolean canSelectDir = false;

	private File selectedFile;
	private HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();
	
	public class SelectionMode {
		public static final int MODE_CREATE = 0;
		public static final int MODE_OPEN = 1;
	}

	private SharedPreferences sharedPreferences;
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED, getIntent());

		setContentView(R.layout.file_dialog_main);
		myPath = (TextView) findViewById(R.id.path);
		mFileName = (EditText) findViewById(R.id.fdEditTextFile);

		//软键盘管�?		
		inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		
		//get sharedPreference
		sharedPreferences = this.getSharedPreferences(SoundRecorder.SHARED_NAME, Context.MODE_PRIVATE);

		selectButton = (Button) findViewById(R.id.fdButtonSelect);
		selectButton.setEnabled(false);
		selectButton.setOnClickListener(this);

		final Button newButton = (Button) findViewById(R.id.fdButtonNew);
		newButton.setOnClickListener(this);

		selectionMode = getIntent().getIntExtra(SELECTION_MODE,
				SelectionMode.MODE_CREATE);

		if (selectionMode == SelectionMode.MODE_OPEN) {
			newButton.setEnabled(false);
		}

		layoutSelect = (LinearLayout) findViewById(R.id.fdLinearLayoutSelect);
		layoutCreate = (LinearLayout) findViewById(R.id.fdLinearLayoutCreate);
		layoutCreate.setVisibility(View.GONE);

		final Button cancelButton = (Button) findViewById(R.id.fdButtonCancel);
		cancelButton.setOnClickListener(this);
		
		final Button createButton = (Button) findViewById(R.id.fdButtonCreate);
		createButton.setOnClickListener(this);
		
		//默认打开当前的保存路�?		
		start_path = sharedPreferences.getString(SoundRecorder.FILE_PATH, DEFAULT_PATH);
		getDir(start_path);
	}

	private void getDir(String dirPath) {

		File file = new File(dirPath);
		selectedFile = file;
		selectButton.setEnabled(true);
		
		boolean useAutoSelection = dirPath.length() < currentPath.length();

		Integer position = lastPositions.get(parentPath);

		getDirImpl(dirPath);

		if (position != null && useAutoSelection) {
			getListView().setSelection(position);
		}

	}
	
	public boolean floderNameFormatVerify(String name){
		System.out.println("floderNameFormatVerify-->" + name);
		//文件名不能是"."
		if (name.equals(".")) {
			showToast(this.getResources().getString(R.string.error_msg_01) + "\".\"");
			return false;
		}else if (name.equals("..")) {//文件名不能是".."
			showToast(this.getResources().getString(R.string.error_msg_01) + "\"..\"");
			return false;
		}else {
			for (int i = 0; i < errorStr.length; i++) {
				if (name.indexOf(errorStr[i]) >= 0) {
					showToast(this.getResources().getString(R.string.error_msg_02) + "*:?<>\"\\/");
					return false;
				}
			}
		}
		return true;
	}
	
	private void showToast(String msg){
		Toast.makeText(FileSelectDialog.this, msg, Toast.LENGTH_LONG).show();
	}
	
	private void showToast(int rId){
		Toast.makeText(FileSelectDialog.this, rId, Toast.LENGTH_LONG).show();
	}

	private void getDirImpl(final String dirPath) {

		currentPath = dirPath;

		final List<String> item = new ArrayList<String>();
		path = new ArrayList<String>();
		mList = new ArrayList<HashMap<String, Object>>();

		File f = new File(currentPath);
		File[] files = f.listFiles();
		if (files == null) {
			currentPath = ROOT;
			f = new File(currentPath);
			files = f.listFiles();
		}
		myPath.setText(getText(R.string.location) + ": " + currentPath);

		if (!currentPath.equals(ROOT)) {

			item.add(ROOT);
			addItem(ROOT, R.drawable.format_folder);
			path.add(ROOT);

			item.add("../");
			addItem("../", R.drawable.format_folder);
			path.add(f.getParent());
			parentPath = f.getParent();

		}

		TreeMap<String, String> dirsMap = new TreeMap<String, String>();
		TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
		TreeMap<String, String> filesMap = new TreeMap<String, String>();
		TreeMap<String, String> filesPathMap = new TreeMap<String, String>();
		for (File file : files) {
			if (!file.isHidden()) {
				if (file.isDirectory()) {
					String dirName = file.getName();
					dirsMap.put(dirName, dirName);
					dirsPathMap.put(dirName, file.getPath());
				} else {
					final String fileName = file.getName();
					final String fileNameLwr = fileName.toLowerCase();
					
					if (formatFilter != null) {
						boolean contains = false;
						for (int i = 0; i < formatFilter.length; i++) {
							final String formatLwr = formatFilter[i].toLowerCase();
							if (fileNameLwr.endsWith(formatLwr)) {
								contains = true;
								break;
							}
						}
						if (contains) {
							filesMap.put(fileName, fileName);
							filesPathMap.put(fileName, file.getPath());
						}
					} else {
						filesMap.put(fileName, fileName);
						filesPathMap.put(fileName, file.getPath());
					}
				}
			}
		}
		item.addAll(dirsMap.tailMap("").values());
		item.addAll(filesMap.tailMap("").values());
		path.addAll(dirsPathMap.tailMap("").values());
		path.addAll(filesPathMap.tailMap("").values());

		SimpleAdapter fileList = new SimpleAdapter(this, mList,
				R.layout.file_dialog_row,
				new String[] { ITEM_KEY, ITEM_IMAGE }, new int[] {
						R.id.fdrowtext, R.id.fdrowimage });

		for (String dir : dirsMap.tailMap("").values()) {
			addItem(dir, R.drawable.format_folder);
		}

		for (String file : filesMap.tailMap("").values()) {
			addItem(file, R.drawable.format_text);
		}

		fileList.notifyDataSetChanged();

		setListAdapter(fileList);

	}

	private void addItem(String fileName, int imageId) {
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(ITEM_KEY, fileName);
		item.put(ITEM_IMAGE, imageId);
		mList.add(item);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fdButtonSelect:
			if (selectedFile != null) {
				getIntent().putExtra(RESULT_PATH, selectedFile.getPath());
				setResult(RESULT_OK, getIntent());
				finish();
			}
			break;
		case R.id.fdButtonCreate:
			if (mFileName.getText().length() > 0) {
				String newPath = currentPath + mFileName.getText().toString().trim();
				
				if (!floderNameFormatVerify(mFileName.getText().toString().trim())) {
					mFileName.setText("");
					return;
				}
				
				File newDir = new File(newPath);
				if (!newDir.exists()) {
					newDir.mkdirs();
				}
				
				if (!newDir.canWrite()) {
					showToast(R.string.create_fail);
					setResult(RESULT_CANCELED);
					break;
				}
				
				getIntent().putExtra(RESULT_PATH, newPath);
				setResult(RESULT_OK, getIntent());
				finish();
			}
			break;
		case R.id.fdButtonNew:
			setLayoutVisible(v, CREATE);

			mFileName.setText("新建文件夹");
			mFileName.selectAll();
			mFileName.requestFocus();
			break;
		case R.id.fdButtonCancel:
			setLayoutVisible(v, SELECT);
			break;

		default:
			break;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		File file = new File(path.get(position));

		setLayoutVisible(v, SELECT);

		if (file.isDirectory()) {
			selectButton.setEnabled(false);
			if (file.canRead()) {
				lastPositions.put(currentPath, position);
				getDir(path.get(position));
				if (canSelectDir) {
					selectedFile = file;
					v.setSelected(true);
					selectButton.setEnabled(true);
				}
			} else {
				new AlertDialog.Builder(this)
						.setIcon(R.drawable.format_folder)
						.setTitle(
								"[" + file.getName() + "] "
										+ getText(R.string.cant_read_folder))
						.setPositiveButton(android.R.string.ok, null).show();
			}
		} else {
			//do nothing
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			setResult(RESULT_CANCELED);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void setLayoutVisible(View view,int type){
		switch (type) {
		case CREATE:
			layoutCreate.setVisibility(View.VISIBLE);
			layoutSelect.setVisibility(View.GONE);
			break;
		case SELECT:
			layoutCreate.setVisibility(View.GONE);
			layoutSelect.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
		//hide soft keyboard	
		inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
		
		if (new File(currentPath).isDirectory()) {
			selectButton.setEnabled(true);
		}else {
			selectButton.setEnabled(false);
		}
	}

}
