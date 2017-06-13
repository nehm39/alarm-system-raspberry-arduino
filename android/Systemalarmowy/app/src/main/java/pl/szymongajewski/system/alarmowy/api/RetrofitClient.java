package pl.szymongajewski.system.alarmowy.api;

import com.google.gson.Gson;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.IOException;
import java.util.List;

import pl.szymongajewski.system.alarmowy.model.Alarm;
import pl.szymongajewski.system.alarmowy.model.Configuration;
import pl.szymongajewski.system.alarmowy.prefs.LoginPrefs_;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Szymon Gajewski on 15.12.2015.
 */
@EBean(scope = EBean.Scope.Singleton)
public class RetrofitClient {
    private ApiService apiService;
    private Retrofit retrofit;

    public ApiService getApiService() {
        return apiService;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public void setRetrofit(String apiURL) {
        Gson gson = new Gson();

        OkHttpClient okHttpClient = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClient.interceptors().add(interceptor);
        okHttpClient.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                HttpUrl url = chain.request().httpUrl()
                        .newBuilder()
                        .addQueryParameter("user", loginPrefs.userName().getOr(""))
                        .addQueryParameter("pass", loginPrefs.password().getOr(""))
                        .build();
                Request request = chain.request().newBuilder().url(url).build();
                return chain.proceed(request);
            }
        });

        if (!apiURL.contains("http://")) apiURL = "http://" + apiURL;
        apiURL += "/";

        retrofit = new Retrofit.Builder()
                .baseUrl(apiURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    @Pref
    LoginPrefs_ loginPrefs;

    public interface ApiService {
        @GET("check_credentials.php")
        Call<ResponseBody> checkCredentials();

        @GET("get_system_status.php")
        Call<ResponseBody> getSystemStatus();

        @GET("get_configuration_list.php")
        Call<List<Configuration>> getConfigurationsList();

        @GET("edit_configuration.php")
        Call<ResponseBody> editConfiguration(@Query("id") String id, @Query("value") String value);

        @GET("get_alarms.php")
        Call<List<Alarm>> getAlarmsList();

        @GET("get_pictures_list.php")
        Call<List<String>> getPicturesList();

        @GET("get_pictures_dir.php")
        Call<ResponseBody> getPicturesDir();

        @GET("delete_alarm.php")
        Call<ResponseBody> deleteAlarm(@Query("id") String id);

        @GET("delete_all_alarms.php")
        Call<ResponseBody> deleteAllAlarms();

        @GET("delete_picture.php")
        Call<ResponseBody> deletePicture(@Query("file") String file);

        @GET("delete_all_pictures.php")
        Call<ResponseBody> deleteAllPictures();

        @GET("start_system.php")
        Call<ResponseBody> startSystem();

        @GET("stop_system.php")
        Call<ResponseBody> stopSystem();

        @GET("restart_system.php")
        Call<ResponseBody> restartSystem();
    }

    public RetrofitClient() {
    }
}
