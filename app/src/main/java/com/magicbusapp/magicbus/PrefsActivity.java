package com.magicbusapp.magicbus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MenuItem;

import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class PrefsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = PrefsActivity.class.getSimpleName();

    public static final String FACEBOOK = "Facebook_Check_Box";
    public static final String AVATAR = "avatarPreference";
    public static final String NICKNAME = "nickname";
    public static final String LOGOUT = "logout";
    public static final String CONDIVIDI = "condividi";
    public static final String NOTIFICHE_PUSH = "notifichePush";
    public static final String ABOUT = "about";

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setTitle("Impostazioni");
        addPreferencesFromResource(R.xml.prefs);

        findPreference(ABOUT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference pref) {

                AlertDialog alertDialog = new AlertDialog.Builder(PrefsActivity.this).create();
                alertDialog.setTitle("About MagicBus");
                alertDialog.setMessage("MagicBus è l’app che non ti lascia mai a piedi: semplice, social," +
                        " green.\nNasce e si sviluppa grazie al contributo di community locali " +
                        "per fornire informazioni real-time sui trasporti pubblici in città.");
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Add your code for the button here.
                    }
                });
                // Set the Icon for the Dialog
                alertDialog.setIcon(R.drawable.ic_launcher);
                alertDialog.show();
                // see http://androidsnippets.com/simple-alert-dialog-popup-with-title-message-icon-and-button

                return true;
            }

        });

        findPreference(NICKNAME).setSummary(
                ParseUser.getCurrentUser().getString(NICKNAME));

        findPreference(LOGOUT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference pref) {
                Log.d(TAG, "onPreferenceClick");
                if(pref.getKey().equals(LOGOUT)){
                    new AlertDialog.Builder(PrefsActivity.this)
                            .setTitle("Logout")
                            .setIcon(R.drawable.ic_launcher)
                            .setMessage("Confermi di voler uscire?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {
                                    doLogout();
                                }

                            }).create().show();
                    return true;
                }
                return false;
            }
        });

		/*
		 * if (!ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
		 * findPreference(FACEBOOK).setSummary("Non connesso"); } else{
		 * findPreference(FACEBOOK).setSummary("Connesso"); }
		 */

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Log.d("Preference", key);

        final Preference connectionPref = findPreference(key);

        if (key.equals(FACEBOOK)) {
            Log.d(key, ": " + sharedPreferences.getBoolean(key, false));
            if (sharedPreferences.getBoolean(key, false)) {
                Log.d(key, "Linko l'utente con facebook");

                final ParseUser user = ParseUser.getCurrentUser();
                if (!ParseFacebookUtils.isLinked(user)) {

                    ParseFacebookUtils.link(user, this, new SaveCallback() {

                        @Override
                        public void done(ParseException ex) {
                            if (ParseFacebookUtils.isLinked(user)) {
                                Log.d("MyApp",
                                        "Woohoo, user logged in with Facebook!");

                                ParseFacebookUtils.saveLatestSessionData(user);

                                MBUtils.showInfoToast(PrefsActivity.this,
                                        "Adesso sei connesso con Facebook");

                                // Set summary to be the user-description for
                                // the selected value
                                connectionPref.setSummary("Connesso");
                            }
                        }

                    });
                }
            } else {
                Log.d(key, "Unlinko l'utente da facebook");

                final ParseUser user = ParseUser.getCurrentUser();
                ParseFacebookUtils.unlinkInBackground(user, new SaveCallback() {
                    @Override
                    public void done(ParseException ex) {
                        if (ex == null) {
                            Log.d("MyApp",
                                    "The user is no longer associated with their Facebook account.");
                            MBUtils.showInfoToast(PrefsActivity.this,
                                    "Account facebook scollegato!");

                            // Set summary to be the user-description for the
                            // selected value
                            connectionPref.setSummary("Non connesso");
                        }
                    }
                });
            }
        }

        else if (key.equals(NOTIFICHE_PUSH)){
            Log.d(NOTIFICHE_PUSH, sharedPreferences.getBoolean(key, true)+"");

            if(sharedPreferences.getBoolean(key, true)){
                //abilita push
                PushService.setDefaultPushCallback(this, MainActivity.class);
                //PushService.subscribe(this, "debug", MBMain.class);
            }
            else{
                //disabilita push
                PushService.setDefaultPushCallback(this, null);
                //PushService.unsubscribe(this, "debug");
            }
        }

        else if (key.equals(NICKNAME)) {
            Log.d(NICKNAME, sharedPreferences.getString(key, ""));
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
            // TODO
            // aggiornare il ParseUser
            ParseUser user = ParseUser.getCurrentUser();
            user.put(NICKNAME, sharedPreferences.getString(key, ""));
            user.saveInBackground(new SaveCallback(){

                @Override
                public void done(ParseException e) {
                    Log.d(TAG, "done");

                    findPreference(NICKNAME).setSummary(
                            ParseUser.getCurrentUser().getString(NICKNAME));

                    MBUtils.showSuccessToast(PrefsActivity.this,
                            "Nickname aggiornato: " + ParseUser.getCurrentUser().getString(NICKNAME));

                }

            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void doLogout() {
        Log.d(TAG, "doLogout");
        MBApplication.setShowWizard(true);
        MBApplication.setAvatarDefault();
        ParseUser.logOut();
        //PushService.unsubscribe(this, "debug");
        ParseInstallation.getCurrentInstallation().deleteInBackground();
        finish();
    }

}
