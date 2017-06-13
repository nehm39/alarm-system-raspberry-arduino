package pl.szymongajewski.system.alarmowy.fragment;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.net.URL;
import java.util.List;

import pl.szymongajewski.system.alarmowy.R;
import pl.szymongajewski.system.alarmowy.activity.MainActivity;
import pl.szymongajewski.system.alarmowy.api.RetrofitClient;
import pl.szymongajewski.system.alarmowy.other.Constants;
import pl.szymongajewski.system.alarmowy.prefs.LoginPrefs_;
import retrofit.Call;
import retrofit.Response;

/**
 * Created by Szymon Gajewski on 25.12.2015.
 */
@EFragment(R.layout.fragment_images)
public class ImagesFragment extends Fragment {

    @Pref
    LoginPrefs_ loginPrefs;
    @ViewById(R.id.images_spinner)
    Spinner imagesSpinner;
    @ViewById(R.id.image_view)
    ImageView image;
    @Bean
    RetrofitClient retrofitClient;
    @ViewById(R.id.images_main_layout)
    RelativeLayout mainLayout;
    @ViewById(R.id.images_progress_bar)
    ProgressBar progressBar;

    private String picturesUrl;
    private Context context;
    private ProgressDialog deleteProgressDialog;
    private ProgressDialog deleteAllProgressDialog;


    public ImagesFragment() {
    }

    @AfterViews
    void init() {
        setHasOptionsMenu(true);
        context = getActivity().getApplicationContext();
        getPicturesList(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.images_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_image) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(context.getString(R.string.delete_image_dialog_title));
            builder.setMessage(context.getString(R.string.delete_image_dialog_msg));
            builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    deleteProgressDialog = ProgressDialog.show(getActivity(), "", context.getString(R.string.deleting_msg), true, true);
                    deletePictureFromApi(imagesSpinner.getSelectedItem().toString());
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
        if (id == R.id.action_delete_all_images) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(context.getString(R.string.delete_images_dialog_title));
            builder.setMessage(context.getString(R.string.delete_images_dialog_msg));
            builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    deleteAllProgressDialog = ProgressDialog.show(getActivity(), "", context.getString(R.string.deleting_msg), true, true);
                    deleteAllPicturesFromApi();
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

    @Background
    void deletePictureFromApi(String file) {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<ResponseBody> deletePictureCall = service.deletePicture(file);
            Response<ResponseBody> response = deletePictureCall.execute();
            if (response != null) {
                switch (response.code()) {
                    case 200:
                        if (response.body().string().equals(Constants.API_SUCCESS_MSG)) {
                            getPicturesList(true);
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
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to delete picture");
            showToast(context.getString(R.string.retrofit_error));
        }
    }

    @Background
    void deleteAllPicturesFromApi() {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<ResponseBody> deletePicturesCall = service.deleteAllPictures();
            Response<ResponseBody> response = deletePicturesCall.execute();
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
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to delete pictures");
            showToast(context.getString(R.string.retrofit_error));
        }
    }

    @Background
    void getPicturesList(boolean update) {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<List<String>> picturesListCall = service.getPicturesList();
            Response<List<String>> response = picturesListCall.execute();
            if (response != null) {
                switch (response.code()) {
                    case 200:
                        List<String> picturesList = response.body();
                        populateSpinner(picturesList);
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
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to download picture list");
            showToast(context.getString(R.string.retrofit_error));
            if(update) dismissDialog(deleteProgressDialog);
        }
    }

    @Background
    void getPicturesDir() {
        try {
            RetrofitClient.ApiService service = retrofitClient.getApiService();
            Call<ResponseBody> getPicturesDirCall = service.getPicturesDir();
            Response<ResponseBody> response = getPicturesDirCall.execute();;
            if (response != null) {
                switch (response.code()) {
                    case 200:
                        String path = response.body().string();
                        picturesUrl = loginPrefs.ipAddress().get() + "/" + path;
                        if (!picturesUrl.contains("http://")) picturesUrl = "http://" + picturesUrl;
                        enableView();
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
            else Log.e(Constants.RETROFIT_ERROR_LOG, "Failed to get pictures dir");
            showToast(context.getString(R.string.retrofit_error));
        }
    }

    @UiThread
    void populateSpinner(List<String> picturesList) {
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, picturesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imagesSpinner.setAdapter(adapter);
        getPicturesDir();
    }

    @UiThread
    void enableView() {
        imagesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadImageFromWeb(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        loadImageFromWeb(0);
        progressBar.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.VISIBLE);
    }

    @Background
    void loadImageFromWeb(int i) {
        try {
            String url = picturesUrl + imagesSpinner.getItemAtPosition(i).toString();
            url = url.replace(" ", "%20");
            URL imageUrl = new URL(url);
            Bitmap bitmap = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
            setImage(bitmap);
        } catch (Exception ex) {
            Log.e("setting_image_error", ex.getMessage());
        }
    }

    @UiThread
    void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    void setImage(Bitmap bitmap) {
        image.setImageBitmap(bitmap);
    }

    @UiThread
    void dismissDialog(ProgressDialog progressDialog) {
        progressDialog.dismiss();
    }

    @UiThread
    void hideView() {
        mainLayout.setVisibility(View.INVISIBLE);
    }

}
