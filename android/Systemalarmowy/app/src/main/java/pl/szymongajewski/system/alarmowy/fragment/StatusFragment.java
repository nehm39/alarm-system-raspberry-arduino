package pl.szymongajewski.system.alarmowy.fragment;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import pl.szymongajewski.system.alarmowy.R;
import pl.szymongajewski.system.alarmowy.activity.MainActivity;
import pl.szymongajewski.system.alarmowy.api.RetrofitClient;
import pl.szymongajewski.system.alarmowy.other.Constants;
import retrofit.Call;
import retrofit.Response;

/**
 * Created by Szymon Gajewski on 29.12.2015.
 */
@EFragment(R.layout.fragment_status)
public class StatusFragment extends Fragment {
    private static final int START_SYSTEM = 0;
    private static final int STOP_SYSTEM = 1;
    private static final int RESTART_SYSTEM = 2;


    @Bean
    RetrofitClient retrofitClient;

    @ViewById(R.id.txt_status_value)
    TextView txtStatus;
    @ViewById(R.id.status_main_layout)
    RelativeLayout mainLayout;
    @ViewById(R.id.profile_progress_bar)
    ProgressBar progressBar;

    @Click(R.id.start_button)
    void startClick() {
        showStatusChangeDialog();
        sendCommandToSystem(START_SYSTEM);
    }

    @Click(R.id.stop_button)
    void stopClick() {
        showStatusChangeDialog();
        sendCommandToSystem(STOP_SYSTEM);
    }

    @Click(R.id.restart_button)
    void restartClick() {
        showStatusChangeDialog();
        sendCommandToSystem(RESTART_SYSTEM);
    }

    @UiThread
    void showStatusChangeDialog() {
        changeSystemStatusProgressDialog = ProgressDialog.show(getActivity(), "", context.getString(R.string.system_change_dialog_msg), true, true);
    }

    private ProgressDialog refreshProgressDialog;
    private ProgressDialog changeSystemStatusProgressDialog;
    private Context context;

    public StatusFragment() {
    }

    @AfterViews
    void init() {
        context = getActivity().getApplicationContext();
        setHasOptionsMenu(true);
        checkSystemStatus(false);
    }

    @Background
    void checkSystemStatus(boolean refresh) {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<ResponseBody> checkSystemStatus = service.getSystemStatus();
            Response<ResponseBody> response = checkSystemStatus.execute();
            if (response != null) {
                switch (response.code()) {
                    case 200:
                        switch (response.body().string()) {
                            case Constants.API_SYSTEM_WORKING_MSG:
                                setStatus(context.getString(R.string.status_working_text), Color.GREEN, refresh);
                                break;
                            case Constants.API_SYSTEM_NOT_WORKING_MSG:
                                setStatus(context.getString(R.string.status_not_working_text), Color.RED, refresh);
                                break;
                            default:
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
            if (refresh) hideRefreshProgressDialog();
            else hideProgressBar();
            if (e.getMessage() != null) Log.e(Constants.RETROFIT_ERROR_LOG, e.getMessage());
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to check status");
            showToast(context.getString(R.string.retrofit_error));
        }
    }

    @UiThread
    void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    void setStatus(String message, int color, boolean refresh) {
        txtStatus.setText(message);
        txtStatus.setTextColor(color);
        if (refresh) refreshProgressDialog.dismiss();
        else {
            progressBar.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
        }
    }

    @UiThread
    void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @UiThread
    void hideRefreshProgressDialog() {
        refreshProgressDialog.dismiss();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.status_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refreshProgressDialog = ProgressDialog.show(getActivity(), "", context.getString(R.string.refresh_progress_dialog_msg), true, true);
            checkSystemStatus(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Background
    void sendCommandToSystem(int code) {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<ResponseBody> call = null;
            switch (code) {
                case START_SYSTEM:
                    call = service.startSystem();
                    break;
                case STOP_SYSTEM:
                    call = service.stopSystem();
                    break;
                case RESTART_SYSTEM:
                    call = service.restartSystem();
                    break;
            }
            Response<ResponseBody> response = call.execute();
            if (response != null) {
                switch (response.code()) {
                    case 200:
                        if (response.body().string().equals(Constants.API_SUCCESS_MSG)) {
                            dismissStatusChangeProgressDialog();
                            checkSystemStatus(false);
                            showToast(context.getString(R.string.status_change_toast));
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
            dismissStatusChangeProgressDialog();
            if (e.getMessage() != null) Log.e(Constants.RETROFIT_ERROR_LOG, e.getMessage());
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to change status");
            showToast(context.getString(R.string.retrofit_error));
        }
    }

    @UiThread
    void dismissStatusChangeProgressDialog() {
        changeSystemStatusProgressDialog.dismiss();
    }

}
