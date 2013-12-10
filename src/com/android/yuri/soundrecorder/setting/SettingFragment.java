package com.android.yuri.soundrecorder.setting;

import com.android.yuri.soundrecorder.R;
import com.android.yuri.soundrecorder.SoundRecorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

public class SettingFragment extends PreferenceFragment implements
		OnPreferenceChangeListener, OnPreferenceClickListener {
	private static final String TAG = "SettingActivity";

	private ListPreference mAudioFormatPreference;
	private String mAudioFormat;
	
	private PreferenceScreen mLoactionScreen;
	
	private SharedPreferences sp = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
		sp = getActivity().getApplicationContext().getSharedPreferences(SoundRecorder.SHARED_NAME, Context.MODE_PRIVATE);

		mAudioFormatPreference = (ListPreference) findPreference("audio_format_set");
		mAudioFormatPreference.setOnPreferenceChangeListener(this);

		mAudioFormat = PreferenceManager.getDefaultSharedPreferences(
				getActivity().getApplicationContext()).getString(
				mAudioFormatPreference.getKey(), "-1");
		Log.d(TAG, "mAudioFormat=" + mAudioFormat);

		mAudioFormatPreference
				.setSummary(mAudioFormatPreference.getEntries()[Integer
						.parseInt(mAudioFormat)]);
		
		mLoactionScreen = (PreferenceScreen) findPreference("location_set");
		mLoactionScreen.setOnPreferenceClickListener(this);
		String path = sp.getString(SoundRecorder.FILE_PATH, "click to set");
		mLoactionScreen.setSummary(path);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		if (mAudioFormatPreference == preference) {
			int index = mAudioFormatPreference.findIndexOfValue((String)newValue);
			mAudioFormatPreference.setSummary(mAudioFormatPreference.getEntries()[index]);
			mAudioFormatPreference.setValueIndex(index);
			
			Editor editor = sp.edit();
			editor.putInt(SoundRecorder.AUDIO_FORMAT, index);
			editor.commit();
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		if (mLoactionScreen == preference) {
			Intent intent = new Intent();
			intent.setClass(getActivity(), FileSelectDialog.class);
			startActivityForResult(intent, 0);
		}
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		getActivity();
		// TODO Auto-generated method stub
		if (resultCode == Activity.RESULT_OK) {
			Bundle bundle = data.getExtras();
			String path = bundle.getString("RESULT_PATH");
			mLoactionScreen.setSummary(path);
			//save
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(SoundRecorder.FILE_PATH, path);
			editor.commit();
		} else {
			getActivity();
			if(resultCode == Activity.RESULT_CANCELED){
				//do nothing
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
