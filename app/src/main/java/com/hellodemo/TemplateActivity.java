package com.hellodemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.hellodemo.adapter.NavGroupAdapter;
import com.hellodemo.adapter.NavScreenAdapter;
import com.hellodemo.interfaces.ScreenInterfaceListener;
import com.hellodemo.interfaces.UIConstants;
import com.hellodemo.models.NavGroupItem;
import com.hellodemo.models.NavMenuModel;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class TemplateActivity extends AppCompatActivity implements ScreenInterfaceListener {

    RecyclerView nav_groups_recycler_view, nav_screens_recycler_view;
    ImageView toolbar_music_recycler_view_list_item_image, img_nav_back, img_nav_profile;

    TextView toolbar_title, txt_nav_name, txt_nav_notif_count, txt_nav_newdemo;

    View view_nav_notif_highlight;

    UserModel mUserModel;
    private NavMenuModel mNavMenuModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        nav_groups_recycler_view = findViewById(R.id.nav_groups_recycler_view);
        nav_screens_recycler_view = findViewById(R.id.nav_screens_recycler_view);
        toolbar_music_recycler_view_list_item_image = findViewById(R.id.notification_avatar);
        toolbar_title = findViewById(R.id.toolbar_title);
        img_nav_back = findViewById(R.id.img_nav_back);
        img_nav_profile = findViewById(R.id.img_nav_profile);
        txt_nav_name = findViewById(R.id.txt_nav_name);
        txt_nav_notif_count = findViewById(R.id.txt_nav_notif_count);
        view_nav_notif_highlight = findViewById(R.id.view_nav_notif_highlight);
        txt_nav_newdemo = findViewById(R.id.txt_nav_newdemo);

        nav_groups_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        nav_screens_recycler_view.setLayoutManager(new LinearLayoutManager(this));

        nav_screens_recycler_view.setAdapter(new NavScreenAdapter(this));
        nav_groups_recycler_view.setAdapter(new NavGroupAdapter(this));

        mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);

        bindValues();

        onLoadService("get-inbox", -1);

        clickListeners();
    }

    private void bindValues() {
        if (mUserModel == null) return;
        Picasso.with(this).load(mUserModel.getAvatar()).into(img_nav_profile);
        Picasso.with(this).load(mUserModel.getAvatar()).into(toolbar_music_recycler_view_list_item_image);
        txt_nav_name.setText(mUserModel.getFullName());
    }

    private void clickListeners() {
        img_nav_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        toolbar_music_recycler_view_list_item_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMenuService();
    }

    private void onLoadService(String action, long group_id) {
        String url;
        if (group_id == -1) {
            url = BuildConfig.BASE_URL + "/" + action + "?user_id=" + mUserModel.getId();
        } else {
            url = BuildConfig.BASE_URL + "/" + action + "?group_id=" + group_id;
        }
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);
        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (!response.optBoolean("error", true)) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");
            }
        });
    }

    private void getMenuService() {
        String url = BuildConfig.BASE_URL + "/get-menu?user_id=" + mUserModel.getId();
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);
        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (!response.optBoolean("error", true)) {
                    mNavMenuModel = Utils.parseJson(response.optJSONObject("data").toString(), NavMenuModel.class);
                    UserSharedPreferences.saveString(TemplateActivity.this, UserSharedPreferences.MENU_MODEL,
                            response.optJSONObject("data").toString());
                    txt_nav_notif_count.setText(String.valueOf(mNavMenuModel.getDemoNum()));
                    ((NavScreenAdapter) nav_screens_recycler_view.getAdapter()).setData(mNavMenuModel);
                    ((NavGroupAdapter) nav_groups_recycler_view.getAdapter()).setData(mNavMenuModel);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");
            }
        });
    }

    public void logout(View view) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        UserSharedPreferences.deleteSharedPreferences(this);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void newGroup(View view) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void selectedScreen(int position, String title) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch (position) {

            case 2: { //messages
                break;
            }
            case 4: { //Settings
                break;
            }
            default:{
                Intent intent = new Intent(this, MainActivity.class);
                intent .setAction(UIConstants.IntentActions.OPEN_MUSIC_LIST_SCREEN);
                intent.putExtra(UIConstants.IntentExtras.SCREEN_TITLE, title);
                intent.putExtra(UIConstants.IntentExtras.SCREEN_POSITION, position);
                startActivity(intent);
            }
        }
    }

    @Override
    public void selectedGroup(NavGroupItem group) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        Intent intent = new Intent(this, MainActivity.class);
        intent .setAction(UIConstants.IntentActions.OPEN_MUSIC_LIST_SCREEN);
        intent.putExtra(UIConstants.IntentExtras.SELECTED_GROUP, group);
        startActivity(intent);
    }

    @Override
    public void setActivityNameNull() { }

    /**
     * Custom request to make multipart header and upload file.
     *
     * Sketch Project Studio
     * Created by Angga on 27/04/2016 12.05.
     */
    public static class VolleyMultipartRequest extends Request<NetworkResponse> {
        private final String twoHyphens = "--";
        private final String lineEnd = "\r\n";
        private final String boundary = "apiclient-" + System.currentTimeMillis();

        private Response.Listener<NetworkResponse> mListener;
        private Response.ErrorListener mErrorListener;
        private Map<String, String> mHeaders;

        /**
         * Default constructor with predefined header and post method.
         *
         * @param url           request destination
         * @param headers       predefined custom header
         * @param listener      on success achieved 200 code from request
         * @param errorListener on error http or library timeout
         */
        public VolleyMultipartRequest(String url, Map<String, String> headers,
                                      Response.Listener<NetworkResponse> listener,
                                      Response.ErrorListener errorListener) {
            super(Method.POST, url, errorListener);
            this.mListener = listener;
            this.mErrorListener = errorListener;
            this.mHeaders = headers;
        }

        /**
         * Constructor with option method and default header configuration.
         *
         * @param method        method for now accept POST and GET only
         * @param url           request destination
         * @param listener      on success event handler
         * @param errorListener on error event handler
         */
        public VolleyMultipartRequest(int method, String url,
                                      Response.Listener<NetworkResponse> listener,
                                      Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.mListener = listener;
            this.mErrorListener = errorListener;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return (mHeaders != null) ? mHeaders : super.getHeaders();
        }

        @Override
        public String getBodyContentType() {
            return "multipart/form-data;boundary=" + boundary;
        }

        @Override
        public byte[] getBody() throws AuthFailureError {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            try {
                // populate text payload
                Map<String, String> params = getParams();
                if (params != null && params.size() > 0) {
                    textParse(dos, params, getParamsEncoding());
                }

                // populate data byte payload
                Map<String, DataPart> data = getByteData();
                if (data != null && data.size() > 0) {
                    dataParse(dos, data);
                }

                // close multipart form data after text and file data
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                return bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Custom method handle data payload.
         *
         * @return Map data part label with data byte
         * @throws AuthFailureError
         */
        protected Map<String, DataPart> getByteData() throws AuthFailureError {
            return null;
        }

        @Override
        protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
            try {
                return Response.success(
                        response,
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (Exception e) {
                return Response.error(new ParseError(e));
            }
        }

        @Override
        protected void deliverResponse(NetworkResponse response) {
            mListener.onResponse(response);
        }

        @Override
        public void deliverError(VolleyError error) {
            mErrorListener.onErrorResponse(error);
        }

        /**
         * Parse string map into data output stream by key and value.
         *
         * @param dataOutputStream data output stream handle string parsing
         * @param params           string inputs collection
         * @param encoding         encode the inputs, default UTF-8
         * @throws IOException
         */
        private void textParse(DataOutputStream dataOutputStream, Map<String, String> params, String encoding) throws IOException {
            try {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    buildTextPart(dataOutputStream, entry.getKey(), entry.getValue());
                }
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException("Encoding not supported: " + encoding, uee);
            }
        }

        /**
         * Parse data into data output stream.
         *
         * @param dataOutputStream data output stream handle file attachment
         * @param data             loop through data
         * @throws IOException
         */
        private void dataParse(DataOutputStream dataOutputStream, Map<String, DataPart> data) throws IOException {
            for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                buildDataPart(dataOutputStream, entry.getValue(), entry.getKey());
            }
        }

        /**
         * Write string data into header and data output stream.
         *
         * @param dataOutputStream data output stream handle string parsing
         * @param parameterName    name of input
         * @param parameterValue   value of input
         * @throws IOException
         */
        private void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd);
            //dataOutputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(parameterValue + lineEnd);
        }

        /**
         * Write data file into header and data output stream.
         *
         * @param dataOutputStream data output stream handle data parsing
         * @param dataFile         data byte as DataPart from collection
         * @param inputName        name of data input
         * @throws IOException
         */
        private void buildDataPart(DataOutputStream dataOutputStream, DataPart dataFile, String inputName) throws IOException {
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                    inputName + "\"; filename=\"" + dataFile.getFileName() + "\"" + lineEnd);
            if (dataFile.getType() != null && !dataFile.getType().trim().isEmpty()) {
                dataOutputStream.writeBytes("Content-Type: " + dataFile.getType() + lineEnd);
            }
            dataOutputStream.writeBytes(lineEnd);

            ByteArrayInputStream fileInputStream = new ByteArrayInputStream(dataFile.getContent());
            int bytesAvailable = fileInputStream.available();

            int maxBufferSize = 1024 * 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dataOutputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dataOutputStream.writeBytes(lineEnd);
        }

        /**
         * Simple data container use for passing byte file
         */
        public class DataPart {
            private String fileName;
            private byte[] content;
            private String type;

            /**
             * Default data part
             */
            public DataPart() {
            }

            /**
             * Constructor with data.
             *
             * @param name label of data
             * @param data byte data
             */
            public DataPart(String name, byte[] data) {
                fileName = name;
                content = data;
            }

            /**
             * Constructor with mime data type.
             *
             * @param name     label of data
             * @param data     byte data
             * @param mimeType mime data like "image/jpeg"
             */
            public DataPart(String name, byte[] data, String mimeType) {
                fileName = name;
                content = data;
                type = mimeType;
            }

            /**
             * Getter file name.
             *
             * @return file name
             */
            public String getFileName() {
                return fileName;
            }

            /**
             * Setter file name.
             *
             * @param fileName string file name
             */
            public void setFileName(String fileName) {
                this.fileName = fileName;
            }

            /**
             * Getter content.
             *
             * @return byte file data
             */
            public byte[] getContent() {
                return content;
            }

            /**
             * Setter content.
             *
             * @param content byte file data
             */
            public void setContent(byte[] content) {
                this.content = content;
            }

            /**
             * Getter mime type.
             *
             * @return mime type
             */
            public String getType() {
                return type;
            }

            /**
             * Setter mime type.
             *
             * @param type mime type
             */
            public void setType(String type) {
                this.type = type;
            }
        }
    }
}
