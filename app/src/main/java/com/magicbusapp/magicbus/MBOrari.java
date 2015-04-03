package com.magicbusapp.magicbus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ShareActionProvider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceJsonTable;
import com.microsoft.windowsazure.mobileservices.table.TableJsonQueryCallback;

import java.net.MalformedURLException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MBOrari.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MBOrari#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MBOrari extends Fragment {

    private static final String TAG = MBOrari.class.getSimpleName();

    private MobileServiceClient mClient;
    private MobileServiceJsonTable mOrarioTable;

    private OrariAdapter mAdapter;

    private ListView listViewOrari;

    private ShareActionProvider mShareActionProvider;

    private JsonObject fermata;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private OnFragmentInteractionListener mListener;

    public static MBOrari newInstance(int sectionNumber) {
        MBOrari fragment = new MBOrari();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MBOrari() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mClient = new MobileServiceClient(
                    "https://magicbusapp.azure-mobile.net/",
                    "xSjGpQNbfAjsytZcwQLJxeIofQTsYu87", getActivity());

            mOrarioTable = mClient.getTable("Orario");

        } catch (MalformedURLException e) {
            MBUtils.showErrorToast(getActivity(), e.getLocalizedMessage());
        }

        //setHasOptionsMenu(true);
        this.fermata = ((MainActivity)getActivity()).fermataSelezionata;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu");

        inflater.inflate(R.menu.a_mborari, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        if(this.fermata!=null){
            Intent intent = newShareIntent("Gli orari della fermata #bus '" + this.fermata.get("fermata_name") + "' su MagicBus!",
                    "http://urlin.it/4f67e");
            if(intent != null)
                this.mShareActionProvider.setShareIntent(intent);
            this.mShareActionProvider.setShareHistoryFileName(null);
        }
    }

    public Intent newShareIntent(String message, String link) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message + " " +link);

        return intent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        ((MainActivity)getActivity()).showProgressBar();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.f_mborari, container,
                false);

		/*
		orariParseQueryAdapter = new OrariParseQueryAdapter(getActivity());
		orariParseQueryAdapter.setSherlockFragment(this);
		orariParseQueryAdapter.setPaginationEnabled(false);
		orariParseQueryAdapter.setPlaceholder(getResources().getDrawable(R.drawable.subtle_pattern_7_placeholder));
		orariParseQueryAdapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>(){

			@Override
			public void onLoaded(List<ParseObject> objects, Exception e) {

				((MBMain)getActivity()).hideProgressBar();

				if(objects.isEmpty()){
					MBUtils.showWarningToast(getActivity(), "Per questa fermata gli orari non sono ancora disponibili :(");
					((MBMain)getSherlockActivity()).goToMBListaFermate(MBMain.MBLISTAFERMATE);
				}
			}

			@Override
			public void onLoading() {
			}

		});
		*/
        mAdapter = new OrariAdapter(getActivity(), R.layout.f_mborari);
        listViewOrari = (ListView) view.findViewById(R.id.orari_list);
        listViewOrari.setAdapter(mAdapter);

        // Load the items from the Mobile Service
        refreshItemsFromTable();

        //listViewOrari.setAdapter(orariParseQueryAdapter);
		/*listViewOrari.setAdapter(new BaseAdapter() {

			@Override
			public int getCount() {
				return orariList.size();
			}

			@Override
			public Object getItem(int position) {
				return orariList.get(position);
			}

			@Override
			public long getItemId(int arg0) {
				  Auto-generated method stub
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ParseObject orarioPO = orariList.get(position);

				View vi = convertView;

				if (convertView == null) {
					vi = getActivity().getLayoutInflater().inflate(
							R.layout.row_mb_orario, null);
				}

				TextView oraTextView = (TextView) vi
						.findViewById(R.id.ora);
				TextView trattaTextView = (TextView) vi
						.findViewById(R.id.tratta);


				try {
					oraTextView.setText(orarioPO.getString("Ora"));
					trattaTextView.setText(orarioPO.getParseObject("Tratta").getString("Descrizione"));
				} catch (Exception e) {
					// Auto-generated catch block
					Log.d(TAG, "Exception");
				}

				return vi;
			}

		});*/

        return view;
    }

    public JsonObject getFermata() {
        return fermata;
    }

    public void setFermata(JsonObject fermata) {
        this.fermata = fermata;
    }

    /**
     * Refresh the list with the items in the Mobile Service Table
     */
    private void refreshItemsFromTable() {

        String filter = fermata.get("id").getAsInt() + "";
        //MobileServiceQuery query = new MobileServiceQuery();
        //query.parameter("f", filter);

        mOrarioTable.execute(mOrarioTable.parameter("fermata_id_all", filter), new TableJsonQueryCallback() {


            @Override
            public void onCompleted(JsonElement orariJson, Exception e, ServiceFilterResponse arg3) {
                ((MainActivity) getActivity()).hideProgressBar();

                if (e != null) {
                    Log.d("exc", e.getLocalizedMessage());
                    MBUtils.showErrorToast(getActivity(), e.getLocalizedMessage());
                } else {

                    if (orariJson.isJsonArray()) {
                        JsonArray array = orariJson.getAsJsonArray();

                        Log.d("fermata", array.toString());

                        mAdapter.clear();
                        for (JsonElement orario : array) {

                            JsonObject o = orario.getAsJsonObject();
                            //Log.d("fermata", o.get("departure").getAsString() + " - " + o.get("tratta_short_name"));
                            mAdapter.add(o);

                        }

                    }
                }
            }
        });

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
        /*
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
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

}
