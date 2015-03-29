package com.magicbusapp.magicbus;

import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.magicbusapp.magicbus.firebase.MessageChat;
import com.magicbusapp.magicbus.firebase.MessageChatListAdapter;
import com.parse.ParseUser;

import java.util.Date;

public class MBChat_Firebase extends Fragment {

    private static final String TAG = MBChat_Firebase.class.getSimpleName();
    private MBApplication mbApplication;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private ImageButton mSendButton;
    private TextView mNumeroPartecipanti;

    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    private Firebase ref;
    private ValueEventListener connectedListener;
    private MessageChatListAdapter messageChatListAdapter;

    public MBChat_Firebase(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mbApplication = (MBApplication) getActivity().getApplication();
        getActivity().setTitle(mbApplication.getNickname() + ", connessione...");

        // Setup our Firebase ref
        ref = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child(getResources().getString(R.string.FIREBASE_CHAT_ROOM));
        //ref = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child(getResources().getString(R.string.FIREBASE_CHAT_ROOM_DEBUG));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mbchat, container, false);

        setupChat(view);
        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();

        ((MainActivity)getActivity()).showProgressDialog("Connessione in corso...");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        //ref.getRoot().child(".info/connected").removeEventListener(connectedListener);
        //messageChatListAdapter.cleanup();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");

        if (savedInstanceState != null) {
            //Restore the fragment's state here
        }
    }

    protected void sendMessage(String msg) {
        Log.d(TAG, "sendMessage");
        // Reset out string buffer to zero and clear the edit text field
        mOutStringBuffer.setLength(0);
        mOutEditText.setText(mOutStringBuffer);

        ParseUser user = ParseUser.getCurrentUser();

        MessageChat messageChat = new MessageChat(msg,
                new Date(),
                user.getObjectId(),
                user.getString("nickname"),
                user.getString("fbId"),
                MBApplication.getAvatarPreference(),
                ((MainActivity) getActivity()).getCurrentLocation().getLatitude(),
                ((MainActivity) getActivity()).getCurrentLocation().getLongitude());

        ref.push().setValue(messageChat);

        MBUtils.sendPushNotification(messageChat);
    }

    private void setupChat(View v) {
        Log.d(TAG, "setupChat()");

        mConversationView = (ListView) v.findViewById(R.id.newchatList);
        messageChatListAdapter = new MessageChatListAdapter(ref.limitToLast(50), R.layout.row_mbchat, getActivity(), mbApplication.getNickname());
        mConversationView.setAdapter(messageChatListAdapter);
        messageChatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mConversationView.setSelection(messageChatListAdapter.getCount() - 1);
            }
        });

        // Finally, a little indication of connection status
        connectedListener = ref.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean)dataSnapshot.getValue();
                if (connected) {
                    if(getActivity()!=null){
                        ((MainActivity)getActivity()).dismissProgressDialog();

                        MBUtils.showInfoToast(getActivity(), mbApplication.getNickname() + " sei online!");

                        ((MainActivity)getActivity()).mTitle = mbApplication.getNickname() + ", online";
                        ((MainActivity)getActivity()).setTitle(mbApplication.getNickname() + ", online");
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError arg0) {
                // TODO Auto-generated method stub

            }
        });

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) v.findViewById(R.id.edit_text_out_newchat);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (ImageButton) v.findViewById(R.id.invia_button_newchat);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.e(TAG, "[sendButton clicked]");

                // Send a message using content of the edit text widget
                String message = mOutEditText.getText().toString();
                if (message != null && !message.equals(""))
                    sendMessage(message);
            }
        });
        // mNumeroPartecipanti = (TextView)
        // findViewById(R.id.numero_partecipanti_textview);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId,
                                      KeyEvent event) {
            // If the action is a key-up event on the return key, send the
            // message
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            Log.i(TAG, "END onEditorAction");
            return true;
        }
    };
}