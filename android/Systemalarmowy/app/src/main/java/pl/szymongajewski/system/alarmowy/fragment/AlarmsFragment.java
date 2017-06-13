package pl.szymongajewski.system.alarmowy.fragment;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pl.szymongajewski.system.alarmowy.R;
import pl.szymongajewski.system.alarmowy.activity.MainActivity;
import pl.szymongajewski.system.alarmowy.api.RetrofitClient;
import pl.szymongajewski.system.alarmowy.model.Alarm;
import pl.szymongajewski.system.alarmowy.other.AlarmsListAdapter;
import pl.szymongajewski.system.alarmowy.other.Constants;
import retrofit.Call;
import retrofit.Response;

/**
 * Created by Szymon Gajewski on 30.12.2015.
 */
@EFragment(R.layout.fragment_alarms)
public class AlarmsFragment extends Fragment {

    @Bean
    RetrofitClient retrofitClient;

    @ViewById(R.id.alarms_recycler_view)
    RecyclerView recyclerView;
    @ViewById(R.id.alarms_main_layout)
    RelativeLayout mainLayout;
    @ViewById(R.id.alarms_progress_bar)
    ProgressBar progressBar;
    private ProgressDialog deleteProgressDialog;
    private ProgressDialog deleteAllProgressDialog;

    private Context context;

    public AlarmsFragment() {
    }

    @AfterViews
    void init() {
        setHasOptionsMenu(true);
        context = getActivity().getApplicationContext();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        getAlarms(false);
    }

    @Background
    void getAlarms(boolean update) {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<List<Alarm>> alarmsListCall = service.getAlarmsList();
            Response<List<Alarm>> response = alarmsListCall.execute();
            if (response != null) {
                switch (response.code()) {
                    case 200:
                        List<Alarm> alarmsList = response.body();
                        Collections.sort(alarmsList, new Comparator<Alarm>() {
                            public int compare(Alarm alarm1, Alarm alarm2) {
                                return Integer.valueOf(alarm2.getId()).compareTo(alarm1.getId());
                            }
                        });
                        setAdapter(response.body());
                        if(update) dismissDialog(deleteProgressDialog);
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
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to download alarms");
            showToast(context.getString(R.string.retrofit_error));
            if(update) dismissDialog(deleteProgressDialog);
        }
    }

    @UiThread
    void setAdapter(List<Alarm> alarmsList) {
        AlarmsListAdapter alarmsListAdapter = new AlarmsListAdapter(alarmsList, this);
        recyclerView.setAdapter(alarmsListAdapter);
        progressBar.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.VISIBLE);
    }

    @UiThread
    void hideView() {
        mainLayout.setVisibility(View.INVISIBLE);
    }

    @UiThread
    void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.alarms_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_all_alarms) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(context.getString(R.string.delete_alarms_dialog_title));
            builder.setMessage(context.getString(R.string.delete_alarms_dialog_msg));
            builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    deleteAllProgressDialog = ProgressDialog.show(getActivity(), "", context.getString(R.string.deleting_msg), true, true);
                    deleteAllAlarmsFromApi();
                }
            });

            builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @UiThread
    public void deleteAlarm(int id) {
        deleteProgressDialog = ProgressDialog.show(getActivity(), "", context.getString(R.string.deleting_msg), true, true);
        deleteAlarmFromApi(id);
    }

    @Background
    void deleteAlarmFromApi(int id) {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<ResponseBody> deleteAlarmCall = service.deleteAlarm(Integer.toString(id));
            Response<ResponseBody> response = deleteAlarmCall.execute();
            if (response != null) {
                switch (response.code()) {
                    case 200:
                        if (response.body().string().equals(Constants.API_SUCCESS_MSG)) {
                            getAlarms(true);
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
            dismissDialog(deleteProgressDialog);
            if (e.getMessage() != null) Log.e(Constants.RETROFIT_ERROR_LOG, e.getMessage());
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to delete alarm");
            showToast(context.getString(R.string.retrofit_error));
        }
    }

    @Background
    void deleteAllAlarmsFromApi() {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<ResponseBody> deleteAlarmsCall = service.deleteAllAlarms();
            Response<ResponseBody> response = deleteAlarmsCall.execute();
            if (response != null) {
                switch (response.code()) {
                    case 200:
                        if (response.body().string().equals(Constants.API_SUCCESS_MSG)) {
                            dismissDialog(deleteAllProgressDialog);
                            hideView();
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
            dismissDialog(deleteAllProgressDialog);
            if (e.getMessage() != null) Log.e(Constants.RETROFIT_ERROR_LOG, e.getMessage());
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to delete alarms");
            showToast(context.getString(R.string.retrofit_error));
        }
    }

    @UiThread
    void dismissDialog(ProgressDialog progressDialog) {
        progressDialog.dismiss();
    }
}
