package com.hellodemo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hellodemo.BuildConfig;
import com.hellodemo.MainActivity;
import com.hellodemo.R;
import com.hellodemo.models.NavMenuModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class NotificationsSettingsAdapter extends BaseAdapter {

    private String TAG = "HelloDemoNotificationsSettingsAdapter";
    private final String[] actionTags;
    private Context mContext;
    private String[] Title;
    private int[] imge;
    private boolean[] settingsValue;

    public NotificationsSettingsAdapter(Context context,
                                        String[] text1,
                                        int[] imageIds,
                                        String[] actionTags,
                                        boolean[] settingsValue) {
        //  int[] imageIds
        mContext = context;
        Title = text1;
        imge = imageIds;
        this.actionTags = actionTags;
        this.settingsValue = settingsValue;

    }

    public int getCount() {
        // TODO Auto-generated method stub
        return Title.length;
    }

    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        //LayoutInflater inflater = null;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row;
        row = inflater.inflate(R.layout.notification_settings_list_row, parent, false);
        TextView title;
        ImageView i1;
        i1 = (ImageView) row.findViewById(R.id.icon);
        title = (TextView) row.findViewById(R.id.Itemname);
        title.setText(Title[position]);
        i1.setImageResource(imge[position]);


        Switch mySwitch = row.findViewById(R.id.switch1);
        mySwitch.setChecked(settingsValue[position]);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("test", "+" + isChecked);
                callSetNotificationSettingsAPI(actionTags[position], isChecked);
            }
        });

        return row;


    }

    // this calls the api to change the required setting
    private void callSetNotificationSettingsAPI(String actionTag, boolean isChecked) {
        Log.v("test", "=" + actionTag + "..." + isChecked);
        String url = BuildConfig.BASE_URL + "/setting/change";


        VolleyRequest volleyRequest = new VolleyRequest(mContext, url, "main", true);

        HashMap<String, String> params = new HashMap<>();
        params.put("type", actionTag);
        params.put("value", String.valueOf(isChecked));
        Log.v(TAG, "Calling Set Notifications Settings API URL : " + url);
        Log.v(TAG, "Set Notifications Settings API Request Params : " + params.toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.v(TAG, "Set Notifications Settings API Response:" + response.toString());
                if (!response.optBoolean("error", true)) {
//                    CustomToast.makeText(mContext, "Saved!", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "Set Notifications Settings API Error Response:" + error.getMessage());
                Log.v(TAG, "Set Notifications Settings API Error Response:" + error.getLocalizedMessage());
                Log.v(TAG, "Set Notifications Settings API Error Response:" + error.toString());
            }
        });
    }


}

