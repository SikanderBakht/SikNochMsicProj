package com.hellodemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.hellodemo.MainActivity;
import com.hellodemo.R;
import com.hellodemo.interfaces.MusicListInterfaceListener;
import com.hellodemo.models.MusicListItem;
import com.hellodemo.models.NavGroupItem;
import com.hellodemo.models.UserModel;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import dm.audiostreamer.AudioStreamingService;

/**
 * Created by new user on 2/16/2018.
 */

public class MusicRecyclerViewAdapter extends RecyclerSwipeAdapter<MusicRecyclerViewAdapter.MusicItemAdapter> {

    private String TAG = "HelloDemoMusicRecyclerViewAdapter";

    // KEYS
    public static final int KEY_INBOX_SCREEN = 0;
    public static final int KEY_UPLOADS_SCREEN = 1;
    public static final int KEY_FAVORITES_SCREEN = 2;
    public static final int KEY_GROUPS_SCREEN = 3;
    public static final int KEY_CHAT_SCREEN = 4;
    MusicListInterfaceListener context;
    MainActivity contextActivity;
    Context appContext;
    private List<MusicListItem> data = new ArrayList<>();
    private int selectedScreen;
    private NavGroupItem mGroup = null;
    private boolean showBorder = false;
    AudioStreamingService s = new AudioStreamingService();
    // this holds the index of currently selected item in the list...
    // we use it to highlight the title...
    private int selectedItemIndex = 0;
    MusicListItem item;

    public MusicRecyclerViewAdapter(MusicListInterfaceListener context, Context appContext) {
        this.context = context;
        this.appContext = appContext;
    }

    @Override
    public MusicRecyclerViewAdapter.MusicItemAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        SwipeLayout swipeLayout = (SwipeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_recycler_view_list_item,
                        parent, false);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.left_wrapper));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewById(R.id.right_wrapper));

        return new MusicItemAdapter(swipeLayout);
    }

    @Override
    public void onBindViewHolder(MusicRecyclerViewAdapter.MusicItemAdapter viewHolder, int position) {
        viewHolder.onBindView(position);
        /*mItemManger.bindView(viewHolder.itemView, position);
        ((SwipeLayout) viewHolder.itemView).addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                mItemManger.closeAllExcept(layout);
            }
        });*/
    }

    public int getItemCount() {
        if (data == null) return 0;
        return data.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.music_recycler_view_swipe_layout;
    }

    public void setSelectedItem(MusicListItem item) {
        int newSelectedPosition = data.indexOf(item);

        if (newSelectedPosition != -1) {

            int prevSelectedPosition = this.selectedItemIndex;
            this.selectedItemIndex = newSelectedPosition;

            // we have to notify both items so highlight is removed on added respectively...
            notifyItemChanged(newSelectedPosition);
            notifyItemChanged(prevSelectedPosition);
        }
    }

    // returns position of the MusicListItem in adapter model array
    public int getPosition(MusicListItem item) {
        return data.indexOf(item);
    }

    public void clearRecyclerView() {
        data.clear();
        notifyDataSetChanged();
    }

    public void setData(List<MusicListItem> data, NavGroupItem group, boolean finalShowBorder, int selectedScreen) {
        this.data.clear();
        this.data.addAll(data);
        this.mGroup = group;
        this.showBorder = finalShowBorder;
        this.selectedScreen = selectedScreen;
        notifyDataSetChanged();
    }

    /**
     * This method will remove MusicListItem item from the RecyclerView
     *
     * @param item
     */
    public void notifyMusicItemRemoved(MusicListItem item) {
        int position = data.indexOf(item);
        if (position != -1) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }

    class MusicItemAdapter extends RecyclerView.ViewHolder {
        View favorite, forward, message, delete, surface_view, notes;
        CircularImageView music_recycler_view_list_item_image, music_recycler_view_list_item_bg_image;
        AppCompatTextView music_recycler_view_list_item_title, music_recycler_view_list_item_tag, txt_favourite;
        ImageView favourite_image_view;
        View unreadNotificationView;

        MusicItemAdapter(View itemView) {
            super(itemView);

            surface_view = itemView.findViewById(R.id.surface_view);
            favorite = itemView.findViewById(R.id.favorite);
            forward = itemView.findViewById(R.id.forward);
            message = itemView.findViewById(R.id.message);
            delete = itemView.findViewById(R.id.delete);
            notes = itemView.findViewById(R.id.notes);
            favourite_image_view = itemView.findViewById(R.id.favourite_image_view);
            txt_favourite = itemView.findViewById(R.id.txt_favourite);

            music_recycler_view_list_item_image = itemView.findViewById(R.id.notification_avatar);
            music_recycler_view_list_item_bg_image = itemView.findViewById(R.id.music_recycler_view_list_item_bg_image);
            music_recycler_view_list_item_title = itemView.findViewById(R.id.notification_text);
            music_recycler_view_list_item_tag = itemView.findViewById(R.id.notification_subtext);
            unreadNotificationView = itemView.findViewById(R.id.unreadNotificationView);
        }

        void onBindView(int position) {
            if (data == null)
                return;

            item = data.get(position);
            itemView.setTag(item);

            setVisibility(position);

            // if item at this position is the currently selected item...
            // we highlight it by making the title bold...
            if (selectedItemIndex == position) {
                music_recycler_view_list_item_title.setTypeface(null, Typeface.BOLD);
                // music_recycler_view_list_item_title.setMovementMethod(new ScrollingMovementMethod());
                music_recycler_view_list_item_title.setSelected(true);
                //music_recycler_view_list_item_title.setEnabled(true);
            } else {

                music_recycler_view_list_item_title.setSelected(false);
                music_recycler_view_list_item_title.setTypeface(null, Typeface.NORMAL);

            }

            music_recycler_view_list_item_title.setText(item.getTitle());

            //      music_recycler_view_list_item_title.setMovementMethod(new ScrollingMovementMethod());

            music_recycler_view_list_item_tag.setText(item.getArtist());


            Picasso.with(context.getCallingContext()).load(item.getAvatar()).into(music_recycler_view_list_item_image);
            if (item.getFavorite() == 0) {
                favourite_image_view.setImageResource(R.drawable.ic_favorite_border_white_18dp);
                txt_favourite.setText(R.string.favorite);
            } else {
                favourite_image_view.setImageResource(R.drawable.ic_favorite_white_18dp);
                txt_favourite.setText(R.string.favorited);
            }
            if (item.getBorderColor() != null && !item.getBorderColor().isEmpty() && showBorder) {
                music_recycler_view_list_item_bg_image.setBorderColor(Color.parseColor(item.getBorderColor()));
                music_recycler_view_list_item_bg_image.setBorderWidth(3);
                music_recycler_view_list_item_image.setBorderWidth(3);
            } else {
                music_recycler_view_list_item_bg_image.setBorderColor(
                        Utils.getColor(context.getCallingContext(), android.R.color.white));
                music_recycler_view_list_item_bg_image.setBorderWidth(0);
                music_recycler_view_list_item_image.setBorderWidth(0);
            }
//            if (!showMessageButton) {
//                message.setVisibility(View.GONE);
////                favorite.setVisibility(View.VISIBLE);
//
//            } else {
//                message.setVisibility(View.VISIBLE);
//            }

            // we also need to show unread notification if music has not been played yet...
            // Log.v(TAG, "Testtt item check value = "+ item.getCheck());
            if (item.getCheck() == 0) {
                unreadNotificationView.setVisibility(View.VISIBLE);
            } else {
                unreadNotificationView.setVisibility(View.GONE);
            }

            surface_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    (context).onClickPlayMusic((MusicListItem) itemView.getTag());
                }
            });

            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    (context).onClickFavoriteMusic((MusicListItem) itemView.getTag(), mGroup);
                }
            });

            forward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    (context).onClickForwardMusic((MusicListItem) itemView.getTag());
                }
            });
            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    (context).onClickMessageMusic((MusicListItem) itemView.getTag());
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View view) {
                    (context).onClickDeleteMusic((MusicListItem) itemView.getTag(), mGroup);
                }
            });

            notes.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View view) {
                    (context).onClickNotesMusic((MusicListItem) itemView.getTag());
                }
            });
        }

        private void setVisibility(int position) {

            // initially we will keep everything as gone....
            delete.setVisibility(View.GONE);
            forward.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
            favorite.setVisibility(View.GONE);
            notes.setVisibility(View.GONE);

            switch (selectedScreen) {
                // inbox
                case KEY_INBOX_SCREEN:
                    forward.setVisibility(View.GONE);
                    message.setVisibility(View.VISIBLE);
                    favorite.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    notes.setVisibility(View.GONE);
                    break;
                case KEY_CHAT_SCREEN:
                    forward.setVisibility(View.GONE);
                    message.setVisibility(View.VISIBLE);
                    favorite.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    notes.setVisibility(View.GONE);
                    break;
                // Uploads
                case KEY_UPLOADS_SCREEN:
                    forward.setVisibility(View.VISIBLE);
                    message.setVisibility(View.GONE);
                    favorite.setVisibility(View.GONE);
                    delete.setVisibility(View.VISIBLE);
                    notes.setVisibility(View.VISIBLE);
                    break;


                //Favorites
                case KEY_FAVORITES_SCREEN:
                    forward.setVisibility(View.GONE);
                    message.setVisibility(View.VISIBLE);
                    favorite.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.GONE);
                    notes.setVisibility(View.GONE);
                    break;


                // Groups
                case KEY_GROUPS_SCREEN:
                    delete.setVisibility(View.GONE);
                    forward.setVisibility(View.GONE);
                    message.setVisibility(View.GONE);
                    favorite.setVisibility(View.GONE);
                    notes.setVisibility(View.GONE);

                    // in case of close groups, delete is always available...
                    if (mGroup.getType().equals("close")) {

                        delete.setVisibility(View.VISIBLE);
                    }
                    // in case of open groups...
                    else if (mGroup.getType().equals("open")) {


                        long userID = Utils.parseJson(UserSharedPreferences.getString(appContext,
                                UserSharedPreferences.USER_MODEL), UserModel.class).getId();

                        // if user is the owner of the group,
                        // or the uploader of the track....
                        if ((userID == mGroup.getCreater())
                                || (userID == data.get(position).getUploaderId())) {

                            delete.setVisibility(View.VISIBLE);
                        }

                        // if user didn't upload the track, we let him make it favorite
                        if (userID != data.get(position).getUploaderId()) {

                            favorite.setVisibility(View.VISIBLE);
                        }

                    }
                    break;


                default:
                    break;
            }
        }
    }
}
