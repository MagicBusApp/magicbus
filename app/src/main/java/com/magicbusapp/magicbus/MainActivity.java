package com.magicbusapp.magicbus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.widget.WebDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonObject;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.AlertDialog.Builder;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    /*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    final static int MAP_FRAGMENT = 0;
    final static int CHAT_FRAGMENT = 1;
    final static int WIKIBUS_FRAGMENT = 2;

    final static String MAP = "Fermate bus";
    final static String CHAT = "MagicChat";
    final static String WIKIBUS = "WikiBus";

    protected static MapView map;
    protected static List<JsonObject> fermteList;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    protected CharSequence mTitle;

    protected GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    private MapFragment mapFragment;
    private Fragment chatFragment;
    private Fragment wikiFragment;

    private IGeoPoint lastCenterMap;

    static int CURRENT_PAGE = -1;
    private ProgressDialog progressBar;
    private IGeoPoint mapCenter;
    private int zoomLevel;
    private BoundingBoxE6 boundingBox;

    JsonObject fermataSelezionata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // enable ActionBar app icon to behave as action to toggle nav
        // drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setCustomView(R.layout.mb_actionbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        mRequestingLocationUpdates = true;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();

        if (MBApplication.getAvatarPreference().equals("default")) {
            MBApplication.setAvatarFacebook();
        }

        CURRENT_PAGE = -1;

        Log.d(TAG, "setupParse: notifiche push");

        if (MBApplication.isPushEnabled()) {
            Log.d(TAG, "notifiche push ON");
            PushService.startServiceIfRequired(this);
            PushService.setDefaultPushCallback(this, MainActivity.class);
            //PushService.subscribe(this, "tester", MainActivity.class);
        } else {
            Log.d(TAG, "notifiche push OFF");
            PushService.setDefaultPushCallback(this, null);
            //PushService.unsubscribe(this, "tester");
        }

        ParseInstallation.getCurrentInstallation().saveInBackground();

        if (savedInstanceState!=null){
            try{
                mapFragment = (MapFragment) getSupportFragmentManager().getFragment(savedInstanceState, "map");
//                chatFragment = getSupportFragmentManager().getFragment(savedInstanceState, "chat");
//                wikiFragment = getSupportFragmentManager().getFragment(savedInstanceState, "wiki");
            }
            catch (Exception e){
                Log.e(TAG, e.getLocalizedMessage());
            }

        }

        Intent intent = getIntent();
        checkNotificaPush(intent);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        checkNotificaPush(intent);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                //setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        Log.i(TAG, "createLocationRequest");

        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        switch (position){
            case MAP_FRAGMENT:
                goToMap(position);

                break;
            case CHAT_FRAGMENT:
                if (mCurrentLocation != null) {
                    //hideProgressBar();
                    goToChat(position);

                } else {
                    MBUtils.showWarningToast(this,
                            "Attendi qualche secondo, non sei stato ancora localizzato...");
                }
                break;
            case WIKIBUS_FRAGMENT:
                if (mCurrentLocation != null) {
                    goToWiki(position);
                }
                else
                    MBUtils.showWarningToast(this,
                            "Attendi qualche secondo, non sei stato ancora localizzato...");

                break;
        }
    }

    private void goToWiki(int position) {
        if (wikiFragment == null){
            wikiFragment = WikibusFragment.newInstance(position + 1);
        }

        mTitle = "WikiBus";

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, wikiFragment)
                .commit();
    }

    private void goToChat(int position) {
        if (chatFragment == null){
            chatFragment = new MBChat_Firebase();
        }

        mTitle = "MagicChat";

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, chatFragment).commit();

        // update selected item and title, then close the drawer
        //mDrawerList.setItemChecked(position, true);
        setTitle("MagicChat");
        //mDrawerLayout.closeDrawer(mDrawerList);

        //CURRENT_PAGE = MBCHAT;
    }

    private void goToMap(int position) {
        if (mapFragment == null){
            mapFragment = MapFragment.newInstance(position + 1);
        }

        mTitle = "Fermate bus";

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mapFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case MAP_FRAGMENT+1:
                mTitle = "Fermate bus";
                break;
            case CHAT_FRAGMENT+1:
                mTitle = "Magicchat";
                break;
            case WIKIBUS_FRAGMENT+1:
                mTitle = "WikiBus";
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //hideProgressBar();
            Intent intent = new Intent(MainActivity.this, PrefsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_share_fb) {
            condividiFacebook();
            return true;
        }
        if (id == R.id.action_share_twitter) {
            condividiTwitter();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");

        //Save the fragment's instance
        if (mapFragment != null && getSupportFragmentManager().findFragmentByTag("map") != null)
            getSupportFragmentManager().putFragment(savedInstanceState, "map", mapFragment);
/*
        if (chatFragment != null && getSupportFragmentManager().findFragmentByTag("chat") != null)
            getSupportFragmentManager().putFragment(savedInstanceState, "chat", chatFragment);

        if (wikiFragment != null && getSupportFragmentManager().findFragmentByTag("wiki") != null)
            getSupportFragmentManager().putFragment(savedInstanceState, "wiki", wikiFragment);
*/
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);

    }

    @Override
    protected void onStart() {
        super.onStart();
        showProgressDialog("Connessione in corso...");
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();

        ckeckCurrentUser();
        checkLocationProviders();
        MBApplication.app_in_background = false;

        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }

        MBApplication.app_in_background = true;

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        PushService.setDefaultPushCallback(this, MainActivity.class);
        //PushService.subscribe(this, "debug", MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        new Builder(this).setTitle("Spegni")
                .setIcon(R.drawable.ic_launcher)
                .setMessage("Vuoi chiudere MagicBus?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create().show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation != null) {

            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();

            if (ParseInstallation.getCurrentInstallation()!=null){
                Log.i(TAG,
                        "Inserisco nell'installation la location per le notifiche push");
                ParseInstallation.getCurrentInstallation().put("location",
                        new ParseGeoPoint(mCurrentLocation.getLatitude(),
                                mCurrentLocation.getLongitude()));
                ParseInstallation.getCurrentInstallation().saveInBackground();
            }

        }
        else {
            Toast.makeText(this, "Nessuna posizione trovata!", Toast.LENGTH_LONG).show();
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        //dismissProgressDialog();
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient != null && mLocationRequest != null)
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG,"onLocationChanged: " + location.toString());

        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        Toast.makeText(this, "onLocationChanged",
                Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        Log.i(TAG, "updateUI");
        //mCurrentLocation
        if (mapFragment != null && mCurrentLocation != null)// && mapFragment.isVisible())
            mapFragment.updateMapLocation(mCurrentLocation);
    }

    public Location getCurrentLocation() {
        if (mCurrentLocation != null)
            return mCurrentLocation;

        return null;
    }

    public void setMapCenter(IGeoPoint mapCenter) {
        this.mapCenter = mapCenter;
    }

    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public void setBoundingBox(BoundingBoxE6 boundingBox) {
        this.boundingBox = boundingBox;
    }

    public IGeoPoint getMapCenter(){
        return mapCenter;
    }

    public int getZoomLevel(){
        return zoomLevel;
    }

    public BoundingBoxE6 getBoundingBox(){
        return boundingBox;
    }

    protected void showProgressDialog(String message) {
        Log.d(TAG, "showProgressDialog");
        if (progressBar == null)
            progressBar = new ProgressDialog(MainActivity.this);
        progressBar.setMessage(message);
        progressBar.setCancelable(true);
        progressBar.setIcon(R.drawable.ic_launcher);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface obj) {
                Log.d(TAG, "onDismissProgressDialog");
            }
        });
        progressBar.show();
    }

    protected void dismissProgressDialog() {
        Log.d(TAG, "dismissProgressDialog");
        if (progressBar != null)
            progressBar.dismiss();
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    private void ckeckCurrentUser() {
        Log.d(TAG, "ckeckCurrentUser");
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // niente da fare
        } else {
            // show the signup or login screen
            Log.d(TAG, "Utente NON autenticato! Go to login activity...");
            Intent i = new Intent(MainActivity.this, MBAuthenticationActivity.class);
            startActivity(i);
            finish();
        }
    }

    /*
	 * Controlla se i servizi di localizzazione sono attivati. In caso contrario
	 * compare un popup con l'avviso
	 */
    private void checkLocationProviders() {
        Log.d(TAG, "checkLocationProviders");
        /*
        String provider = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCATION_MODE);

        if (provider != null)
            Log.d(TAG, provider);

        if (provider.equals("")) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(
                    "Non è attivato nessun servizio di localizzazione!\nAttivare i servizi di localizzazione wireless o GPS nelle 'Impostazioni'")
                    .setCancelable(false)
                    .setTitle("Il GPS è spento")
                    .setIcon(R.drawable.ic_launcher)

                    .setPositiveButton("Attiva",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    Intent intent = new Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(intent, 1);
                                }
                            })
                    .setNegativeButton("Non adesso",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    MainActivity.this.finish();
                                }
                            }).show();
        }
        */

        LocationManager lm = null;
        boolean gps_enabled = false,network_enabled = false;
        if(lm==null)
            lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){}
        try{
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){}

        if(!gps_enabled && !network_enabled){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setCancelable(false);
            dialog.setTitle("Il GPS è spento");
            dialog.setIcon(R.drawable.ic_launcher);
            dialog.setMessage("Non è attivato nessun servizio di localizzazione!\n" +
                    "Attivare i servizi di localizzazione wireless o GPS nelle 'Impostazioni'");
            dialog.setPositiveButton("Attiva", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(myIntent, 1);
                    //get gps
                }
            });
            dialog.setNegativeButton("Non adesso", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    MainActivity.this.finish();

                }
            });
            dialog.show();

        }
    }

    void showProgressBar() {

        setSupportProgressBarIndeterminateVisibility(true);
    }

    void hideProgressBar() {
        try {
            setSupportProgressBarIndeterminateVisibility(false);
        } catch (Exception e) {

        }

    }

    private void checkNotificaPush(Intent intent) {
        Log.d(TAG, "checkNotificaPush: " + intent);

        if (intent != null && intent.getExtras() != null
                && intent.getExtras().containsKey("com.parse.Data")) {
            try {
                // String action = intent.getAction();
                // String channel =
                // intent.getExtras().getString("com.parse.Channel");
                JSONObject json = new JSONObject(intent.getExtras().getString(
                        "com.parse.Data"));

                if (json.has("web") && json.has("alert")) {
                    // Messaggio dal portale
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(json.getString("alert"))
                            .setCancelable(true)
                            .setTitle("MagicBus Team")
                            .setIcon(R.drawable.ic_launcher)
                            .setNeutralButton("Ricevuto",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {

                                        }
                                    }).show();
                } else if (json.has("alert")) {

                    String fbId = null;
                    final String nickname = json.getString("nickname");
                    String msg = json.getString("alert");
                    double Lat;
                    double Lng;
                    try{
                        Lat = json.getDouble("latitude");
                        Lng = json.getDouble("longitude");

                        if (json.has("fbId")){
                            fbId = json.getString("fbId");
                        }
                        if (mapFragment!=null && map!=null){
                            mapFragment.addChatMessageMarker(new GeoPoint(Lat, Lng), msg, nickname, fbId);
                        }
                    }
                    catch(Exception e){
                        Lat = getCurrentLocation().getLatitude();
                        Lng = getCurrentLocation().getLongitude();
                    }

                    final double lat = Lat;
                    final double lng = Lng;

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(msg)
                            .setCancelable(true)
                            .setTitle(nickname)
                            .setIcon(R.drawable.ic_launcher)

                            .setPositiveButton("Rispondi",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            MBUtils.showChatDialog(MainActivity.this, nickname, lat, lng);

                                        }
                                    })
                            .setNeutralButton("Chiudi",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {

                                        }
                                    }).show();
                }

				/*
				 * Log.d(TAG, "got action " + action + " on channel " + channel
				 * + " with:"); Iterator itr = json.keys(); while
				 * (itr.hasNext()) { String key = (String) itr.next();
				 * Log.d(TAG, "..." + key + " => " + json.getString(key)); }
				 */
            } catch (JSONException e) {
                Log.d(TAG, "JSONException: " + e.getMessage());
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");

        // Decide what to do based on the original request code
        switch (requestCode) {
            // ...
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
                switch (resultCode) {
                    case RESULT_OK:
				/*
				 * Try the request again
				 */
                        // ...
                        break;
                }
                // ...
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error code
            int errorCode = 0;// connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
            }
            return false;
        }
    }

    private boolean checkWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            return true;
        }
        return false;
    }

    public void goToMBOrari(JsonObject fermata) {
        Log.d(TAG, "goToMBOrari");

        this.fermataSelezionata = fermata;

        Fragment fragment = new MBOrari();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, (Fragment) fragment).commit();

        // update selected item and title, then close the drawer
        // mDrawerList.setItemChecked(position, true);
        setTitle("Orari " + fermata.get("fermata_name").getAsString());
        //mDrawerLayout.closeDrawer(mDrawerList);

        //CURRENT_PAGE = MBORARI;
    }

    //Menu
    public void condividiTwitter() {
        Log.d(TAG, "condividiTwitter");

        // share intent
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent
                .putExtra(
                        Intent.EXTRA_TEXT,
                        "#MagicBus, l'app social per viaggiare sui mezzi pubblici! #Cosenza #Uni #MagnaGraecia #Milano #Trento via @MagicBusApp");
        startActivity(Intent.createChooser(shareIntent, "Condividi"));
    }

    private void lanciaUnTweet() {

        final String tweet = "Sto usando #MagicBus, l'#app #social per viaggiare sui #mezzi #pubblici! In beta su #Cosenza, #Università #MagnaGraecia e #Milano! https://play.google.com/store/apps/details?id=com.magicbusapp.magicbus";

        new AlertDialog.Builder(this).setTitle("Lancia Un tweet")
                .setMessage(tweet).setIcon(R.drawable.ic_launcher)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Tweet", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {

                        new Thread() {
                            public void run() {

                                HttpClient client = new DefaultHttpClient();
                                HttpGet verifyGet = new HttpGet(
                                        "https://api.twitter.com/1/account/verify_credentials.json");
                                ParseTwitterUtils.getTwitter().signRequest(
                                        verifyGet);
                                try {
                                    HttpResponse response2 = client
                                            .execute(verifyGet);
                                    Log.d(TAG, response2.toString());
                                } catch (ClientProtocolException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                } catch (IOException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }

                                HttpPost httppost = new HttpPost(
                                        "https://api.twitter.com/1.1/statuses/update.json");
                                try {
                                    // Add your data
                                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                                            2);
                                    nameValuePairs.add(new BasicNameValuePair(
                                            "status", tweet));

                                    httppost.setEntity(new UrlEncodedFormEntity(
                                            nameValuePairs));
                                    httppost.addHeader("Content-Type",
                                            "application/x-www-form-urlencoded");

                                    ParseTwitterUtils.getTwitter().signRequest(
                                            httppost);

                                    // Execute HTTP Post Request
                                    HttpResponse response = client
                                            .execute(httppost);

                                    HttpEntity resEntity = response.getEntity();
                                    if (resEntity != null) {
                                        Log.i("RESPONSE",
                                                EntityUtils.toString(resEntity));
                                    }
                                    Handler myHandler = new Handler();
                                    myHandler.post(new Runnable() {
                                        public void run() {
                                            // call the activity method that
                                            // updates the UI
                                            MBUtils.showSuccessToast(
                                                    MainActivity.this,
                                                    "Mitico! Tweet inviato :-)");
                                        }
                                    });

                                } catch (ClientProtocolException e) {
                                    // TODO Auto-generated catch block
                                    Log.d(TAG, e.getLocalizedMessage());
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    Log.d(TAG, e.getLocalizedMessage());
                                }
                            }
                        }.start();

                    }
                }).create().show();
    }

    public void condividiFacebook() {
        Log.d(TAG, "condividiFacebook");

        if (ParseFacebookUtils.getSession() != null
                && ParseFacebookUtils.getSession().isOpened()) {

            Bundle postParams = new Bundle();
            postParams
                    .putString("name",
                            "Sto usando MagicBus: l'app social per viaggiare sui mezzi pubblici!");
            postParams
                    .putString("caption",
                            "In beta su Cosenza, Catanzaro (Università Magna Graecia) e Milano!");
            postParams
                    .putString(
                            "description",
                            "Quali sono le fermate più vicine a me? L'autobus è in ritardo o è già passato? Posso aggiungere nuove fermate e nuove linee? Scoprilo con MagicBus!");
            postParams
                    .putString("link",
                            "https://play.google.com/store/apps/details?id=com.magicbusapp.magicbus");
            postParams.putString("picture",
                    "https://lh5.ggpht.com/-QPUrchbGKFBhDqlHd2BCcvTzAlmuysntj5lQvyjbeO8jdwn3avPsDRXxHQMtRUxWcA=w300-rw");

            WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(this,
                    ParseFacebookUtils.getSession(), postParams))
                    .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                        @Override
                        public void onComplete(Bundle values,
                                               FacebookException error) {
                            if (error == null) {
                                // When the story is posted, echo the success
                                // and the post Id.
                                final String postId = values
                                        .getString("post_id");
                                if (postId != null) {
                                    MBUtils.showSuccessToast(MainActivity.this,
                                            "Grazie per la condivisione!");
                                } else {
                                    // User clicked the Cancel button

                                }
                            } else if (error instanceof FacebookOperationCanceledException) {
                                // User clicked the "x" button
                                Toast.makeText(getApplicationContext(),
                                        "Publish cancelled", Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                // Generic, ex: network error
                                Toast.makeText(getApplicationContext(),
                                        "Error posting story",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                    }).build();
            feedDialog.show();
        } else {
            Log.d(TAG, "Facebook session NULL");
            chiediPermessiFacebook();
        }
    }

    private void chiediPermessiFacebook() {
        Log.d(TAG, "chiediPermessiFacebook");

        new AlertDialog.Builder(this)
                .setTitle("Passaparola")
                .setMessage(
                        "Non sei connesso con Facebook.\nVuoi farlo adesso?")
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {

						/*
						 * ParseFacebookUtils.logIn(
						 * Arrays.asList(Permissions.User.EMAIL), MBMain.this,
						 * new LogInCallback() {
						 *
						 * @Override public void done(ParseUser user,
						 * ParseException err) {
						 *
						 * if (user == null) { Log.d(TAG,
						 * "Uh oh. The user cancelled the Facebook login: ");
						 * MBUtils.showCustomToast( MBMain.this,
						 * "Accesso tramite FB interrotto!");
						 * dismissProgressDialog();
						 *
						 * } else if (user.isNew()) { Log.d(TAG,
						 * "User signed up and logged in through Facebook!");
						 *
						 * MBUtils.getFacebookInfoInBackground(); //
						 * updatePreferences(user);
						 *
						 * } else { Log.d(TAG,
						 * "User logged in through Facebook!");
						 *
						 * } } });
						 */

						/*
						 * final ParseUser user = ParseUser.getCurrentUser();
						 *
						 * if (!ParseFacebookUtils.isLinked(user)) {
						 * ParseFacebookUtils.link(user, MBMain.this, new
						 * SaveCallback() {
						 *
						 * @Override public void done(ParseException ex) { if
						 * (ParseFacebookUtils.isLinked(user)) { Log.d("MyApp",
						 * "Woohoo, user logged in with Facebook!");
						 * ParseFacebookUtils.saveLatestSessionData(user);
						 *
						 * MBUtils.showCustomToast(MBMain.this,
						 * "Adesso sei connesso con Facebook"); } } }); }
						 */
                        final ParseUser user = ParseUser.getCurrentUser();
                        if (!ParseFacebookUtils.isLinked(user)) {
                            ParseFacebookUtils.link(user, MainActivity.this,
                                    new SaveCallback() {
                                        @Override
                                        public void done(ParseException ex) {
                                            Log.d(TAG, "done link facebook");
                                            if (ex != null)
                                                Log.d(TAG, ex
                                                        .getLocalizedMessage());

                                            if (ParseFacebookUtils
                                                    .isLinked(user)) {
                                                Log.d("MyApp",
                                                        "Woohoo, user logged in with Facebook!");
                                                MBUtils.getFacebookInfoInBackground();
                                                condividiFacebook();
                                            } else {
                                                Log.d(TAG,
                                                        "User non ancora linkato :(");
                                            }
                                        }
                                    });
                        }
                    }
                }).create().show();

    }
}