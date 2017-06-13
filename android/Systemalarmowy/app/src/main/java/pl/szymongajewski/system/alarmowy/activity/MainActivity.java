package pl.szymongajewski.system.alarmowy.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import pl.szymongajewski.system.alarmowy.R;
import pl.szymongajewski.system.alarmowy.fragment.AlarmsFragment_;
import pl.szymongajewski.system.alarmowy.fragment.ImagesFragment_;
import pl.szymongajewski.system.alarmowy.fragment.SettingsFragment_;
import pl.szymongajewski.system.alarmowy.fragment.StatusFragment_;
import pl.szymongajewski.system.alarmowy.prefs.LoginPrefs_;

/**
 * Created by Szymon Gajewski on 12.12.2015.
 */
@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Pref
    LoginPrefs_ loginPrefs;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.drawer_layout)
    DrawerLayout drawer;
    @ViewById(R.id.nav_view)
    NavigationView navigationView;

    private boolean backPressed;
    private Bundle savedInstanceState;
    private int currentDrawerPosition = R.id.drawer_option_status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
    }

    @SuppressWarnings("ConstantConditions")
    @AfterViews
    void init() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Status systemu");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.drawer_option_status).setChecked(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new StatusFragment_()).commit();
        }


        View header = navigationView.getHeaderView(0);
        TextView headerIP = (TextView) header.findViewById(R.id.drawer_header_ip_adress);
        TextView headerUserName = (TextView) header.findViewById(R.id.drawer_header_user_name);
        headerIP.setText(loginPrefs.ipAddress().getOr(""));
        headerUserName.setText(loginPrefs.userName().getOr(""));
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (backPressed) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.press_back_to_exit), Toast.LENGTH_SHORT).show();
                backPressed = true;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = null;
        String tag = "";

        switch(id) {
            case R.id.drawer_option_status:
                getSupportActionBar().setTitle(getString(R.string.drawer_option_status));
                fragment = new StatusFragment_();
                break;
            case R.id.drawer_option_alarms:
                getSupportActionBar().setTitle(getString(R.string.drawer_option_alarms));
                fragment = new AlarmsFragment_();
                break;
            case R.id.drawer_option_pictures:
                getSupportActionBar().setTitle(getString(R.string.drawer_option_pictures));
                fragment = new ImagesFragment_();
                break;
            case R.id.drawer_option_settings:
                getSupportActionBar().setTitle(getString(R.string.drawer_option_settings));
                fragment = new SettingsFragment_();
                break;
            case R.id.drawer_option_logout:
                logout();
                break;
        }

        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, tag)
                    .commit();
            currentDrawerPosition = id;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout() {
        loginPrefs.clear();
        LoginActivity_.intent(this).start();
    }
}
