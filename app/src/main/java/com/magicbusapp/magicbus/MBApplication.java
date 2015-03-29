package com.magicbusapp.magicbus;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by giuseppe on 14/03/15.
 */
public class MBApplication extends Application {
    private static final String TAG = MBApplication.class.getSimpleName();
    private static SharedPreferences prefs;
    private static List<JsonObject> fermateJsonObjects;
    static boolean app_in_background = false;

    @Override
    public void onCreate()
    {
        Log.d(TAG, "onCreate");
        super.onCreate();

        Firebase.setAndroidContext(this);

        Log.d(TAG, "setUpParse: inizializzazione + facebook");
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        // Inizializzazione della libreria Parse

        Parse.initialize(this, getString(R.string.PARSE_APP_ID), getString(R.string.PARSE_CLIENT_KEY));
        // Inizializzazione parse&facebook
        ParseFacebookUtils.initialize(getString(R.string.FACEBOOK_APP_ID));
        ParseTwitterUtils.initialize(getString(R.string.TWITTER_CONSUMER_KEY), getString(R.string.TWITTER_CONSUMER_SECRET));


        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        ImageLoaderConfiguration config  =
                ImageLoaderConfiguration.createDefault(getBaseContext());
        ImageLoader.getInstance().init(config);
    }

    public boolean showWizard(){
        return prefs.getBoolean("showWizard", true);
    }

    public static void setShowWizard(boolean s){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("showWizard", s);
        editor.commit();
    }
/*
    public String getOpenFireServerAddress(){

        return prefs.getString("openfireServerAddress", ":(");
    }

    public int getOpenFirePort(){
        return 5222;
    }

    public static void setRoomname(String s){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("roomname", s);
        editor.commit();
    }

    public String getRoomname(){
        return prefs.getString("roomname", "magicroom");
    }

    public static void setOpenfireServerAddress(String s){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("openfireServerAddress", s);
        editor.commit();
    }

    public String getOpenFireProvider(){
        return "conference.magicbusapp.it";
    }
*/
    public String getNickname(){

        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser!=null){
            String nickname = currentUser.getString("nickname");
            if(nickname!=null)
                return nickname;
            else
                return prefs.getString("nickname", "MagicUserrrr");
        }
        return "Ops: errore :(";
    }

    public static void setNickname(String n){
        Log.d(TAG, "setNickname");

        if(TextUtils.isEmpty(n))
            n = "magicUser";

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nickname", n);
        editor.commit();
    }

    public static int getDistanzaKmFermate(){
        return 10;
    }

    public static int getDistanzaMetriChat(){

        return 10000;
    }

    public static String getAvatarPreference(){
        return prefs.getString("avatarPreference", "default");
    }

    public static void setAvatarDefault(){
        Log.d(TAG, "setAvatarDefault");

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("avatarPreference", "fry");
        editor.commit();
    }

    public static void setAvatarFacebook(){
        Log.d(TAG, "setAvatarDefault");

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("avatarPreference", "facebook");
        editor.commit();
    }

    public static void updatePreferences(ParseUser user, boolean facebook) {
        Log.d(TAG, "updatePreferences");

        setNickname(user.getString("nickname"));
        if(facebook){
            setAvatarFacebook();
        }
        else{
            setAvatarDefault();
        }
    }

    public static List<JsonObject> getFermateJsonObjects() {
        return fermateJsonObjects;
    }

    public static void setFermateParseObjects(List<JsonObject> fermateJsonObjects) {
        MBApplication.fermateJsonObjects = fermateJsonObjects;
    }

    public static boolean isPushEnabled(){
        return prefs.getBoolean("notifichePush", true);
    }

}
