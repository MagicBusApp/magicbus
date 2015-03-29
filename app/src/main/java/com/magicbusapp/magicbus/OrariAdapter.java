package com.magicbusapp.magicbus;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.JsonObject;

/**
 * Created by giuseppe on 28/03/15.
 */
public class OrariAdapter extends ArrayAdapter<JsonObject> {

    /**
     * Adapter context
     */
    Context mContext;

    /**
     * Adapter View layout
     */
    int mLayoutResourceId;

    public OrariAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    /**
     * Returns the view for a specific item on the list
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final JsonObject currentItem = getItem(position);

        if (row == null) {
            //LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            //row = inflater.inflate(mLayoutResourceId, parent, false);
            row = View.inflate(getContext(),
                    R.layout.row_mb_orario, null);
        }

        row.setTag(currentItem);

        TextView oraTextView = (TextView) row
                .findViewById(R.id.ora);
        TextView trattaTextView = (TextView) row
                .findViewById(R.id.tratta);

        if(currentItem.has("departure")){

            oraTextView.setText(currentItem.get("departure").getAsString());

            try {
                if(!TextUtils.isEmpty(currentItem.get(
                        "tratta_short_name").getAsString())){
                    trattaTextView.setText(currentItem.get("tratta_short_name").getAsString());
                }
            } catch (Exception e1) {
                try {
                    if(!TextUtils.isEmpty(currentItem.get(
                            "tratta_headsign").getAsString())){
                        trattaTextView.setText(currentItem.get("tratta_headsign").getAsString());
                    }
                } catch (Exception e2) {
                    // TODO Auto-generated catch block
                    trattaTextView.setText("-");
                }
            }

				/*if(currentItem.has("tratta_short_name")){
					trattaTextView.setText(currentItem.get("tratta_short_name").getAsString());
				}
				else{
					trattaTextView.setText("-");
				}*/

        }

        return row;
    }
}
