package pl.szymongajewski.system.alarmowy.prefs;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by Szymon Gajewski on 13.12.2015.
 */
@SharedPref(SharedPref.Scope.UNIQUE)
public interface LoginPrefs {
    String ipAddress();
    String userName();
    String password();
}