package com.magicbusapp.magicbus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.magicbusapp.magicbus.firebase.MessageChat;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by giuseppe on 14/03/15.
 */
public class MBUtils {

    final static String TAG = MBUtils.class.getName();

    static synchronized void showInfoToast(Activity activity, String msg) {
        Log.d(TAG, "showCustomToast: " + msg);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.info_toast_layout,
                (ViewGroup) activity.findViewById(R.id.custom_relative_toast));

        ((TextView) layout.findViewById(R.id.toast_message)).setText(msg);

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

    }

    static synchronized void showSuccessToast(Activity activity, String msg) {
        Log.d(TAG, "showCustomToast: " + msg);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.success_toast_layout,
                (ViewGroup) activity.findViewById(R.id.custom_relative_toast));

        ((TextView) layout.findViewById(R.id.toast_message)).setText(msg);

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    static synchronized void showErrorToast(Activity activity, String msg) {
        Log.d(TAG, "showCustomToast: " + msg);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.error_toast_layout,
                (ViewGroup) activity.findViewById(R.id.custom_relative_toast));

        ((TextView) layout.findViewById(R.id.toast_message)).setText(msg);

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    static synchronized void showWarningToast(Activity activity, String msg) {
        Log.d(TAG, "showCustomToast: " + msg);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.warning_toast_layout,
                (ViewGroup) activity.findViewById(R.id.custom_relative_toast));

        ((TextView) layout.findViewById(R.id.toast_message)).setText(msg);

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }


    public static void getFacebookInfoInBackground() {
        Log.d(TAG, "getFacebookInfoInBackground");

        Request me = com.facebook.Request.newMeRequest(ParseFacebookUtils.getSession(),
                new Request.GraphUserCallback() {

                    @Override
                    public void onCompleted(GraphUser user, Response response) {

                        if (response.getError() == null && user != null) {
                            Log.d(TAG, "onCompleted\n" + user.toString() + "\n" + response.toString());

                            //Email
                            GraphObject go = response.getGraphObject();
                            ParseUser.getCurrentUser().setEmail(
                                    (String) go.getProperty("email"));
                            ParseUser.getCurrentUser().setUsername(
                                    (String) go.getProperty("email"));

                            try {
                                //Facebook ID
                                ParseUser.getCurrentUser()
                                        .put("fbId", user.getId());

                                ParseUser.getCurrentUser().put("nickname",
                                        user.getUsername());
                                ParseUser.getCurrentUser().put("nome",
                                        user.getFirstName());
                                ParseUser.getCurrentUser().put("cognome",
                                        user.getLastName());

                                if (user.getLocation().getProperty("name") != null) {
                                    ParseUser.getCurrentUser().put("location", (String) user
                                            .getLocation().getProperty("name"));
                                }

                                if (user.getUsername() == null) {
                                    MBUtils.setUpNichname(ParseUser.getCurrentUser());
                                }

                                MBApplication.updatePreferences(ParseUser.getCurrentUser(), true);

                            } catch (Exception e) {
                                Log.d(TAG, e.getLocalizedMessage());
                            }

                            ParseUser.getCurrentUser().saveInBackground();
                        }

                    }
                });

        //Bundle params = me.getParameters();
        //params.putString("fields", "email, name");
        //me.setParameters(params);
        me.executeAsync();

    }

    private static void setUpNichname(ParseUser user) {
        Log.d(TAG, "setUpNichname");

        int id = (int) (Math.random()*5000000);
        String nickname = "MagicUser_" + id;
        user.put("nickname", nickname);
        try {
            user.save();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
        }
    }

    public static String substituteEmoticons(String s){
        String res = s.replace(":)", "\ue415");
        res = s.replace(":(", "\ue403");
        res = s.replace(";-)", "\ue405");

        return res;
    }

    public static String get_avatar_from_service(int service, String userid, int size){
        //https://gist.github.com/jcsrb/1081548/raw/773117bb255e93432e4e627b440a14d1af27076d/gistfile1.js
        // this return the url that redirects to the according user image/avatar/profile picture
        // implemented services: google profiles, facebook, gravatar, twitter, tumblr, default fallback
        // for google   use get_avatar_from_service('google', profile-name or user-id , size-in-px )
        // for facebook use get_avatar_from_service('facebook', vanity url or user-id , size-in-px or size-as-word )
        // for gravatar use get_avatar_from_service('gravatar', md5 hash email@adress, size-in-px )
        // for twitter  use get_avatar_from_service('twitter', username, size-in-px or size-as-word )
        // for tumblr   use get_avatar_from_service('tumblr', blog-url, size-in-px )
        // everything else will go to the fallback
        // google and gravatar scale the avatar to any site, others will guided to the next best version

        String url = "";
        String sizeparam = "";

        switch(service){

            case 1:
                //facebook
                // see https://developers.facebook.com/docs/reference/api/
                // available sizes: square (50x50), small (50xH) , normal (100xH), large (200xH)

                if (size >= 200) {
                    sizeparam = "large";
                };
                if (size >= 100 && size < 200) {
                    sizeparam = "normal";
                };
                if (size >= 50 && size < 100) {
                    sizeparam = "small";
                };
                if (size < 50) {
                    sizeparam = "square";
                };
                url = "https://graph.facebook.com/" + userid + "/picture?width=64&height=64";
                break;

            case 2:
                //twitter
                // see https://dev.twitter.com/docs/api/1/get/users/profile_image/%3Ascreen_name
                // available sizes: bigger (73x73), normal (48x48), mini (24x24), no param will give you full size

                if (size >= 73) {
                    sizeparam = "bigger";
                };
                if (size >= 48 && size < 73) {
                    sizeparam = "normal";
                };
                if (size < 48) {
                    sizeparam = "mini";
                };

                url = "http://api.twitter.com/1/users/profile_image?screen_name=" + userid + "&size=" + sizeparam;
                break;

            default:
                break;
        }

        return url;
    }

    private static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;
        if(bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;
        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
                sbmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2+0.7f, sbmp.getHeight() / 2+0.7f,
                sbmp.getWidth() / 2+0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }

    public static String getDateAgo(Date date){

        String format = "dd MMM yyyy  kk:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ITALY);

        DateTime dt1 = new DateTime(new DateTime(date));
        DateTime dt2 = new DateTime(new DateTime(new Date()));

        int days = Days.daysBetween(dt1, dt2).getDays();
        int hours = Hours.hoursBetween(dt1, dt2).getHours();
        int minutes = Minutes.minutesBetween(dt1, dt2).getMinutes();

        if (hours > 0){
            if (hours == 1)
                return "1 ora fa";
            else if (hours < 24)
                return String.valueOf(hours) + " ore fa";
            else
            {
                if (days == 1)
                    return "1 giorno fa";
                else
                    return sdf.format(date);
            }
        }
        else
        {
            if (minutes == 0)
                return "pochi secondi fa";
            else if (minutes == 1)
                return "1 minuto fa";
            else
                return String.valueOf(minutes) + " minuti fa";
        }
    }

    public static void showChatDialog(final Activity activity, String nickname, final double lat, final double lng) {
        Log.d(TAG, "showChatDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("Risposta @ " + nickname); //Set Alert dialog title here
        alert.setMessage("*Il messaggio sarà visibile anche agli altri passengers.");
        alert.setIcon(R.mipmap.ic_chatmessage);
        // Set an EditText view to get user input
        final EditText input = new EditText(activity);
        alert.setView(input);

        alert.setPositiveButton("Invia", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String srt = input.getEditableText().toString();
                if(srt.length()>0){

                    ParseUser user = ParseUser.getCurrentUser();

                    MessageChat messageChat = new MessageChat(srt,
                            new Date(),
                            user.getObjectId(),
                            user.getString("nickname"),
                            user.getString("fbId"),
                            MBApplication.getAvatarPreference(),
                            lat,
                            lng);
                    // Setup our Firebase ref
                    Firebase ref = new Firebase(activity.getResources().getString(R.string.FIREBASE_URL)).child(activity.getResources().getString(R.string.FIREBASE_CHAT_ROOM));
                    ref.push().setValue(messageChat, new Firebase.CompletionListener(){

                        @Override
                        public void onComplete(
                                FirebaseError e,
                                Firebase arg1) {
                            if(e == null){
                                MBUtils.showSuccessToast(activity, "Messaggio inviato!");
                            }
                        }

                    });

                    Log.d(TAG, "sendPushNotification");
                    sendPushNotification(messageChat);

                }
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton
        alert.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        }); //End of alert.setNegativeButton
        alert.show();

    }

    public static void showChatDialog(final MapFragment mapFragment,
                                      String nomeFermata, final double lat, final double lng) {
        Log.d(TAG, "showChatDialog");

        AlertDialog.Builder alert = new AlertDialog.Builder(mapFragment.getActivity());
        alert.setTitle("MagicChat"); //Set Alert dialog title here
        alert.setMessage("*Una notifica push sarà inviata a tutti i passengers nei pressi della fermata bus #" + nomeFermata + ".");
        alert.setIcon(R.mipmap.ic_chatmessage);
        // Set an EditText view to get user input
        final EditText input = new EditText(mapFragment.getActivity());
        alert.setView(input);

        alert.setPositiveButton("Invia", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String srt = input.getEditableText().toString();
                if(srt.length()>0){

                    ParseUser user = ParseUser.getCurrentUser();

                    MessageChat messageChat = new MessageChat(srt,
                            new Date(),
                            user.getObjectId(),
                            user.getString("nickname"),
                            user.getString("fbId"),
                            MBApplication.getAvatarPreference(),
                            lat,
                            lng);
                    // Setup our Firebase ref
                    Firebase ref = new Firebase(mapFragment.getResources().getString(R.string.FIREBASE_URL)).child(mapFragment.getResources().getString(R.string.FIREBASE_CHAT_ROOM));
                    ref.push().setValue(messageChat, new Firebase.CompletionListener(){

                        @Override
                        public void onComplete(
                                FirebaseError e,
                                Firebase arg1) {
                            if(e == null){
                                MBUtils.showSuccessToast(mapFragment.getActivity(), "Messaggio inviato!");
                            }
                        }

                    });

                    Log.d(TAG, "sendPushNotification");
                    sendPushNotification(messageChat);

                }
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton
        alert.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        }); //End of alert.setNegativeButton
        alert.show();

    }

    protected static void sendPushNotification(MessageChat messageChat) {
        Log.d(TAG, "sendPushNotification");

        // Find users near a given location
        ParseQuery<ParseInstallation> userQuery = ParseInstallation.getQuery();
        userQuery.whereWithinMiles("location", new ParseGeoPoint(
                messageChat.getLatitude(),
                messageChat.getLongitude()), 6.5);
        userQuery.whereNotEqualTo("installationId", ParseInstallation
                .getCurrentInstallation().getInstallationId());
        // Notification for Android users
        userQuery.whereEqualTo("deviceType", "android");

        // Create time interval
        long quartodoraInterval = 60 * 15; // 60*60*24*7; // 1 week

        // Send push notification to query
        ParsePush push = new ParsePush();
        //push.setChannel("debug");
        push.setExpirationTimeInterval(quartodoraInterval);
        push.setQuery(userQuery); // Set our Installation query


        JSONObject data = new JSONObject();
        //new JSONObject("{\"action\": \"com.example.UPDATE_STATUS\","\"name\": \"Vaughn\",\"newsItem\": \"Man bites dog\""}));
        try {
            data.put("alert", messageChat.getMessaggio());
            data.put("nickname", messageChat.getNickname());
            data.put("fbId", messageChat.getFbId());
            data.put("latitude", messageChat.getLatitude());
            data.put("longitude", messageChat.getLongitude());
            push.setData(data);
        } catch (JSONException e) {
            push.setMessage(messageChat.getNickname() + " ha scritto: '" + messageChat.getMessaggio() + "'");
        }

        push.sendInBackground();

    }

    public static void showChatDialogFromMain(final FragmentActivity activity, String s, final double latitude, final double longitude) {
        Log.d(TAG, "showChatDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(s); //Set Alert dialog title here
        alert.setMessage("*Il messaggio sarà visibile a tutti i passengers nelle tue vicinanze.");
        alert.setIcon(R.mipmap.ic_chatmessage);
        // Set an EditText view to get user input
        final EditText input = new EditText(activity);
        alert.setView(input);

        alert.setPositiveButton("Invia", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String srt = input.getEditableText().toString();
                if(srt.length()>0){

                    ParseUser user = ParseUser.getCurrentUser();

                    MessageChat messageChat = new MessageChat(srt,
                            new Date(),
                            user.getObjectId(),
                            user.getString("nickname"),
                            user.getString("fbId"),
                            MBApplication.getAvatarPreference(),
                            latitude,
                            longitude);
                    // Setup our Firebase ref
                    Firebase ref = new Firebase(activity.getResources().getString(R.string.FIREBASE_URL)).child(activity.getResources().getString(R.string.FIREBASE_CHAT_ROOM));
                    ref.push().setValue(messageChat, new Firebase.CompletionListener(){

                        @Override
                        public void onComplete(
                                FirebaseError e,
                                Firebase arg1) {
                            if(e == null){
                                MBUtils.showSuccessToast(activity, "Messaggio inviato!");
                            }
                        }

                    });

                    Log.d(TAG, "sendPushNotification");
                    sendPushNotification(messageChat);

                }
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton
        alert.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        }); //End of alert.setNegativeButton
        alert.show();
    }
}