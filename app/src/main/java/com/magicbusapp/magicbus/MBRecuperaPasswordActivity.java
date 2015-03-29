package com.magicbusapp.magicbus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;


public class MBRecuperaPasswordActivity extends ActionBarActivity {

    private final static String TAG = MBRecuperaPasswordActivity.class.getSimpleName();

    private String mEmail;

    // UI references.
    private EditText mEmailView;
    private View mRecPasswordFormView;
    private View mRecPasswordStatusView;
    private TextView mRecPasswordStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_mbrecupera_password);

        setUpActionBar();

        mEmailView = (EditText) findViewById(R.id.recupero_password_email);

        mRecPasswordFormView = findViewById(R.id.recupero_password_form);
        mRecPasswordStatusView = findViewById(R.id.recupera_password_status);
        mRecPasswordStatusMessageView = (TextView) findViewById(R.id.recupero_password_status_message);

        findViewById(R.id.recupero_fermata_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptRecuperaPassword();
                    }
                });
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mbrecupera_password, menu);
        return true;
    }
    */

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
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
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

    private void attemptRecuperaPassword(){

        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

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
            mRecPasswordStatusMessageView.setText(R.string.recupero_password_progress_signing_in);
            showProgress(true);
            recuperaPassword();
        }
    }

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

            mRecPasswordStatusView.setVisibility(View.VISIBLE);
            mRecPasswordStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mRecPasswordStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mRecPasswordFormView.setVisibility(View.VISIBLE);
            mRecPasswordFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mRecPasswordFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        }

        else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mRecPasswordStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRecPasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void recuperaPassword(){
        Log.d(TAG, "recuperaPassword");

        ParseUser.requestPasswordResetInBackground(mEmail,
                new RequestPasswordResetCallback() {
                    public void done(ParseException e) {

                        showProgress(false);

                        if (e == null) {
                            // An email was successfully sent with reset instructions.
                            Log.d(TAG, "recuperaPassword OK!");
                            showToast("Ti �� appena stata inviata un'email con un link per ripristinare la password!");
                            goToLoginActivity();

                        } else {
                            // Something went wrong. Look at the ParseException to see what's up.
                            Log.e(TAG, e.getLocalizedMessage());
                            showToast(e.getLocalizedMessage());
                        }
                    }
                });

    }

    protected void goToLoginActivity(){
        Log.d(TAG, "goToLoginActivity");
        NavUtils.navigateUpFromSameTask(this);
        finish();
    }

    protected void showToast(String msg){
        Log.d(TAG, "showToast");

        Context context = this;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, msg, duration); toast.show();
    }
}
