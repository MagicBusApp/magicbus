package com.magicbusapp.magicbus;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceJsonTable;
import com.microsoft.windowsazure.mobileservices.table.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.table.TableJsonQueryCallback;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.ParseUser;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DirectedLocationOverlay;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;

import static com.magicbusapp.magicbus.MainActivity.fermteList;
import static com.magicbusapp.magicbus.MainActivity.map;


public class MapFragment extends Fragment implements MapEventsReceiver {
    private static final String TAG = MapFragment.class.getSimpleName();

    private static final String ARG_SECTION_NUMBER = "section_number";

    final static int ZOOM = 16;

    private OnFragmentInteractionListener mListener;

    private MobileServiceClient mClient;
    HashMap<String, JsonObject> markers = new HashMap<String, JsonObject>();
    private MainActivity mainActivity;

    protected DirectedLocationOverlay myLocationOverlay;
    private RadiusMarkerClusterer poiMarkers;
    private MapEventsOverlay mapEventsOverlay;

    public static MapFragment newInstance(int sectionNumber) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        try {
            mClient = new MobileServiceClient(
                    "https://magicbusapp.azure-mobile.net/",
                    "xSjGpQNbfAjsytZcwQLJxeIofQTsYu87", getActivity());//.withFilter(new ProgressFilter());
        } catch (MalformedURLException e) {
            MBUtils.showErrorToast(getActivity(), e.getLocalizedMessage());
            //createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        map = (MapView) view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        mapEventsOverlay = new MapEventsOverlay(getActivity(), this);
        map.getOverlays().add(0, mapEventsOverlay);

        myLocationOverlay = new DirectedLocationOverlay(getActivity());
        map.getOverlays().add(myLocationOverlay);

        IMapController mapController = map.getController();

        if (savedInstanceState == null){
            Location location = mainActivity.getCurrentLocation();
            updateMapLocation(location);
            mapController.setZoom(ZOOM);
        } else {
            myLocationOverlay.setLocation((GeoPoint)savedInstanceState.getParcelable(MainActivity.LOCATION_KEY));
            //TODO: restore other aspects of myLocationOverlay...
        }

        if (savedInstanceState != null){
            myLocationOverlay.setLocation((GeoPoint)savedInstanceState.getParcelable("location"));
        }

        map.invalidate();

        poiMarkers = new RadiusMarkerClusterer(getActivity());
        Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster);
        Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
        poiMarkers.setIcon(clusterIcon);
        map.getOverlays().add(poiMarkers);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");

        if (mainActivity != null && mainActivity.getCurrentLocation() != null){
            IGeoPoint myPosition = new GeoPoint(mainActivity.getCurrentLocation().getLatitude(), mainActivity.getCurrentLocation().getLongitude());
            IMapController mapController = map.getController();

            mapController.setZoom(mainActivity.getZoomLevel());
            mapController.setCenter(mainActivity.getMapCenter());

            updateMapLocation(mainActivity.getCurrentLocation());
            /*
            if (fermteList == null)
                downloadAndShowFermate(mainActivity.getCurrentLocation());
            else {
                for (JsonObject fermata : fermteList) {
                    aggiungiFermataSullaMappa(fermata);
                }
            }
            */
        }
        //map.invalidate();
        //poiMarkers.invalidate();


        if (savedInstanceState != null) {
            //Restore the fragment's state here
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");

        outState.putParcelable(MainActivity.LOCATION_KEY, myLocationOverlay.getLocation());
        
        IGeoPoint center = map.getMapCenter();
        outState.putDouble("latitude", center.getLatitude());
        outState.putDouble("longitude", center.getLongitude());
        outState.putInt("zoom", map.getZoomLevel());

    }

    public void updateMapLocation(Location location){
        Log.i(TAG, "updateMapLocation");

        if (location != null) {
            GeoPoint newLocation = new GeoPoint(location);

            if (!myLocationOverlay.isEnabled()){
                //we get the location for the first time:
                myLocationOverlay.setEnabled(true);
                map.getController().setCenter(newLocation);
            }

            GeoPoint prevLocation = myLocationOverlay.getLocation();
            myLocationOverlay.setLocation(newLocation);
            myLocationOverlay.setAccuracy((int)location.getAccuracy());

            if (fermteList == null) {
                mainActivity.showProgressDialog("Carico...");
                downloadAndShowFermate(location);
                mainActivity.dismissProgressDialog();
            }
            else {
                mainActivity.showProgressDialog("Carico...");
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        for (JsonObject fermata : fermteList) {
                            aggiungiFermataSullaMappa(fermata);
                        }

                        //map.invalidate();
                        poiMarkers.invalidate();

                        mainActivity.dismissProgressDialog();
                    }
                };
                new Thread(runnable).start();

            }



        } else {
            //no location known: hide myLocationOverlay
            myLocationOverlay.setEnabled(false);
        }
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

        mainActivity = (MainActivity)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            //case R.id.action_refresh:
              //  Log.d(TAG, "refresh fermate");
                //map.getOverlays().clear();
                //downloadAndShowFermate();

                //return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //TODO
    public void addChatMessageMarker(final GeoPoint geoPoint, String chatMessage, String nicknameSender, String facebookId) {
        Log.i(TAG, "addChatMessageMarker: " + nicknameSender + ", " + chatMessage + ", " + geoPoint);

        //addChatMessageMarker(new GeoPoint(Lat, Lng), msg, nickname, fbId);

        final Marker startMarker = new Marker(map);
        startMarker.setPosition(geoPoint);
        startMarker.setTitle(nicknameSender);
        startMarker.setSnippet(chatMessage);
        startMarker.setIcon(getResources().getDrawable(R.mipmap.ic_chatmessage));
        //TODO foto sender
        if (facebookId != null){
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true).cacheOnDisc(true)
                    .displayer(new RoundedBitmapDisplayer(5)).build();
            ImageLoader.getInstance().loadImage(MBUtils.get_avatar_from_service(1, facebookId,
                    48), new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    Log.i(TAG, "onLoadingComplete");
                    startMarker.setImage(new BitmapDrawable(bitmap));//.setIcon(getResources().getDrawable(R.mipmap.magicuser_default));

                    startMarker.showInfoWindow();

                    map.getController().setCenter(geoPoint);
                    map.invalidate();

                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }
        else{
            startMarker.setImage(getResources().getDrawable(R.drawable.fry));
        }



        startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                marker.showInfoWindow();
                return true;
            }
        });
        //markers.put(startMarker.toString(), fermata);

        startMarker.showInfoWindow();

        map.getController().setCenter(geoPoint);

        map.getOverlays().add(startMarker);
        map.invalidate();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {

        MBUtils.showInfoToast(getActivity(),
                "Tieni premuto sul marker per spostarlo nella posizione corretta!");

        Marker markerFermata = new Marker(map);
        markerFermata.setPosition(geoPoint);
        markerFermata.setTitle("Nuova fermata");
        markerFermata.setSnippet("Tieni premuto sul marker per spostarlo nella posizione corretta!");
        markerFermata.setIcon(getResources().getDrawable(R.mipmap.ic_bus_marker));

        markerFermata.setDraggable(true);
        markerFermata.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {

                //MBUtils.showInfoToast(getActivity(),
                  //      "Rilascia il marker nel punto esatto della fermata...");
            }

            @Override
            public void onMarkerDragEnd(final Marker marker) {
                Log.d(TAG, "onMarkerDragEnd: " + marker.getPosition());

                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature((int) Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_wikibus_view);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface arg0) {
                        marker.remove(map);

                    }

                });

                dialog.findViewById(R.id.add_fermata_button)
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                String nomeFermata = ((EditText) dialog
                                        .findViewById(R.id.nome_fermata))
                                        .getText().toString();
                                if (!TextUtils.isEmpty(nomeFermata)) {

                                    mainActivity.showProgressBar();

                                    MobileServiceJsonTable fermataTable = mClient
                                            .getTable("Fermata");

                                    JsonObject fermata = new JsonObject();
                                    fermata.addProperty("fermata_name",
                                            nomeFermata);
                                    fermata.addProperty("fermata_lat",
                                            marker.getPosition().getLatitude());
                                    fermata.addProperty("fermata_lon",
                                            marker.getPosition().getLongitude());
                                    fermata.addProperty("fermata_validata",
                                            false);

                                    ParseUser currentUser = ParseUser
                                            .getCurrentUser();
                                    if (currentUser != null)
                                        fermata.addProperty("userObjectId",
                                                currentUser.getObjectId());

                                    fermataTable.insert(fermata,
                                            new TableJsonOperationCallback() {

                                                @Override
                                                public void onCompleted(
                                                        JsonObject fermataObject,
                                                        Exception exception,
                                                        ServiceFilterResponse arg2) {

                                                   mainActivity.hideProgressBar();

                                                    if (exception == null) {
                                                        Log.d(TAG,
                                                                "Fermata salvata con successo! id: "
                                                                        + fermataObject
                                                                        .get("id")
                                                                        .getAsInt());

                                                        Log.d(TAG,
                                                                "Fermata salvata con successo!");

                                                        MBUtils.showSuccessToast(
                                                                getActivity(),
                                                                "Grazie! Dopo i controlli la fermata comparirà sulla mappa.");
                                                        dialog.dismiss();

                                                    } else {
                                                        MBUtils.showErrorToast(
                                                                getActivity(),
                                                                exception
                                                                        .getLocalizedMessage());
                                                    }

                                                }

                                            });
                                } else {
                                    ((EditText) dialog
                                            .findViewById(R.id.nome_fermata))
                                            .setError(getString(R.string.error_field_required));
                                    ((EditText) dialog
                                            .findViewById(R.id.nome_fermata))
                                            .requestFocus();
                                }

                            }

                        });

                dialog.show();

            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                Toast.makeText(getActivity(), "Rilascia il marker nel punto esatto della fermata...", Toast.LENGTH_SHORT).show();
            }
        });

        map.getOverlays().add(markerFermata);
        map.invalidate();

        return true;
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

    private void downloadAndShowFermate(Location location) {
        Log.d(TAG, "downloadAndShowFermate");

        if (location != null) {

        fermteList = new LinkedList<JsonObject>();

        mainActivity.showProgressBar();
        mainActivity.showProgressDialog("Sto cercando le fermate bus intorno a te...");
            /* matera
            String filter = 40.666379 + ","
                    + 16.6043199 + ","
                    + (20 * 1000);
            */
        String filter = location.getLatitude() + ","
                + location.getLongitude() + "," + (600 * 1000);
                //+ (MBApplication.getDistanzaKmFermate() * 1000);

        MobileServiceJsonTable fermataTable = mClient.getTable("Fermata");
        fermataTable.execute(fermataTable.parameter("f", filter),
                new TableJsonQueryCallback() {

                    @Override
                    public void onCompleted(JsonElement fermateArray, Exception e, ServiceFilterResponse response) {
                        Log.d("", "onCompleted");

                        if (e != null) {
                            Log.d("exc", e.getLocalizedMessage());
                            if (this != null && e != null) {
                                MBUtils.showErrorToast(getActivity(),
                                      e.getLocalizedMessage());
                                mainActivity.hideProgressBar();
                                mainActivity.dismissProgressDialog();
                            }

                        } else {
                            if (fermateArray.isJsonArray()) {
                                JsonArray array = fermateArray
                                        .getAsJsonArray();
                                for (JsonElement fermataJsonElement : array) {

                                    JsonObject fermata = fermataJsonElement
                                            .getAsJsonObject();
                                    aggiungiFermataSullaMappa(fermata);
                                    fermteList.add(fermata);
                                }
                                poiMarkers.invalidate();
                                map.invalidate();

                                MBApplication
                                      .setFermateParseObjects(fermteList);
                                mainActivity.hideProgressBar();
                                mainActivity.dismissProgressDialog();
                            }
                        }
                    }
                });


        }
        else {
             MBUtils.showWarningToast(getActivity(), "Controlla che il gps sia attivo...");
        }

    }

    protected void aggiungiFermataSullaMappa(JsonObject fermata) {
        Log.d(TAG, "aggiungiFermataSullaMappa: " + fermata.toString());

        // 39.329194,16.24394
        Location locationFermata = new Location("location");
        locationFermata
                .setLatitude(fermata.get("fermata_lat").getAsFloat());
        locationFermata.setLongitude(fermata.get("fermata_lon")
                .getAsFloat());

        GeoPoint startPoint = new GeoPoint(fermata.get("fermata_lat").getAsFloat(), fermata.get("fermata_lon")
                .getAsFloat());

        final Location myCurrentLocation = mainActivity.getCurrentLocation();
        if (myCurrentLocation != null) {
            // distanza in metri
            double distanza = myCurrentLocation.distanceTo(locationFermata);

            String snippet = "";
            if (distanza >= 1000) {
                distanza = distanza / 1000;
                snippet = (int) distanza + " km";
            } else {
                snippet = (int) distanza + " m";
            }

            Marker markerFermata = new Marker(map);
            markerFermata.setPosition(startPoint);
            markerFermata.setTitle(fermata.get("fermata_name").getAsString());
            markerFermata.setSnippet(snippet);
            markerFermata.setIcon(getResources().getDrawable(R.mipmap.ic_bus_marker));

            markerFermata.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker, MapView mapView) {
                    //marker.showInfoWindow();

                    if (markers.containsKey(marker.toString())) {

                        final JsonObject fermata = markers.get(marker.toString());

                        final Location locationFermata = new Location("location");
                        locationFermata
                                .setLatitude(fermata.get("fermata_lat").getAsFloat());
                        locationFermata.setLongitude(fermata.get("fermata_lon")
                                .getAsFloat());

                        final Dialog dialog = new Dialog(getActivity());
                        dialog.requestWindowFeature((int) Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.custom_info_window);

                        Button buttonChat = (Button) dialog.findViewById(R.id.chatbutton);
                        buttonChat.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                MBUtils.showChatDialog(MapFragment.this,
                                        marker.getTitle(), locationFermata.getLatitude(),
                                        locationFermata.getLongitude());
                            }
                        });

                        Button realtimebutton = (Button) dialog
                                .findViewById(R.id.realtimebutton);
                        realtimebutton.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                goToWebView(fermata.get("stop_url").getAsString());
                            }
                        });

                        try {
                            if (!TextUtils.isEmpty(fermata.get("stop_url").getAsString())) {
                                realtimebutton.setVisibility(Button.VISIBLE);
                            }
                        } catch (Exception e1) {
                        }

                        Button buttonCondividi = (Button) dialog
                                .findViewById(R.id.condividibutton);
                        buttonCondividi.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                // share intent
					/*
					 * if (ParseFacebookUtils.getSession() != null &&
					 * ParseFacebookUtils.getSession().isOpened()) {
					 *
					 * Bundle postParams = new Bundle();
					 * postParams.putString("name",
					 * "Nei pressi della fermata #bus " +
					 * fermata.get("fermata_name") .getAsString() +
					 * " - #MagicBus http://urlin.it/4f67e");
					 * postParams.putString("caption",
					 * "L'app social gps per i mezzi pubblici"); //postParams //
					 * .putString("description", //
					 * "Informazioni realtime sul trasporto pubblico!");
					 * postParams .putString("link",
					 * "https://play.google.com/store/apps/details?id=com.magicbusapp.magicbus"
					 * ); postParams .putString("picture",
					 * "http://www.magicbusapp.it/img/bus_stop.png");
					 *
					 * WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
					 * getActivity(), ParseFacebookUtils.getSession(),
					 * postParams)).setOnCompleteListener( new
					 * OnCompleteListener() {
					 *
					 * @Override public void onComplete(Bundle values,
					 * FacebookException error) { if (error == null) { // When
					 * the story is posted, echo // the // success // and the
					 * post Id. final String postId = values
					 * .getString("post_id"); if (postId != null) {
					 * MBUtils.showSuccessToast( getActivity(),
					 * "Grazie per la condivisione!"); } else { // User clicked
					 * the Cancel // button
					 *
					 * } } else if (error instanceof
					 * FacebookOperationCanceledException) { // User clicked the
					 * "x" button Toast.makeText(getActivity(),
					 * "Publish cancelled", Toast.LENGTH_SHORT).show(); } else {
					 * // Generic, ex: network error
					 * Toast.makeText(getActivity(), "Error posting story",
					 * Toast.LENGTH_SHORT).show(); } }
					 *
					 * }).build(); feedDialog.show(); } else {
					 */
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent
                                        .putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Nei pressi della fermata #bus "
                                                        + fermata.get("fermata_name")
                                                        .getAsString()
                                                        + " - #MagicBus via @MagicBusApp http://urlin.it/4f67e");
                                startActivity(Intent
                                        .createChooser(shareIntent, "Condividi"));
                                // }

                            }

                        });

                        final Button buttonOrari = (Button) dialog
                                .findViewById(R.id.oraributton);
                        buttonOrari.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                Log.d(TAG, "onClick");
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                mainActivity.goToMBOrari(fermata);

                            }

                        });

                        TextView nomeFermataView = (TextView) dialog
                                .findViewById(R.id.nomefermata);
                        nomeFermataView.setText(marker.getTitle());

                        TextView distanzaView = (TextView) dialog
                                .findViewById(R.id.distanza);
                        distanzaView.setText(marker.getSnippet());

                        final TextView infoOraView = (TextView) dialog
                                .findViewById(R.id.infoora);
                        infoOraView.setText("Caricamento...");

                        String filter = fermata.get("id").getAsInt() + "";

                        MobileServiceJsonTable fermataTable = mClient.getTable("Orario");
                        fermataTable.execute(fermataTable.parameter("fermata_id", filter),
                                new TableJsonQueryCallback() {

                                    @Override
                                    public void onCompleted(JsonElement orariJson, Exception e, ServiceFilterResponse arg3) {

                                        if (e != null) {
                                            Log.d("exc", e.getLocalizedMessage());
                                            infoOraView.setText("");
                                        } else {

                                            if (orariJson.isJsonArray()) {
                                                JsonArray array = orariJson
                                                        .getAsJsonArray();
                                                if (array.size() == 0) {
                                                    infoOraView
                                                            .setText("Orario non ancora disponibile :(");
                                                } else {
                                                    infoOraView
                                                            .setText("Prossime linee:\n");

                                                    for (JsonElement orario : array) {

                                                        JsonObject o = orario
                                                                .getAsJsonObject();
                                                        String orariString = "";

                                                        orariString = orariString.concat(o
                                                                .get("departure")
                                                                .getAsString());

                                                        try {
                                                            if (!TextUtils.isEmpty(o.get(
                                                                    "tratta_short_name")
                                                                    .getAsString())) {
                                                                orariString = orariString
                                                                        .concat(" - "
                                                                                + o.get("tratta_short_name")
                                                                                .getAsString());
                                                            }
                                                        } catch (Exception e1) {
                                                        }
                                                        try {
                                                            if (!TextUtils.isEmpty(o.get(
                                                                    "tratta_headsign")
                                                                    .getAsString())) {
                                                                orariString = orariString
                                                                        .concat(" - "
                                                                                + o.get("tratta_headsign")
                                                                                .getAsString());
                                                            }
                                                        } catch (Exception e2) {
                                                        }

                                                        orariString = orariString
                                                                .concat("\n");
                                                        infoOraView.append(orariString);

                                                    }
                                                    buttonOrari
                                                            .setVisibility(Button.VISIBLE);
                                                }

                                            }
                                        }
                                        mainActivity.hideProgressBar();

                                    }
                                });

                        dialog.show();

                    }

                    return true;
                }
            });
            markers.put(markerFermata.toString(), fermata);

            //map.getOverlays().add(startMarker);
            poiMarkers.add(markerFermata);
            //map.invalidate();
        }

    }

    private void goToWebView(String stop_url) {
        //Intent i = new Intent(getActivity(), MBWebViewActivity.class);
        //i.putExtra("fermata_url", url);
        //startActivity(i);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");

        mainActivity.setMapCenter(map.getMapCenter());
        mainActivity.setZoomLevel(map.getZoomLevel());
        mainActivity.setBoundingBox(map.getBoundingBox());
    }

}
