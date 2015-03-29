package com.magicbusapp.magicbus;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class MBAuthenticationActivity extends ActionBarActivity {

    private final static String TAG = MBAuthenticationActivity.class.getSimpleName();

    private ProgressDialog progressBar;
    private Button facebookButton, accediButton, iscrivitiButton;
    private TextView facebookTextView;

    //Facebook
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private boolean pendingPublishReauthorization = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_mbauthentication);

        Log.d(TAG, "onCreate");

        getSupportActionBar().hide();

        this.facebookTextView = (TextView)findViewById(R.id.frase_fb);

        this.accediButton = (Button)findViewById(R.id.accedi_button);
        this.accediButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goToLoginActivity();
                    }
                });

        this.facebookButton = (Button)findViewById(R.id.accedi_facebook_button);
        this.facebookButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loginConFacebook();
                    }
                });

        this.iscrivitiButton = (Button)findViewById(R.id.iscriviti_button);
        this.iscrivitiButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goToRegistrationActivity();
                    }
                });
    }

    protected void goToRegistrationActivity() {
        Log.d(TAG, "goToRegistrationActivity");
        Intent i = new Intent(MBAuthenticationActivity.this, MBRegistrationActivity.class);
        startActivity(i);

    }

    protected void loginConFacebook() {
        Log.d(TAG, "loginConFacebook");

        this.facebookTextView.setVisibility(TextView.GONE);
        this.iscrivitiButton.setVisibility(Button.GONE);
        this.facebookButton.setVisibility(Button.GONE);
        this.accediButton.setVisibility(Button.GONE);

        //showProgressDialog("Accesso in corso...");
        MBUtils.showInfoToast(this, "Autorizzo...");

        ParseFacebookUtils.logIn(Arrays.asList(
                        //"basic_info",
                        "public_profile",
                        "user_friends",
                        //"user_about_me",
                        //"user_location",
                        "email"),
                this, 123,
                new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {

                        if (user == null) {
                            Log.d(TAG, "Uh oh. The user cancelled the Facebook login: " + err.getLocalizedMessage());
                            MBUtils.showErrorToast(MBAuthenticationActivity.this, "Accesso tramite FB interrotto!");
                            dismissProgressDialog();

                            facebookTextView.setVisibility(TextView.VISIBLE);
                            iscrivitiButton.setVisibility(Button.VISIBLE);
                            facebookButton.setVisibility(Button.VISIBLE);
                            accediButton.setVisibility(Button.VISIBLE);

                        } else if (user.isNew()) {
                            Log.d(TAG, "User signed up and logged in through Facebook! " + user.toString());

                            MBUtils.showSuccessToast(MBAuthenticationActivity.this, "Mitico! Inizializzazione in corso...");

                            MBUtils.getFacebookInfoInBackground();

                            goToMain();

                        } else {
                            Log.d(TAG, "User logged in through Facebook!");

                            MBUtils.showSuccessToast(MBAuthenticationActivity.this, "Ciao " + user.get("nickname") + "!");

                            goToMain();

                        }
                    }

                });
    }

    private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }

    protected void publishStory() {
        Log.d(TAG, "publishStory");

        Session session = ParseFacebookUtils.getSession();

        if (session != null){

            // Check for publish permissions
            List<String> permissions = session.getPermissions();
            if (!isSubsetOf(PERMISSIONS, permissions)) {
                Log.d(TAG, "Permesso non autorizzato! Richiesta in corso...");
                pendingPublishReauthorization = true;
                Session.NewPermissionsRequest newPermissionsRequest = new Session
                        .NewPermissionsRequest(this, PERMISSIONS);
                session.requestNewPublishPermissions(newPermissionsRequest);

            }

            Bundle postParams = new Bundle();
            postParams.putString("name", "Sto usando MagicBus: la community on the road della mia città.");
            postParams.putString("caption", "Disponibile per Android in beta per Cosenza: la prossima città potrebbe essere la tua!");
            postParams.putString("description", "Quali sono le fermate più vicine a me? L'autobus è in ritardo o è già passato? Posso aggiungere nuove fermate e nuove linee? Scoprilo con MagicBus!");
            postParams.putString("link", "https://play.google.com/store/apps/details?id=com.magicbusapp.magicbus");
            postParams.putString("picture", "https://lh5.ggpht.com/0l5Aeu1O3uFoi-jgENa2C551W-XSqRXW3qB9YYbfdTyjB7Ch_B5LcmAw3Vxo_4FaZUE=w124");

            Request.Callback callback= new Request.Callback() {
                public void onCompleted(Response response) {
                    JSONObject graphResponse = response
                            .getGraphObject()
                            .getInnerJSONObject();
                    String postId = null;
                    try {
                        postId = graphResponse.getString("id");
                    } catch (JSONException e) {
                        Log.i(TAG,
                                "JSON error "+ e.getMessage());
                    }
                    FacebookRequestError error = response.getError();

                    if (error != null) {
                        Log.d(TAG, "Facebook: " + error.getErrorMessage() );
                    } else {
                        Log.d(TAG, "Facebook: non error" );
                    }

                }
            };
            Log.d(TAG, "Inoltro la richiesta...");
            Request request = new Request(session, "me/feed", postParams,
                    HttpMethod.POST, callback);

            RequestAsyncTask task = new RequestAsyncTask(request);
            task.execute();
            MBUtils.showSuccessToast(this, "Grazie per il passaparola :-)");
        }

    }

    private void checkUserAcceptsPublishFB() {
        Log.d(TAG, "userAcceptsPublishFB");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Fai passaparola con i tuoi amici di Facebook.\n Insieme migliorerete MagicBus!")
                .setCancelable(false)
                .setTitle("Benvenuto nella Community!")
                .setPositiveButton("Condividi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        publishStory();

                    }
                })
                .setNeutralButton("No, adesso non mi va", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        goToMain();
                    }
                }).show();

    }

    protected void goToMain() {
        Log.d(TAG, "goToMain");
        //configureServerSettings();
        Intent i = new Intent(MBAuthenticationActivity.this, MainActivity.class);
        startActivity(i);
        dismissProgressDialog();
        finish();
    }

    /*
    private void configureServerSettings() {
        Log.d(TAG, "configureServerSettings");

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Parametri");

        query.getInBackground("cuX0LWw9M3", new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject parametri, ParseException e) {
                if (e == null) {
                    String openfire_server_address = parametri.getString("openfire_server_address");
                    String roomname = parametri.getString("roomname");
                    MBApplication.setOpenfireServerAddress(openfire_server_address);
                    MBApplication.setRoomname(roomname);
                    Log.d(TAG, "Openfire server address:" + openfire_server_address);
                } else {
                    // something went wrong
                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
        });

    }
    */

    protected void goToLoginActivity() {
        Log.d(TAG, "goToLoginActivity");
        Intent i = new Intent(MBAuthenticationActivity.this, MBAccediActivity.class);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MBUtils.showInfoToast(this, "Autentico...");
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, "onStart");

		if(((MBApplication)getApplication()).showWizard()){
			Log.d(TAG, "Go to wizard activity");
			Intent i = new Intent(MBAuthenticationActivity.this, MBWizardActivity.class);
			startActivity(i);
			finish();
		}

    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");
        ckeckCurrentUser();
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void ckeckCurrentUser(){
        Log.d(TAG, "ckeckCurrentUser");

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            Log.d(TAG, "user recognized!");
            goToMain();
        } else {
            // show the signup or login screen
            Log.d(TAG, "user NOT recognized! Go to login activity...");
        }
    }

    private void showProgressDialog(String message){
        Log.d(TAG, "showProgressDialog");

        if(progressBar == null)
            progressBar = new ProgressDialog(MBAuthenticationActivity.this);

        progressBar.setMessage(message);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setOnDismissListener(new DialogInterface.OnDismissListener(){

            @Override
            public void onDismiss(DialogInterface obj) {
                Log.d(TAG, "onDismissProgressDialog");

            }

        });
        progressBar.show();
    }

    private void dismissProgressDialog(){
        Log.d(TAG, "dismissProgressDialog");

        if(progressBar != null)
            progressBar.dismiss();
    }
}
