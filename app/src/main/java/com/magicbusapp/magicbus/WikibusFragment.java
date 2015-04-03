package com.magicbusapp.magicbus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceJsonTable;
import com.microsoft.windowsazure.mobileservices.table.TableJsonOperationCallback;
import com.parse.ParseUser;

import java.net.MalformedURLException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WikibusFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WikibusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WikibusFragment extends Fragment {
    private static final String TAG = WikibusFragment.class.getSimpleName();

    private static final String ARG_SECTION_NUMBER = "section_number";

    private OnFragmentInteractionListener mListener;

    private View mNuovaFermataFormView;
    private View mSalvataggioFermataStatusView;
    private TextView mSalvataggioFermataStatusMessageView;
    private TextView popupWikiTextView;
    private Button addFermataButton;
    private Button shareButton;
    private TextView infoWikiTextView;

    private String mNomeFermata;
    private EditText nomeFermataEditText;

    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String PROVIDER = "provider";



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sectionNumber Parameter 1.
     * @return A new instance of fragment WikibusFragment.
     */
    public static WikibusFragment newInstance(int sectionNumber) {
        WikibusFragment fragment = new WikibusFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public WikibusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //todo recupera location fermate
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wikibus, container, false);

        this.shareButton = (Button) view.findViewById(R.id.share_button);
        /*
        this.shareButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ParseFacebookUtils.getSession() != null
						&& ParseFacebookUtils.getSession().isOpened()) {

					Bundle postParams = new Bundle();
					postParams.putString(
							"name",
							"Ho aggiunto la fermata #bus '"
									+ mNomeFermata
									+ "' usando MagicBus! http://urlin.it/4f67e");
					postParams.putString("caption",
							"L'app che non ti lascia mai a piedi");
					postParams.putString("description",
							"Informazioni realtime sul trasporto pubblico!");
					postParams
							.putString("link",
									"https://play.google.com/store/apps/details?id=com.magicbusapp.magicbus");
					postParams.putString("picture",
							"http://www.magicbusapp.it/img/logo_bus_new.png");

					WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
							getActivity(), ParseFacebookUtils.getSession(),
							postParams)).setOnCompleteListener(
							new OnCompleteListener() {

								@Override
								public void onComplete(Bundle values,
										FacebookException error) {
									if (error == null) {
										// When the story is posted, echo the
										// success
										// and the post Id.
										final String postId = values
												.getString("post_id");
										if (postId != null) {
											MBUtils.showSuccessToast(
													getActivity(),
													"Grazie per la condivisione!");
										} else {
											// User clicked the Cancel button

										}
									} else if (error instanceof FacebookOperationCanceledException) {
										// User clicked the "x" button
										Toast.makeText(getActivity(),
												"Publish cancelled",
												Toast.LENGTH_SHORT).show();
									} else {
										// Generic, ex: network error
										Toast.makeText(getActivity(),
												"Error posting story",
												Toast.LENGTH_SHORT).show();
									}
								}

							}).build();
					feedDialog.show();
				} else {
					// share intent
					Intent shareIntent = new Intent();
					shareIntent.setAction(Intent.ACTION_SEND);
					shareIntent.setType("text/plain");
					shareIntent
							.putExtra(
									Intent.EXTRA_TEXT,
									"Ho aggiunto la fermata #bus '"
											+ mNomeFermata
											+ "' usando #MagicBus! #Cosenza, #Università #MagnaGraecia e #Milano http://urlin.it/4f67e");
					startActivity(Intent
							.createChooser(shareIntent, "Condividi"));
				}
			}

		});
         */

        this.infoWikiTextView = (TextView) view.findViewById(R.id.info_wikibus);
        this.nomeFermataEditText = (EditText) view
                .findViewById(R.id.nome_fermata);
        this.addFermataButton = (Button) view
                .findViewById(R.id.add_fermata_button);
        this.addFermataButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                attemptSalvaFermata();
            }
        });

        mSalvataggioFermataStatusView = view
                .findViewById(R.id.salvataggio_fermata_status);
        mSalvataggioFermataStatusMessageView = (TextView) view
                .findViewById(R.id.salvataggio_fermata_status_message);
        mNuovaFermataFormView = view.findViewById(R.id.nuova_fermata_form);
        popupWikiTextView = (TextView) view.findViewById(R.id.popupwiki);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void attemptSalvaFermata() {
        Log.d(TAG, "attemptSalvaFermata");

        // Reset errors.
        nomeFermataEditText.setError(null);

        mNomeFermata = nomeFermataEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid nomeFermata.
        if (TextUtils.isEmpty(mNomeFermata)) {
            nomeFermataEditText
                    .setError(getString(R.string.error_field_required));
            focusView = nomeFermataEditText;
            cancel = true;
        } else if (mNomeFermata.length() < 4) {
            nomeFermataEditText.setError(getString(R.string.error_invalid));
            focusView = nomeFermataEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mSalvataggioFermataStatusMessageView
                    .setText(R.string.salvataggio_fermata_progress);
            showProgress(true);
            try {
                insertFermata();
            } catch (MalformedURLException e) {
                MBUtils.showErrorToast(getActivity(), e.getLocalizedMessage());
            }
        }
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mSalvataggioFermataStatusView.setVisibility(View.VISIBLE);
            mSalvataggioFermataStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mSalvataggioFermataStatusView
                                    .setVisibility(show ? View.VISIBLE
                                            : View.GONE);
                        }
                    });

            mNuovaFermataFormView.setVisibility(View.VISIBLE);
            mNuovaFermataFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mNuovaFermataFormView
                                    .setVisibility(show ? View.GONE
                                            : View.VISIBLE);
                        }
                    });
        }

        else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mSalvataggioFermataStatusView.setVisibility(show ? View.VISIBLE
                    : View.GONE);
            mNuovaFermataFormView
                    .setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void insertFermata() throws MalformedURLException {
        //TODO insert fermata
        Log.d(TAG, "insertFermata");

        ((MainActivity) getActivity()).showProgressBar();

        MobileServiceClient mClient = new MobileServiceClient(
                "https://magicbusapp.azure-mobile.net/",
                "xSjGpQNbfAjsytZcwQLJxeIofQTsYu87", getActivity());

        MobileServiceJsonTable fermataTable = mClient.getTable("Fermata");

        JsonObject fermata = new JsonObject();
        fermata.addProperty("fermata_name", this.mNomeFermata);
        fermata.addProperty("fermata_lat", ((MainActivity)getActivity()).getCurrentLocation().getLatitude());
        fermata.addProperty("fermata_lon", ((MainActivity)getActivity()).getCurrentLocation().getLongitude());
        fermata.addProperty("fermata_validata", false);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null)
            fermata.addProperty("userObjectId", currentUser.getObjectId());

        fermataTable.insert(fermata, new TableJsonOperationCallback(){

            @Override
            public void onCompleted(JsonObject fermataObject, Exception exception,
                                    ServiceFilterResponse arg2) {

                showProgress(false);
                ((MainActivity) getActivity()).hideProgressBar();

                if(exception == null){
                    Log.d(TAG, "Fermata salvata con successo! id: " + fermataObject.get("id").getAsInt());

                    popupWikiTextView
                            .setText("Grazie! Dopo i controlli la fermata comparirà sulla mappa.");
                    shareButton.setVisibility(View.VISIBLE);
                    addFermataButton.setVisibility(View.GONE);
                    nomeFermataEditText.setVisibility(View.GONE);
                    infoWikiTextView.setVisibility(View.GONE);

                    MBUtils.showSuccessToast(getActivity(), "Grazie! Dopo i controlli la fermata comparirà sulla mappa.");

                } else {
                    MBUtils.showErrorToast(getActivity(), exception.getLocalizedMessage());
                }

            }

        });

    }
}
