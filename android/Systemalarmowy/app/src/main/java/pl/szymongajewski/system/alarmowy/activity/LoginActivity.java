package pl.szymongajewski.system.alarmowy.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import pl.szymongajewski.system.alarmowy.R;
import pl.szymongajewski.system.alarmowy.api.RetrofitClient;
import pl.szymongajewski.system.alarmowy.other.Constants;
import pl.szymongajewski.system.alarmowy.prefs.LoginPrefs_;
import retrofit.Call;
import retrofit.Response;


/**
 * Created by Szymon Gajewski on 13.12.2015.
 */
@EActivity(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity {
    @Pref
    LoginPrefs_ loginPrefs;
    @Bean
    RetrofitClient retrofitClient;

    @ViewById(R.id.etxt_ip)
    EditText etxtIp;
    @ViewById(R.id.etxt_username)
    EditText etxtUsername;
    @ViewById(R.id.etxt_password)
    EditText etxtPassword;
    ProgressDialog progressDialog;

    @Click(R.id.login_button)
    void loginButtonClicked() {
        if (!etxtIp.getText().toString().isEmpty() && !etxtPassword.getText().toString().isEmpty() && !etxtUsername.getText().toString().isEmpty()) {
            progressDialog = ProgressDialog.show(this, "", getString(R.string.logging_in), true, true);
            retrofitClient.setRetrofit(etxtIp.getText().toString());
            loginPrefs.edit().ipAddress().put(etxtIp.getText().toString()).apply();
            loginPrefs.edit().userName().put(etxtUsername.getText().toString()).apply();
            loginPrefs.edit().password().put(etxtPassword.getText().toString()).apply();
            authenticateUser();
        } else showToast(context.getString(R.string.login_empty_fields));
    }

    private Context context;

    @AfterViews
    void init() {
        context = getApplicationContext();

        if (loginPrefs.ipAddress().exists() && loginPrefs.userName().exists() && loginPrefs.password().exists()) {
            retrofitClient.setRetrofit(loginPrefs.ipAddress().get());
            authenticateUser();
        }
    }

    @Background
    void authenticateUser() {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<ResponseBody> authenticateCall = service.checkCredentials();
            Response<ResponseBody> response = authenticateCall.execute();
            if (response != null) {
                switch (response.code()) {
                    case 200:
                        if (response.body().string().equals(Constants.API_SUCCESS_MSG)) {
                            MainActivity_.intent(this).start();
                            dismissProgressDialog();
                        }
                        else {
                            throw new Exception();
                        }
                        break;
                    case 403:
                        if (response.errorBody().string().equals(Constants.API_FORBIDDEN_MSG)) {
                            loginPrefs.clear();
                            showToast(context.getString(R.string.wrong_credentials));
                            dismissProgressDialog();
                        }
                        else throw new Exception();
                        break;
                    default:
                        throw new Exception();
                }
            } else throw new Exception();
        } catch (Exception e) {
            loginPrefs.clear();
            if (e.getMessage() != null) Log.e(Constants.RETROFIT_ERROR_LOG, e.getMessage());
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to authenticate");
            showToast(context.getString(R.string.login_error));
            dismissProgressDialog();
        }
    }

    @UiThread
    void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    @UiThread
    void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
