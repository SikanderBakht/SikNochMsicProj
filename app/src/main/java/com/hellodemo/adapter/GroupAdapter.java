package com.hellodemo.adapter;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.hellodemo.R;
import com.hellodemo.interfaces.SendPopupAdaptersInterface;
import com.hellodemo.models.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by new user on 2/25/2018.
 */

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
    private List<Group> mGroupsSearchedList = new ArrayList<>();
    private List<Group> mGroupsOriginalList = new ArrayList<>();
    private AppCompatActivity mActivity;

    public GroupAdapter(AppCompatActivity activity) {
        mActivity = activity;
    }

    @Override
    public GroupAdapter.GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mActivity).inflate(R.layout.group_list_item, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupAdapter.GroupViewHolder holder, int position) {
        holder.bindValues(position);
    }

    @Override
    public int getItemCount() {
        return mGroupsSearchedList.size();
    }

    public void setDataNotify(List<Group> groupList) {
        mGroupsSearchedList.clear();
        mGroupsOriginalList.clear();
        mGroupsSearchedList.addAll(groupList);
        mGroupsOriginalList.addAll(groupList);
        notifyDataSetChanged();
    }

    public void performSearch(String newText) {

        newText = newText.toLowerCase(Locale.getDefault());
        mGroupsSearchedList.clear();
        if (newText.length() == 0) {
            mGroupsSearchedList.addAll(mGroupsOriginalList);
        } else {
            for (Group group : mGroupsOriginalList) {
                if (group.getName().toLowerCase(Locale.getDefault()).contains(newText)) {
                    mGroupsSearchedList.add(group);
                }
            }
        }
        notifyDataSetChanged();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView group_name_first_letter;
        private AppCompatCheckBox group_name;

        GroupViewHolder(View itemView) {
            super(itemView);
            group_name_first_letter = itemView.findViewById(R.id.group_name_first_letter);
            group_name = itemView.findViewById(R.id.group_name);
        }

        void bindValues(int position) {
            if (position >= mGroupsSearchedList.size()) return;
            Group group = mGroupsSearchedList.get(position);
            group_name.setText(group.getName());
            group_name_first_letter.setText(group.getName());
            itemView.setTag(group);
            group_name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    Group group = (Group) itemView.getTag();
                    if (isChecked) {
                        ((SendPopupAdaptersInterface) mActivity).selectGroup(group.getId());
                    } else {
                        ((SendPopupAdaptersInterface) mActivity).deselectGroup(group.getId());
                    }
                }
            });


            // if already sent to, lets disable that...
            if (group.isAlreadySent()) {
//                setImageDisable(recycler_view_list_item_image);
                group_name.setTextColor(Color.GRAY);
                group_name.setEnabled(false);
            }
        }
    }
}
