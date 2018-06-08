package com.aston.tanion.schedule.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.widget.BaseAdapter;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.activity.WeekActivity;
import com.aston.tanion.schedule.database.SharedPrefs;
import com.aston.tanion.schedule.utility.CommonMethod;
import com.aston.tanion.schedule.utility.Constant;

/**
 * Created by Aston Tanion on 12/03/2016.
 */
public class SettingsFragment extends PreferenceFragment {
    public static final String TAG = "SettingsFragment";

    private Context mContext;
    private Resources mResources;
    private SharedPrefs mPrefs;

    private Preference mWakeUpCallPref;
    private EditTextPreference mWakeUpCallEditPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResources = getResources();
        mContext = getActivity();
        mPrefs = SharedPrefs.get(mContext);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Week preference.
        PreferenceScreen weekPref = (PreferenceScreen) findPreference(Constant.WEEK_PREF);
        weekPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = WeekActivity.newIntent(getActivity());
                startActivity(i);
                return false;
            }
        });

        // Sound preference
        RingtonePreference soundPref = (RingtonePreference) findPreference(Constant.SOUND_PREF);
        // Get the sound preference value.
        String soundUri = (String) mPrefs.read(Constant.SOUND_PREF, "");
        setSoundSummary(soundPref, soundUri);
        soundPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setSoundSummary(preference, newValue.toString());
                return true;
            }
        });

        // Vibrator preference.
        ListPreference vibratoryPref = (ListPreference) findPreference(Constant.VIBRATOR_PREF);
        vibratoryPref.setSummary(vibratoryPref.getEntry());
        vibratoryPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Play the chosen vibration
                int choice = Integer.parseInt((String) newValue);
                Vibrator vibrator = (Vibrator)
                        getActivity().getSystemService(Context.VIBRATOR_SERVICE);

                if (choice != 0) {
                    try {
                        vibrator.vibrate(CommonMethod.getVibrator(choice), -1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Get a list of all the entry values
                String[] values = mResources.getStringArray(R.array.alarm_vibrator_option_values);

                int index = 0;
                // Check which index corresponds to newValue and assign it to index
                for (int i = 0; i < values.length; i++) {
                    if (values[i].equals(newValue)) {
                        index = i;
                    }
                }

                // Get a list of all entries
                String[] entries = mResources.getStringArray(R.array.alarm_vibrator_option_entries);
                preference.setSummary(entries[index]);
                return true;
            }
        });

        // Wake-up call
        mWakeUpCallPref = findPreference(Constant.WAKE_UP_CALL_PREF);
        setWakeUpCallSummary((boolean) mPrefs.read(Constant.WAKE_UP_CALL_SWITCH_PREF, false));

        mWakeUpCallEditPref = (EditTextPreference) findPreference(Constant.WAKE_UP_CALL_EDIT_PREF);
        String summary = String.format(
                mResources.getString(R.string.wake_up_call_edit_summary),
                (String) mPrefs.read(Constant.WAKE_UP_CALL_EDIT_PREF, ""));

        mWakeUpCallEditPref.setSummary(summary);
        mWakeUpCallEditPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                String value = (String) newValue;

                if (!value.contains(":") && !value.isEmpty()) {
                    value = value.concat(":00");

                } else {
                    return true;
                }

                String[] component = value.trim().split(":");
                int hour;
                int minute;

                try {
                    hour = Integer.parseInt(component[0].trim());
                    minute = Integer.parseInt(component[1].trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return true;
                }

                if (hour > 24 || minute > 59) return true;

                String timeFormat = String.format(
                        mResources.getString(R.string.hour_format),
                        CommonMethod.timeComponent(hour),
                        CommonMethod.timeComponent(minute));

                mWakeUpCallEditPref.setSummary(String.format(
                        mResources.getString(R.string.wake_up_call_edit_summary), timeFormat));

                mWakeUpCallPref.setSummary(null);
                mWakeUpCallPref.setSummary(String.format(
                        mResources.getString(R.string.wake_up_call_edit_summary), timeFormat));

                ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();

                return true;
            }
        });

        SwitchPreference wakeUpCallSwitchPref = (SwitchPreference) findPreference(Constant.WAKE_UP_CALL_SWITCH_PREF);
        wakeUpCallSwitchPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setWakeUpCallSummary((boolean) newValue);
                return true;
            }
        });
    }

    private void setWakeUpCallSummary(boolean newValue) {
        mWakeUpCallPref.setSummary(null);

        String timeFormat = (String) mPrefs.read(Constant.WAKE_UP_CALL_EDIT_PREF, "06:30");

        if (newValue) {
            mWakeUpCallPref.setSummary(String.format(
                    mResources.getString(R.string.wake_up_call_edit_summary), timeFormat));
        } else {
            mWakeUpCallPref.setSummary(mResources.getString(R.string.wake_up_call_default_summary));
        }

        ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
    }

    private void setSoundSummary(Preference preference, String newValue) {
        // Get the chosen ringtone.
        Ringtone ringtone = RingtoneManager.getRingtone(mContext, Uri.parse(newValue));
        // Set the title of the ringtone as a summary.
        preference.setSummary(ringtone.getTitle(mContext));
    }
}