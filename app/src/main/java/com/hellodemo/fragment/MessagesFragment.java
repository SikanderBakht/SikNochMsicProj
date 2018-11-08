package com.hellodemo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.reflect.TypeToken;
import com.hellodemo.BuildConfig;
import com.hellodemo.R;
import com.hellodemo.adapter.MessagesListRecyclerAdapter;
import com.hellodemo.models.MessagesUserGroupListItem;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.List;

public class MessagesFragment extends Fragment {

    private String TAG = "HelloDemoMessagesFragment";
    private UserModel mUserModel;
    private RecyclerView messages_list_recycler_view;
    private Socket mSocket;

    public MessagesFragment() {
        // Required empty public constructor
    }

    public static MessagesFragment newInstance() {
        MessagesFragment fragment = new MessagesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mUserModel = Utils.parseJson(
                UserSharedPreferences.getString(
                        getActivity(), UserSharedPreferences.USER_MODEL), UserModel.class);


    }

    private void setupSocket() {
        try {
            if(mSocket != null && mSocket.connected()) {
                mSocket.off();
                mSocket.disconnect();
            }

            mSocket = IO.socket(BuildConfig.SOCKET_URL);
            mSocket.emit("myConnection", new JSONObject("{\"userId\": " + mUserModel.getId() + "}"));

            mSocket.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (args != null) {
                        if (args.length > 0) {
                            Log.v(TAG, "New message received on Socket:\n" + args[0]);
                            MessagesFragment.this.onResume();
                        }
                    }
                }
            });

            mSocket.connect();

            Log.v(TAG, "Emitting message on myConnection: " + "{\"userId\": " + mUserModel.getId() + "}");
            Log.v(TAG, "Socket Connected on " + BuildConfig.SOCKET_URL + " : " + mSocket.connected());

        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.v(TAG, "Socket Error");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // this allows us to show custom menu for this fragment
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        messages_list_recycler_view = getView().findViewById(R.id.messages_list_recycler_view);
        messages_list_recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        messages_list_recycler_view.setAdapter(new MessagesListRecyclerAdapter(getActivity()));

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onPause() {
        super.onPause();

        mSocket.off();
        mSocket.disconnect();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void onLoadService() {
        //String url = BuildConfig.BASE_URL + "/" + mUserModel.getId() + "/" + "users_list" + "?user_id=" + mUserModel.getId();
        String url = BuildConfig.BASE_URL + "/" + mUserModel.getId() + "/" + "users_list";
        VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "main", true);
        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                if (!response.optBoolean("error", true)) {
                    List<MessagesUserGroupListItem> users_list = Utils.parseJsonArray(response.optJSONArray("user_list").toString(),
                            new TypeToken<List<MessagesUserGroupListItem>>() {
                            }.getType());
                    if (messages_list_recycler_view != null) {
                        ((MessagesListRecyclerAdapter) messages_list_recycler_view.getAdapter()).setDataNotify(users_list);
                    }
                    Log.d("", "");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // this allows us to show custom menu for this fragment
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.v(TAG, "onCreateOptionsMenu in messages fragment started...");
        getActivity().getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        LinearLayout searchEditFrame = (LinearLayout) mSearchView.findViewById(R.id.search_edit_frame); // Get the Linear Layout

        // Get the associated LayoutParams and set leftMargin
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) searchEditFrame.getLayoutParams();

//        ((LinearLayout.LayoutParams) searchEditFrame.getLayoutParams()).leftMargin = -26;
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                Log.v(TAG, "Search Text: " + newText);
                ((MessagesListRecyclerAdapter) messages_list_recycler_view.getAdapter()).performSearch(newText);
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        // for the custom actionbar menu of this fragment
        getActivity().invalidateOptionsMenu();
        onLoadService();

        // connecting socket...
        setupSocket();
    }
}
