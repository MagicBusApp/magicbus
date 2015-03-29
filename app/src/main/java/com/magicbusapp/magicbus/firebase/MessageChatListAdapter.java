package com.magicbusapp.magicbus.firebase;

/**
 * Created by giuseppe on 14/03/15.
 */

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Query;
import com.magicbusapp.magicbus.MBApplication;
import com.magicbusapp.magicbus.MBUtils;
import com.magicbusapp.magicbus.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class MessageChatListAdapter extends FirebaseListAdapter<MessageChat> {

    private String myUsername;

    public MessageChatListAdapter(Query ref, int layout, Activity activity,
                                  String username) {
        super(ref, MessageChat.class, layout, activity);
        this.myUsername = username;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MessageChat model = mModels.get(i);
        String nickname = model.getNickname();

        if (view == null) {
            if (!myUsername.equals(nickname)) {
                // I'm RECIVER
                view = mInflater.inflate(R.layout.row_mbchat, viewGroup, false);
                nickname = model.getNickname();
            } else {
                // I'm, SENDER
                view = mInflater.inflate(R.layout.row_mbchat_sender, viewGroup,
                        false);
                nickname = "io";
            }
        } else {
            if (!myUsername.equals(nickname)) {
                // I'm RECIVER
                view = mInflater.inflate(R.layout.row_mbchat, viewGroup, false);
                nickname = model.getNickname();
            } else {
                // I'm, SENDER
                view = mInflater.inflate(R.layout.row_mbchat_sender, viewGroup,
                        false);
                nickname = "io";
            }
        }
        // Call out to subclass to marshall this model into the provided view
        populateView(view, model, nickname);
        return view;
    }

    @Override
    protected void populateView(View v, MessageChat messageChat, String nickname) {
        Log.d("MessageChatListAdapter", "populateView: " + messageChat.getDataInvio());

        String avatar = "";

        if (!myUsername.equals(nickname)) {
            // I'm RECIVER
            if (messageChat.getAvatar() == null) {
                avatar = "default";
            } else {
                avatar = messageChat.getAvatar();
            }
        } else {
            // I'm, SENDER
            avatar = MBApplication.getAvatarPreference();
        }

        TextView chatMessageView = (TextView) v.findViewById(R.id.chat);
        TextView nicknameView = (TextView) v.findViewById(R.id.chat_nickname);
        ImageView thumb_image = (ImageView) v.findViewById(R.id.list_image);

        chatMessageView.setText(messageChat.getMessaggio());

        nicknameView.setText(nickname + " @ " + MBUtils.getDateAgo(messageChat.getDataInvio()));

        if(nickname.equalsIgnoreCase("MagicBus Team")){
            thumb_image.setImageResource(R.drawable.ic_launcher);
        }
        else if (!TextUtils.isEmpty(messageChat.getFbId())
                && avatar.equals("facebook")) {

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true).cacheOnDisc(true)
                    .displayer(new RoundedBitmapDisplayer(5)).build();
            ImageLoader.getInstance().displayImage(
                    MBUtils.get_avatar_from_service(1, messageChat.getFbId(),
                            48), thumb_image, options);


        } else {

            if (avatar.equals("zoidberg"))
                thumb_image.setImageResource(R.drawable.zoidberg);
            else if (avatar.equals("fry"))
                thumb_image.setImageResource(R.drawable.fry);
            else if (avatar.equals("leela"))
                thumb_image.setImageResource(R.drawable.leela);
            else if (avatar.equals("hermes"))
                thumb_image.setImageResource(R.drawable.hermes);
            else if (avatar.equals("bender"))
                thumb_image.setImageResource(R.drawable.bender);
            else if (avatar.equals("amy"))
                thumb_image.setImageResource(R.drawable.amy);
            else if (avatar.equals("farnsworth"))
                thumb_image.setImageResource(R.drawable.farnsworth);
            else
                thumb_image.setImageResource(R.drawable.default_chat_icon);
        }

    }

}