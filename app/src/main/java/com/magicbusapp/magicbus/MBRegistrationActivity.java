package com.magicbusapp.magicbus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Arrays;


public class MBRegistrationActivity extends ActionBarActivity {

    private final static String TAG = MBRegistrationActivity.class.getSimpleName();

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    // Values for nickname, email and password at the time of the registration attempt.
    private String mNickname;
    private String mEmail;
    private String mPassword;

    // UI references.
    private EditText mNicknameView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    //@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_mbregistration);

        Log.d(TAG, "onCreate");

        // Show the Up button in the action bar.
        setUpActionBar();

        // Set up the registration form.
        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setText(mEmail);

        mNicknameView = (EditText) findViewById(R.id.nickname);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id,
                                          KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    try {
                        attemptRegistration();
                    } catch (InterruptedException e) {
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.registration_form);
        mLoginStatusView = findViewById(R.id.registration_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.registration_status_message);

        findViewById(R.id.register_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            attemptRegistration();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });

        findViewById(R.id.accedi_facebook_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loginConFacebook();
                    }
                });

        findViewById(R.id.already_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, "onStart");
        //EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop");
        //EasyTracker.getInstance(this).activityStop(this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setUpActionBar() {
        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_USE_LOGO|ActionBar.DISPLAY_SHOW_HOME);
            //actionBar.setCustomView(R.layout.mb_actionbar);
            actionBar.setLogo(R.drawable.ic_logotype_small);
            actionBar.setDisplayUseLogoEnabled(true);

        }
    }

    private void attemptRegistration() throws InterruptedException{
        Log.d(TAG, "attemptRegistration");

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mNicknameView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();
        mNickname = mNicknameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid nickname.
        if (TextUtils.isEmpty(mNickname)) {
            mNicknameView.setError(getString(R.string.error_field_required));
            focusView = mNicknameView;
            cancel = true;
        } else if (mNickname.length() < 4) {
            mNicknameView.setError(getString(R.string.error_invalid_nickname));
            focusView = mNicknameView;
            cancel = true;
        }


        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_registration);
            showProgress(true);
            doRegistration();
        }
    }

    private void doRegistration(){
        Log.d(TAG, "doRegistration");

        final ParseUser user = new ParseUser();
        user.setUsername(this.mEmail);
        user.setPassword(this.mPassword);
        user.setEmail(this.mEmail);

        // Campo extra, da usare come nickname per la chat
        user.put("nickname", this.mNickname);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {

                if (e == null) {
                    // Hooray! Let them use the app now.
                    Log.d(TAG, "Registration ok!");
                    MBApplication.updatePreferences(user, false);
                    goToMain();
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong

                    showProgress(false);
                    Log.d(TAG, e.getLocalizedMessage());
                    MBUtils.showErrorToast(MBRegistrationActivity.this, e.getLocalizedMessage());
                }
            }
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        }

        else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void goToMain(){
        Log.d(TAG, "goToMap");
        //configureServerSettings();
        Intent i = new Intent(MBRegistrationActivity.this, MainActivity.class);
        startActivity(i);
        finish();
        showProgress(false);
    }
/*
    private static void configureServerSettings(){
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

    protected void showToast(String msg){
        Log.d(TAG, "showToast");

        Context context = this;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, msg, duration); toast.show();
    }

    protected void loginConFacebook() {
        Log.d(TAG, "loginConFacebook");

        MBUtils.showInfoToast(this, "Accesso tramite Facebook in corso...Attendi qualche secondo :-)");

        ParseFacebookUtils.logIn(Arrays.asList(
                        "public_profile",
                        "user_friends",
                        //"basic_info",
                        //"user_about_me",
                        //"user_location",
                        "email"),
                this, 123,
                new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {

                        if (user == null) {
                            Log.d(TAG, "Uh oh. The user cancelled the Facebook login: ");
                            MBUtils.showErrorToast(MBRegistrationActivity.this, "Accesso tramite FB interrotto!");

                        } else if (user.isNew()) {
                            Log.d(TAG, "User signed up and logged in through Facebook! " + user.toString());

                            MBUtils.showSuccessToast(MBRegistrationActivity.this, "Mitico! Inizializzazione in corso...");

                            MBUtils.getFacebookInfoInBackground();

                            goToMain();

                        } else {
                            Log.d(TAG, "User logged in through Facebook!");

                            MBUtils.showSuccessToast(MBRegistrationActivity.this, "Ben tornato " + user.get("nickname") + "!");

                            goToMain();

                        }
                    }

                });
    }
}
