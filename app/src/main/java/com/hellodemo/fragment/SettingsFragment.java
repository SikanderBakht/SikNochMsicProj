package com.hellodemo.fragment;

/**
 * Created by Mahnoor on 10/04/2018.
 */

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.Status;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.hellodemo.BuildConfig;
import com.hellodemo.Change_Password;
import com.hellodemo.MainActivity;
import com.hellodemo.R;
import com.hellodemo.StorageActivity;
import com.hellodemo.adapter.PlaceAutocompleteAdapter;
import com.hellodemo.adapter.Settings_Account_Adapter;
import com.hellodemo.adapter.Settings_LinkedAccounts_Adaptor;
import com.hellodemo.adapter.Settings_Social_Adaptor;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.NotificationSettingsActivity;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.LoadingManager;
import com.hellodemo.utils.Utils;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.UserAttributes;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment {
    //RelativeLayout rv;
    String newloc;
    PlaceAutocompleteFragment autocompleteFragment;
    AppCompatEditText new_name/*, new_location*/;

    // Settings Location Task
    AutoCompleteTextView new_location;
    protected GeoDataClient mGeoDataClient;
    private PlaceAutocompleteAdapter mAdapter;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    /////////////////////////////////
    private String TAG = "HelloDemoSettingsFragment";
    UserModel mUserModel;
    LoadingManager mainLoadingManger;
    public static final int RESULT_LOAD_IMAGE = 111, CAMERA = 211;
    com.mikhaellopez.circularimageview.CircularImageView image;
    ListView account_list_view, social_listview, linked_account_listview;
    TextView tvEmail, tvName;
    private String mCurrentPhotoPath;
    MainActivity mainActivityContext;
    Uri fileUri;
    File file;
    ImageView toolbar_music_recycler_view_list_item_image;
    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(MainActivity mainActivityContext) {

        SettingsFragment fragment = new SettingsFragment();
        fragment.mainActivityContext = mainActivityContext;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mUserModel = Utils.parseJson(UserSharedPreferences.getString(getActivity(), UserSharedPreferences.USER_MODEL), UserModel.class);
        // this allows us to show custom menu for this fragment
        setHasOptionsMenu(true);

        mGeoDataClient = Places.getGeoDataClient(mainActivityContext, null);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.settings_screen, container, false);

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        image = getView().findViewById(R.id.img_profile_settings);

        tvName = getView().findViewById(R.id.full_name);
        tvName.setText(mUserModel.getFullName());

        tvEmail = getView().findViewById(R.id.textemail);
        tvEmail.setText(mUserModel.getEmail());



        // will contain all the content of the fragment other than loader_animator...
        View fragmentContent = view.findViewById(R.id.fragment_content11);

        //loading icon...
//        GifImageView mGigImageView = getView().findViewById(R.id.loading_gif);
        ImageView loadingIcon = (ImageView) view.findViewById(R.id.loading_icon1);

        // This container contains the loading icon
        View loadingContainer = view.findViewById(R.id.loading_container);

        // setup loading icon...
        mainLoadingManger = new LoadingManager(getContext(), fragmentContent, loadingIcon, loadingContainer);



        Picasso.with(getActivity()).load(mUserModel.getAvatar()).into(image);

        String[] Account_Options = {/*"Subscription", */"Display Name", "Password", "Location", "Storage", "Notification", "Ask us a Question"};
        int[] drawableIds = {
//                R.drawable.subscription,
                R.drawable.changedisplayname,
                R.drawable.changepassword,
                R.drawable.location,
                R.drawable.storage,
                R.drawable.notifications,
                R.drawable.help_center
        };
        Settings_Account_Adapter adapter = new Settings_Account_Adapter(getActivity(), Account_Options, drawableIds);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                int permissionCheckStorage = ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.CAMERA);

                // we already asked for permisson & Permission granted, call camera intent
                if (permissionCheckStorage == PackageManager.PERMISSION_GRANTED) {
// for fragment (DO NOT use `getActivity()`)
                    CropImage.activity()
                            .start(getContext(), SettingsFragment.this);

                    //do what you want

                } else {

                    // if storage request is denied
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.CAMERA)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("You need to give for camera.");
                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setPositiveButton("GIVE PERMISSION", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                // Show permission request popup
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.CAMERA},
                                        CAMERA);
                            }
                        });
                        builder.show();

                    } //asking permission for first time
                    else {

                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                CAMERA);

                    }

                }


            }
        });

        account_list_view = getView().findViewById(R.id.account_listview);
        account_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);

                Log.v("On listclicked", "clicked");
                if (i == 0) { // name
                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(getContext());
                    View promptsView = li.inflate(R.layout.custom_popup_changename, null);
                    new_name = promptsView.findViewById(R.id.newname);
                    if(!mUserModel.getFullName().isEmpty()) {
                        new_name.setText(mUserModel.getFullName());
                    } else {
                        new_name.setText(mUserModel.getUsername());
                    }


                    View save = promptsView.findViewById(R.id.Save);
                    View cancel = promptsView.findViewById(R.id.Cancel);

                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getContext());

                    // retrieve display dimensions
                    Rect displayRectangle = new Rect();
                    Window window = getActivity().getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

                    promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.7f));
                    promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.3f));

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);
                    // create alert dialog
                    final AlertDialog alertDialog = alertDialogBuilder.create();

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // get user input and set it to result

                            final ProgressDialog pd = new ProgressDialog(getContext());
                            pd.setMessage("Changing Name. Please Wait!");
                            pd.show();

                            String strToPass = "";

                            if(!new_name.getText().toString().isEmpty()) {
                                strToPass = new_name.getText().toString().trim();
                            } else {
                                strToPass = mUserModel.getUsername();
                            }
                            String url = "";
                            url = BuildConfig.BASE_URL + "/name/change";

                            VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "Change_Name", true);
                            HashMap<String, String> params = new HashMap<>();
                            params.put("user_id", String.valueOf(mUserModel.getId()));
                            params.put("new_name", strToPass);//new_name.getText().toString());*/

                            Log.v(TAG, "Change Name API Called:\n"
                                    + "URL: " + url + "\n"
                                    + "Params: " + params.toString());

                            volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.v(TAG, "Change Password API Called:\n"
                                            + response.toString());


                                    if (!response.optBoolean("error", true)) {
//                                        Toast.makeText(getActivity(), "Successfully saved", Toast.LENGTH_LONG).show();
                                        mUserModel.setFullName(new_name.getText().toString().trim());
                                        tvName.setText(mUserModel.getFullName());
                                        CustomToast.makeText(getActivity(), "Successfully saved", Toast.LENGTH_LONG).show();

                                        UserAttributes userAttributes = new UserAttributes.Builder().withName(new_name.getText().toString().trim()).build();
                                        Intercom.client().updateUser(userAttributes);

                                        // Saving the data to shared preferences as well
                                        Gson gson = new Gson();
                                        UserSharedPreferences.saveString(
                                                getActivity(),
                                                UserSharedPreferences.USER_MODEL,
                                                gson.toJson(mUserModel)
                                        );
                                    }
                                    pd.dismiss();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    CustomToast.makeText(getActivity(), "Error Occurred!", Toast.LENGTH_LONG).show();
                                    pd.dismiss();
                                }
                            });
                            alertDialog.dismiss();
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Intent intent = new Intent(getActivity(),SettingsFragment);
                            // startActivity(intent);
                            alertDialog.dismiss();
                        }
                    });

                    // show it
                    alertDialog.show();

                }

                if (i == 1) { // Password
                    startActivity(new Intent(getActivity(), Change_Password.class));

                }

                if (i == 2) { //location
                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(getContext());
                    View promptsViewlocation = li.inflate(R.layout.custom_popup_changelocation, null);

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    mainActivityContext.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;

                    //rv = promptsViewlocation.findViewById(R.id.rlSearchPlacement);
                    new_location = promptsViewlocation.findViewById(R.id.location);
                    new_location.setDropDownHeight(height/5);
//                    new_location.setDropDownWidth(width-(width/8));
                    new_location.setDropDownVerticalOffset(10);
//                    new_location.setDropDownHorizontalOffset(-(width/21));
                    new_location.setText(mUserModel.getLocation());
                    //new_location.setInputType(InputType.TYPE_NULL);

                    new_location.setOnItemClickListener(mAutocompleteClickListener);
                    mAdapter = new PlaceAutocompleteAdapter(mainActivityContext, mGeoDataClient, null, null);
                    new_location.setAdapter(mAdapter);

                    View save = promptsViewlocation.findViewById(R.id.Save);
                    View cancel = promptsViewlocation.findViewById(R.id.Cancel);
                    // create alert dialog



                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getContext());
                    alertDialogBuilder.setView(promptsViewlocation);
                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // get user input and set it to result

                            final ProgressDialog pd = new ProgressDialog(getContext());
                            pd.setMessage("Changing Location. Please Wait!");
                            pd.show();

                            String url = "";
                            url = BuildConfig.BASE_URL + "/location/change";

                            VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "Change_Name", true);
                            HashMap<String, String> params = new HashMap<>();
                            params.put("user_id", "1");
                            params.put("new_location", new_location.getText().toString());//new_name.getText().toString());*/

                            Log.v(TAG, "Change Location API Called:\n"
                                    + "URL: " + url + "\n"
                                    + "Params: " + params.toString());

                            volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.v(TAG, "Change Location API Called:\n"
                                            + response.toString());


                                    if (!response.optBoolean("error", true)) {
                                        CustomToast.makeText(getActivity(), "Successfully saved", Toast.LENGTH_LONG).show();

                                        mUserModel.setLocation(new_location.getText().toString().trim());

                                        UserAttributes userAttributes = new UserAttributes.Builder()
                                                .withCustomAttribute("location", new_location.getText().toString().trim())
                                                .build();
                                        Intercom.client().updateUser(userAttributes);

                                        // Saving the data to shared preferences as well
                                        Gson gson = new Gson();
                                        UserSharedPreferences.saveString(
                                                getActivity(),
                                                UserSharedPreferences.USER_MODEL,
                                                gson.toJson(mUserModel)
                                        );
                                    }
                                    pd.dismiss();
                                    alertDialog.dismiss();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    CustomToast.makeText(getActivity(), "Error Occurred!", Toast.LENGTH_LONG).show();
                                    pd.dismiss();
                                }
                            });
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            alertDialog.dismiss();

                        }
                    });
                    // show it
                    alertDialog.show();
                }

                if (i == 3) {  // storage

                    startActivity(new Intent(getActivity(), StorageActivity.class));
                }

                if (i == 4) { // NotificationsActivity

                    startActivity(new Intent(getActivity(), NotificationSettingsActivity.class));
                }
                if (i == 5) { // Help Center
                    if(BuildConfig.BASE_URL.equals("https://beta.hello-demo.com/api") || BuildConfig.BASE_URL.equals("https://web.hello-demo.com/api")) {
                        Intercom.client().displayMessenger();
                    }
                }
            }

        });
        account_list_view.setAdapter(adapter);


        String[] Social_Profile_Option = {
                "Facebook",
                "Instagram",
                "SoundCloud",
                "Twitter",
                "Youtube",
                "BeatPort"
        };

        int[] drawableIds_social = {
                R.drawable.facebook,
                R.drawable.label,
                R.drawable.soundcloud,
                R.drawable.twitter,
                R.drawable.youtube,
                R.drawable.beatport
        };

        Settings_Social_Adaptor adapter_Social_Acounts = new Settings_Social_Adaptor(getActivity(), Social_Profile_Option, drawableIds_social);
        social_listview = getView().findViewById(R.id.social_accounts_listview);
        social_listview.setAdapter(adapter_Social_Acounts);

        String[] Social_LinkedAccount_Option = {"Link Artist Account", "Link Label Account"};
        int[] drawableIds_account = {
                R.drawable.subscription,
                R.drawable.label
        };
        Settings_LinkedAccounts_Adaptor adapter_link_Acounts = new Settings_LinkedAccounts_Adaptor(getActivity(), Social_LinkedAccount_Option, drawableIds_account);
        linked_account_listview = getView().findViewById(R.id.linked_account_listview);
        linked_account_listview.setAdapter(adapter_link_Acounts);

        linked_account_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);


                if (i == 0) { // link artists account
                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(getContext());
                    View promptsView = li.inflate(R.layout.custom_popup_linkanartistaccount, null);
                    new_name = promptsView.findViewById(R.id.newname);

                    View save = promptsView.findViewById(R.id.Save);
                    View cancel = promptsView.findViewById(R.id.Cancel);

                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getContext());

                    // retrieve display dimensions
                    Rect displayRectangle = new Rect();
                    Window window = getActivity().getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

                    promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.7f));
                    promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.3f));

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);
                    // create alert dialog
                    final AlertDialog alertDialog = alertDialogBuilder.create();

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Intent intent = new Intent(getActivity(),SettingsFragment);
                            // startActivity(intent);
                            alertDialog.dismiss();
                        }
                    });

                    // show it
                    alertDialog.show();

                }

                if (i == 1) { // link a label account
                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(getContext());
                    View promptsView = li.inflate(R.layout.custom_popup_linklabelaccount, null);

                    View save = promptsView.findViewById(R.id.Save);
                    View cancel = promptsView.findViewById(R.id.Cancel);

                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getContext());

                    // retrieve display dimensions
                    Rect displayRectangle = new Rect();
                    Window window = getActivity().getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

                    promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.7f));
                    promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.3f));

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);
                    // create alert dialog
                    final AlertDialog alertDialog = alertDialogBuilder.create();

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Intent intent = new Intent(getActivity(),SettingsFragment);
                            // startActivity(intent);
                            alertDialog.dismiss();
                        }
                    });

                    // show it
                    alertDialog.show();

                }
            }
        });

        social_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);

                if (i == 5) { // beatport account

                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(getContext());
                    View promptsView = li.inflate(R.layout.custom_popup_beatport, null);

                    View save = promptsView.findViewById(R.id.Save);
                    View cancel = promptsView.findViewById(R.id.Cancel);

                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getContext());

                    // retrieve display dimensions
                    Rect displayRectangle = new Rect();
                    Window window = getActivity().getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

                    promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.7f));
                    promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.3f));

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);
                    // create alert dialog
                    final AlertDialog alertDialog = alertDialogBuilder.create();

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Intent intent = new Intent(getActivity(),SettingsFragment);
                            // startActivity(intent);
                            alertDialog.dismiss();
                        }
                    });

                    // show it
                    alertDialog.show();

                }
            }
        });

        // this code will fix list view height issue
        setListViewHeightBasedOnChildren(account_list_view);
        setListViewHeightBasedOnChildren(social_listview);
        setListViewHeightBasedOnChildren(linked_account_listview);

        }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // this allows us to show custom menu for this fragment
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public void onResume() {
        super.onResume();
        // for the custom actionbar menu of this fragment
        getActivity().invalidateOptionsMenu();
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data Client to retrieve a Place object with
             additional details about the place.
              */
            Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);
                places.release();
            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete.", e);
                return;
            }
        }
    };

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri imageUri = data.getData();

            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri)
                    .start(getActivity());



        } else if (requestCode == CAMERA) {

            // start cropping activity for pre-acquired image saved on the device
         //   CropImage.activity(fileUri)
           //         .start(getActivity());
            // for fragment (DO NOT use `getActivity()`)
            CropImage.activity()
                    .start(getContext(), this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                image.setImageURI(resultUri);
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                final InputStream imageStream;
                try {

                    imageStream = getActivity().getContentResolver().openInputStream(resultUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    mainLoadingManger.showLoadingIcon();
                    SaveProfilePictureToServer(selectedImage);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void choosePhotoFromGallary() {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
    }

    private void takePhotoFromCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(getActivity().getExternalCacheDir(),
                String.valueOf(System.currentTimeMillis()) + ".jpg");
        fileUri = Uri.fromFile(file);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        getActivity().startActivityFromFragment(this, cameraIntent, CAMERA);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.CAMERA)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //do what you want;

                        CropImage.activity()
                                .start(getContext(), this);
                      /*  AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getContext());
                        pictureDialog.setTitle("Select Action");
                        String[] pictureDialogItems = {
                                "Select photo from gallery",
                                "Capture photo from camera"};
                        pictureDialog.setItems(pictureDialogItems,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                choosePhotoFromGallary();
                                                break;
                                            case 1:
                                                takePhotoFromCamera();
                                                break;
                                        }
                                    }
                                });
                        pictureDialog.show();

                    }*/
                        //}
                        //}
                    }
                }
        }
    }

    private void SaveProfilePictureToServer(final Bitmap bmp) {


        // Converting Bitmap to ByteArray...
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();


        String url = BuildConfig.BASE_URL + "/user/change_profile_picture";

        Log.v(TAG, "Change Profile Pic API URL : " + url);

        // Change base URL to your upload server URL.
        OkHttpClient client = new OkHttpClient();
        Service service = new Retrofit
                .Builder()
                .baseUrl(BuildConfig.BASE_URL + "/")
                .client(client)
                .build()
                .create(Service.class);

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), byteArray);
        MultipartBody.Part body = MultipartBody.Part.createFormData("profile_picture", "file_name", reqFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");


        String accessToken = Utils.getAccessToken(getActivity());

        String auth = "Bearer " + accessToken;

        retrofit2.Call<okhttp3.ResponseBody> req = service.postImage(body, name, auth, "application/json");
        req.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {


                if (response.body() != null) {
                    try {
                        
                        String responseString = response.body().string();
                        Log.v(TAG, "Change Profile Pic API Response: " + responseString);
                        JSONObject jObj = new JSONObject(responseString);
                        if (jObj.has("error")) {
                            if (!jObj.getBoolean("error")) {
                                mainLoadingManger.hideLoadingIcon();
                                CustomToast.makeText(getActivity(), "Picture Is Changed", Toast.LENGTH_LONG).show();

                 //               image.setImageBitmap(bmp);
                                mUserModel.setAvatar(jObj.getString("image_url"));
                                // Saving the data to shared preferences as well
                                Gson gson = new Gson();
                                UserSharedPreferences.saveString(
                                        getActivity(),
                                        UserSharedPreferences.USER_MODEL,
                                        gson.toJson(mUserModel)
                                );
                             
                                // update gui as well...
                                mainActivityContext.bindValues();

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        mainLoadingManger.hideLoadingIcon();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mainLoadingManger.hideLoadingIcon();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                Log.v(TAG, "Change Profile Pic API onFailure: " + t.getLocalizedMessage());
            }
        });

    }

}



interface Service {
    @Multipart
    @POST("user/change_profile_picture")
    Call<ResponseBody> postImage(@Part MultipartBody.Part image, @Part("name") RequestBody name, @Header("Authorization") String authorization, @Header("Accept") String accept);
}