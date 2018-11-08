package com.hellodemo.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.hellodemo.R;
import com.hellodemo.interfaces.SendPopupAdaptersInterface;
import com.hellodemo.models.ArtistLabel;
import com.hellodemo.utils.PicassoGrayscaleTransformation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by new user on 2/25/2018.
 */

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistsViewHolder> {

    private List<ArtistLabel> mSelectedArtists = new ArrayList<>();
    private List<ArtistLabel> mOriginalArtists = new ArrayList<>();
    private AppCompatActivity mActivity;

    public ArtistAdapter(AppCompatActivity activity) {
        mActivity = activity;
    }

    @Override
    public ArtistsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.artist_label_list_item, parent, false);
        return new ArtistsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ArtistsViewHolder holder, int position) {
        holder.bindValues(position);
    }


    public int getItemCount() {
        return mSelectedArtists.size();
    }

    public void setDataNotify(List<ArtistLabel> artistList) {
        mSelectedArtists.clear();
        mOriginalArtists.clear();
        mSelectedArtists.addAll(artistList);
        mOriginalArtists.addAll(artistList);
        notifyDataSetChanged();
    }

    public void performSearch(String newText) {
        newText = newText.toLowerCase(Locale.getDefault());
        mSelectedArtists.clear();
        if (newText.length() == 0) {
            mSelectedArtists.addAll(mOriginalArtists);
        } else {
            for (ArtistLabel artistLabel : mOriginalArtists) {
                if (artistLabel.getFullName().toLowerCase(Locale.getDefault()).contains(newText)) {
                    mSelectedArtists.add(artistLabel);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ArtistsViewHolder extends RecyclerView.ViewHolder {
        private ImageView recycler_view_list_item_image;
        private AppCompatCheckBox artist_label_name;

        ArtistsViewHolder(View itemView) {
            super(itemView);
            artist_label_name = itemView.findViewById(R.id.artist_label_name);
            recycler_view_list_item_image = itemView.findViewById(R.id.recycler_view_list_item_image);
        }

        
        public void bindValues(int position) {
            if (position >= mSelectedArtists.size())
                return;

            final ArtistLabel artist = mSelectedArtists.get(position);
            Picasso.with(mActivity).load(artist.getAvatar()).into(recycler_view_list_item_image);
            artist_label_name.setText(artist.getFullName());
            itemView.setTag(artist);
            artist_label_name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {


                
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        ((SendPopupAdaptersInterface) mActivity).selectLabelArtist(artist.getId());
                    } else {
                        ((SendPopupAdaptersInterface) mActivity).deselectLabelArtist(artist.getId());
                    }
                }

            });


            // if already sent to, lets disable that...
            if(artist.isAlreadySent()){
                setImageDisable(recycler_view_list_item_image);
                artist_label_name.setTextColor(Color.GRAY);
                artist_label_name.setEnabled(false);
            }
        }

        public void uncheck() {
            artist_label_name.setChecked(false);
        }

        public void setImageDisable(ImageView v)
        {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);  //0 means grayscale
            ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
            v.setColorFilter(cf);
            v.setImageAlpha(128);   // 128 = 0.5
        }

    }

}
