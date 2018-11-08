package com.hellodemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.hellodemo.ChatActivity;
import com.hellodemo.R;
import com.hellodemo.interfaces.ChatSongItemInterfaceListener;
import com.hellodemo.interfaces.MusicListInterfaceListener;
import com.hellodemo.interfaces.UIConstants;
import com.hellodemo.models.MessagesUser;
import com.hellodemo.models.MessagesUserGroupListItem;
import com.hellodemo.models.MusicListItem;
import com.hellodemo.models.Song;
import com.hellodemo.utils.CustomToast;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by new user on 2/24/2018.
 */

public class MessagesBySongsRecyclerAdapter extends RecyclerSwipeAdapter<MessagesBySongsRecyclerAdapter.MessagesBySongsItemAdapter> {

    private String TAG = "HelloDemoMessagesBySongsRecyclerAdapter";

    ChatSongItemInterfaceListener context;
    Context appContext;
    //private AppCompatActivity mActivity;
    private MessagesUser mMessagesUser;
    private MessagesUserGroupListItem targetUser;
    private ArrayList<Song> mSearchedSongs = new ArrayList<>();
    private ArrayList<Song> mOriginalSongs = new ArrayList<>();

    public MessagesBySongsRecyclerAdapter(ChatSongItemInterfaceListener context, Context appContext) {
        this.context = context;
        this.appContext = appContext;
    }

//    public MessagesBySongsRecyclerAdapter(AppCompatActivity appCompatActivity) {
//        mActivity = appCompatActivity;
//    }

    @Override
    public MessagesBySongsRecyclerAdapter.MessagesBySongsItemAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        SwipeLayout swipeLayout = (SwipeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_messages_list_by_song,parent, false);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.left_wrapper));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewById(R.id.right_wrapper));

        return new MessagesBySongsItemAdapter(swipeLayout);
    }

    @Override
    public void onBindViewHolder(MessagesBySongsRecyclerAdapter.MessagesBySongsItemAdapter viewHolder, final int position) {
        viewHolder.onBindView(position);
        /*mItemManger.bindView(viewHolder.itemView, position);
        ((SwipeLayout) viewHolder.itemView).addSwipeListener(new SimpleSwipeListener() {
            public void onStartOpen(SwipeLayout layout) {
                super.onStartOpen(layout);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        if (mMessagesUser == null || mSearchedSongs == null) return 0;
        return mSearchedSongs.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.chat_music_recycler_view_swipe_layout;
    }

    public void setDataNotify(MessagesUser messagesUser) {
//        Log.d(TAG, "songs found: " + messagesUser.getSongs().size());

        mMessagesUser = messagesUser;
        mSearchedSongs.clear();
        mOriginalSongs.clear();
        mSearchedSongs.addAll(messagesUser.getSongs());
        mOriginalSongs.addAll(messagesUser.getSongs());
        notifyDataSetChanged();
    }

    public void setTargetUser(MessagesUserGroupListItem targetUser) {
        this.targetUser = targetUser;
    }

    public void performSearch(String newText) {
        newText = newText.toLowerCase(Locale.getDefault());
        mSearchedSongs.clear();
        if (newText.length() == 0) {
            mSearchedSongs.addAll(mOriginalSongs);
        } else {
            for (Song song : mOriginalSongs) {
                if (song.getTitle().toLowerCase(Locale.getDefault()).contains(newText)) {
                    mSearchedSongs.add(song);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void notifyMessageBySongsItemRemoved(Song item) {
        int position = mSearchedSongs.indexOf(item);
        if (position != -1) {
            mSearchedSongs.remove(position);
            notifyItemRemoved(position);
        }
    }

    class MessagesBySongsItemAdapter extends RecyclerView.ViewHolder {
        View archive, delete, surface_view;
        private TextView recycler_view_list_item_title, unread_messages_count, last_message;
        private ImageView recycler_view_list_item_image;

        MessagesBySongsItemAdapter(View itemView) {
            super(itemView);

            surface_view = itemView.findViewById(R.id.surface_view);
            archive = itemView.findViewById(R.id.archive);
            delete = itemView.findViewById(R.id.delete);

            recycler_view_list_item_image = itemView.findViewById(R.id.recycler_view_list_item_image);
            recycler_view_list_item_title = itemView.findViewById(R.id.recycler_view_list_item_title);
            unread_messages_count = itemView.findViewById(R.id.unread_messages_count);
            last_message = itemView.findViewById(R.id.last_message);
        }

        void onBindView(int position) {
            if (mSearchedSongs == null || mOriginalSongs == null)
                return;

            Song song = mSearchedSongs.get(position);

            Picasso.with(appContext).load(mMessagesUser.getSenderImage()).into(recycler_view_list_item_image);
            recycler_view_list_item_title.setText(song.getTitle());
            if (song.getUnreadMessages() > 0) {
                //unread_messages_count.setText(String.valueOf(song.getUnreadMessages()));
                unread_messages_count.setVisibility(View.VISIBLE);
                recycler_view_list_item_title.setTypeface(recycler_view_list_item_title.getTypeface(), Typeface.BOLD);
                last_message.setTextColor(appContext.getResources().getColor(R.color.md_black_1000));
            } else {
                unread_messages_count.setVisibility(View.INVISIBLE);
                recycler_view_list_item_title.setTypeface(recycler_view_list_item_title.getTypeface(), Typeface.NORMAL);
                last_message.setTextColor(appContext.getResources().getColor(R.color.txt_secondary));
            }
            last_message.setText(song.getLastMessage());
            itemView.setTag(song);
            /*itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Song song = (Song) view.getTag();
                    Intent intent = new Intent(mActivity, ChatActivity.class);

                    // setting action
                    if (song.getType().equalsIgnoreCase(Song.TYPE_PACKAGE)) {
                        intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_PACKAGE);
                    } else if (song.getType().equalsIgnoreCase(Song.TYPE_TRACK)) {
                        intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_SONG);
                    }
                    else {
                        Toast.makeText(mActivity, "unknown tye", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // passing data
                    intent.putExtra(ChatActivity.KEY_TARGET_USER_ID, targetUser.getId());
                    intent.putExtra(ChatActivity.KEY_TARGET_USER_FULLNAME, targetUser.getFullName());
                    intent.putExtra(ChatActivity.KEY_MUSIC_UPLOAD_ID, song.getId());
                    intent.putExtra(ChatActivity.KEY_MUSIC_TITLE, song.getTitle());

                    mActivity.startActivity(intent);
                }
            });*/

            surface_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    (context).onClickSurface((Song) itemView.getTag());
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    (context).onClickDeleteMusic((Song) itemView.getTag());
                }
            });

            archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    (context).onClickArchiveMusic((Song) itemView.getTag());
                }
            });
        }
    }
}
