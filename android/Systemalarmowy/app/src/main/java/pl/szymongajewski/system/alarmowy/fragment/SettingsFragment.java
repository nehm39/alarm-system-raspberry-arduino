package pl.szymongajewski.system.alarmowy.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import pl.szymongajewski.system.alarmowy.R;
import pl.szymongajewski.system.alarmowy.activity.MainActivity;
import pl.szymongajewski.system.alarmowy.api.RetrofitClient;
import pl.szymongajewski.system.alarmowy.model.Configuration;
import pl.szymongajewski.system.alarmowy.other.Constants;
import retrofit.Call;
import retrofit.Response;

/**
 * Created by Szymon Gajewski on 28.12.2015.
 */
@EFragment
public class SettingsFragment extends PreferenceFragment {

    @Bean
    RetrofitClient retrofitClient;

    public SettingsFragment() {
    }

    private Context context;
    private SharedPreferences settings;
    private ProgressDialog progressDialog;
    private ProgressDialog updateProgressDialog;
    private static final String settingsLogAlarmsKey = "settingsLogAlarms";
    private static final String settingsSendMailKey = "settingsSendMail";
    private static final String settingsPicturesPerSecondKey = "settingsPicturesPerSecond";
    private static final String settingsSenderMailKey = "settingsSenderMail";
    private static final String settingsReceiverMailKey = "settingsReceiverMail";
    private static final String settingsMailSubjectKey = "settingsMailSubject";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        context = getActivity().getApplicationContext();
        progressDialog = ProgressDialog.show(getActivity(), "", context.getString(R.string.settings_progress_dialog_msg), true, true);
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        getPreferences();
    }

    @Background
    void getPreferences() {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<List<Configuration>> configurationListCall = service.getConfigurationsList();
            Response<List<Configuration>> response = configurationListCall.execute();
            if (response != null) {
                switch (response.code()) {
                    case 200:
                        setPreferences(response.body());
                        break;
                    case 403:
                        if (response.errorBody().string().equals(Constants.API_FORBIDDEN_MSG)) {
                            showToast(context.getString(R.string.wrong_credentials));
                            ((MainActivity) getActivity()).logout();
                        } else throw new Exception();
                        break;
                    default:
                        throw new Exception();
                }
            } else throw new Exception();
        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(Constants.RETROFIT_ERROR_LOG, e.getMessage());
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to download settings");
            showToast(context.getString(R.string.retrofit_error));
        }
    }

    @Background
    void changeSettings(int id, String value) {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<ResponseBody> editConfigurationCall = service.editConfiguration(Integer.toString(id), value);
            Response<ResponseBody> response = editConfigurationCall.execute();
            if (response != null) {
                switch (response.code()) {
                    case 200:
                        if (response.body().string().equals(Constants.API_SUCCESS_MSG)) {
                            dismissDialog(updateProgressDialog);
                        }
                        else {
                            throw new Exception();
                        }
                        break;
                    case 403:
                        if (response.errorBody().string().equals(Constants.API_FORBIDDEN_MSG)) {
                            showToast(context.getString(R.string.wrong_credentials));
                            ((MainActivity) getActivity()).logout();
                        } else throw new Exception();
                        break;
                    default:
                        throw new Exception();
                }
            } else throw new Exception();
        } catch (Exception e) {
            dismissDialog(updateProgressDialog);
            if (e.getMessage() != null) Log.e(Constants.RETROFIT_ERROR_LOG, e.getMessage());
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to update settings");
            showToast(context.getString(R.string.retrofit_error));
        }
    }

    @UiThread
    void dismissDialog(ProgressDialog dialog) {
        dialog.dismiss();
    }

    @UiThread
    void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    void setPreferences(List<Configuration> configurationList) {
        SharedPreferences.Editor editor = settings.edit();

        for (Configuration conf : configurationList) {
            switch (conf.getId()) {
                case 1:
                    if (conf.getValue().equals("true"))
                        editor.putBoolean(context.getString(R.string.settings_log_alarms_key), true);
                    else
                        editor.putBoolean(context.getString(R.string.settings_log_alarms_key), false);
                    break;
                case 2:
                    if (conf.getValue().equals("true"))
                        editor.putBoolean(context.getString(R.string.settings_send_email_key), true);
                    else
                        editor.putBoolean(context.getString(R.string.settings_send_email_key), false);
                    break;
                case 3:
                    editor.putString(context.getString(R.string.settings_sender_mail_key), conf.getValue());
                    break;
                case 4:
                    editor.putString(context.getString(R.string.settings_receiver_mail_key), conf.getValue());
                    break;
                case 5:
                    editor.putString(context.getString(R.string.settings_mail_subject_key), conf.getValue());
                    break;
                case 6:
                    editor.putString(context.getString(R.string.settings_pictures_per_second_key), conf.getValue());
                    break;
            }
        }
        editor.commit();
        addPreferencesToView();
    }

    @UiThread
    void addPreferencesToView() {
        addPreferencesFromResource(R.xml.settings);
        progressDialog.dismiss();

        Preference.OnPreferenceChangeListener preferenceChangeListener = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateProgressDialog = ProgressDialog.show(getActivity(), "", context.getString(R.string.settings_update_progress_dialog_msg), true, true);
                switch (preference.getKey()) {
                    case settingsLogAlarmsKey:
                        changeSettings(1, newValue.toString());
                        break;
                    case settingsSendMailKey:
                        changeSettings(2, newValue.toString());
                        break;
                    case settingsSenderMailKey:
                        changeSettings(3, newValue.toString());
                        break;
                    case settingsReceiverMailKey:
                        changeSettings(4, newValue.toString());
                        break;
                    case settingsMailSubjectKey:
                        changeSettings(5, newValue.toString());
                        break;
                    case settingsPicturesPerSecondKey:
                        changeSettings(6, newValue.toString());
                        break;
                }
                return true;
            }
        };

        CheckBoxPreference logAlarmsPref = (CheckBoxPreference) findPreference(settingsLogAlarmsKey);
        logAlarmsPref.setOnPreferenceChangeListener(preferenceChangeListener);
        CheckBoxPreference sendMailPref = (CheckBoxPreference) findPreference(settingsSendMailKey);
        sendMailPref.setOnPreferenceChangeListener(preferenceChangeListener);
        ListPreference picturesPerSecondPref = (ListPreference) findPreference(settingsPicturesPerSecondKey);
        picturesPerSecondPref.setOnPreferenceChangeListener(preferenceChangeListener);
        EditTextPreference mailSenderPref = (EditTextPreference) findPreference(settingsSenderMailKey);
        mailSenderPref.setOnPreferenceChangeListener(preferenceChangeListener);
        EditTextPreference mailReceiverPref = (EditTextPreference) findPreference(settingsReceiverMailKey);
        mailReceiverPref.setOnPreferenceChangeListener(preferenceChangeListener);
        EditTextPreference mailSubjectPref = (EditTextPreference) findPreference(settingsMailSubjectKey);
        mailSubjectPref.setOnPreferenceChangeListener(preferenceChangeListener);
    }

}