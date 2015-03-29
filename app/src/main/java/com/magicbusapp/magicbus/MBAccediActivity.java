package com.magicbusapp.magicbus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;


public class MBAccediActivity extends ActionBarActivity {

    private final static String TAG = MBAccediActivity.class.getSimpleName();

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    // Values for email and password at the time of the login attempt.
    private String mEmail;
    private String mPassword;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    // Facebook
    private static final List<String> PERMISSIONS = Arrays
            .asList("publish_actions");
    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private boolean pendingPublishReauthorization = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_mblogin);
        setUpActionBar();
        Log.d(TAG, "onCreate");

        // Set up the login form.
        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setText(mEmail);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id,
                                                  KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            try {
                                attemptLogin();
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                Log.d(TAG, e.getLocalizedMessage());
                            }
                            return true;
                        }
                        return false;
                    }
                });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            attemptLogin();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            Log.d(TAG, e.getLocalizedMessage());
                        }
                    }
                });

        findViewById(R.id.recupera_password_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goToRecuperaPassword();
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_mblogin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_forgot_password: {
                goToRecuperaPassword();
                return true;
            }
            case R.id.menu_nuovo_account: {
                goToRegistrationActivity();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // ckeckCurrentUser();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        //EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        //EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");
    }

    private void setUpActionBar() {
        getSupportActionBar().hide();
    }

    private void goToRegistrationActivity() {
        Intent i = new Intent(MBAccediActivity.this,
                MBRegistrationActivity.class);
        startActivity(i);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     *
     * @throws InterruptedException
     */
    public void attemptLogin() throws InterruptedException {
        Log.d(TAG, "attempLogin");

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();

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
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            doLogin();
        }
    }

    private void doLogin() {
        Log.d(TAG, "doLogin");

        ParseUser.logInInBackground(mEmail, mPassword, new LogInCallback() {

            public void done(ParseUser user, ParseException e) {

                if (user != null) {
                    Log.d(TAG, "login success! Go to MapActivity...");
                    MBUtils.showSuccessToast(MBAccediActivity.this,
                            "Accesso eseguito con successo!");

                    goToMain();

                } else {
                    // Signup failed. Look at the ParseException to see what
                    // happened.
                    Log.d(TAG, "login failed: " + e.getLocalizedMessage());
                    Log.w(TAG, e);
                    MBUtils.showErrorToast(MBAccediActivity.this,
                            e.getLocalizedMessage());
                    showProgress(false);
                }
            }
        });
    }

    /*
     * public void printHashKey() {
     *
     * try { PackageInfo info =
     * getPackageManager().getPackageInfo("com.magicbusapp.magicbus",
     * PackageManager.GET_SIGNATURES); for (Signature signature :
     * info.signatures) { MessageDigest md = MessageDigest.getInstance("SHA");
     * md.update(signature.toByteArray()); Log.d("TEMPTAGHASH KEY:",
     * Base64.encodeToString(md.digest(), Base64.DEFAULT)); } } catch
     * (NameNotFoundException e) {
     *
     * } catch (NoSuchAlgorithmException e) {
     *
     * }
     *
     * }
     */

    /*
    private static void configureServerSettings() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Parametri");

        query.getInBackground("cuX0LWw9M3", new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject parametri, ParseException e) {
                if (e == null) {
                    String openfire_server_address = parametri
                            .getString("openfire_server_address");
                    String roomname = parametri.getString("roomname");
                    MBApplication
                            .setOpenfireServerAddress(openfire_server_address);
                    MBApplication.setRoomname(roomname);
                    Log.d(TAG, "Openfire server address:"
                            + openfire_server_address);
                } else {
                    // something went wrong
                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
        });
    }
    */

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
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

    private void goToMain() {
        Log.d(TAG, "goToMain");
        //configureServerSettings();
        Intent i = new Intent(MBAccediActivity.this, MainActivity.class);
        startActivity(i);

        showProgress(false);
        finish();
    }

    private void goToRecuperaPassword() {
        Log.d(TAG, "goToRecuperaPassword");
        Intent i = new Intent(MBAccediActivity.this,
                MBRecuperaPasswordActivity.class);
        startActivity(i);
    }

    private void ckeckCurrentUser() {
        Log.d(TAG, "ckeckCurrentUser");

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            Log.d(TAG, "user recognized!");
            // updatePreferences(currentUser);
            goToMain();
            finish();
        } else {
            // show the signup or login screen
            Log.d(TAG, "user NOT recognized! Go to login activity...");
        }
    }

}
