package com.hellodemo.adapter;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hellodemo.R;
import com.hellodemo.interfaces.ScreenInterfaceListener;
import com.hellodemo.models.NavGroupItem;
import com.hellodemo.models.NavMenuModel;

/**
 * Created by new user on 2/17/2018.
 */

public class NavGroupAdapter extends RecyclerView.Adapter<NavGroupAdapter.NavGroupViewHolder> {

    AppCompatActivity mActivity;
    private int selectedItemPosition = -1;
    private NavMenuModel data;

    public NavGroupAdapter(AppCompatActivity appCompatActivity) {
        mActivity = appCompatActivity;
    }

    @Override
    public NavGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nav_menu, parent, false);
        return new NavGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NavGroupViewHolder holder, int position) {
        holder.onBindView(position);
    }

    @Override
    public int getItemCount() {
        if (data == null || data.getCloseGroups() == null) return 0;
        return data.getCloseGroups().size();
    }

    public void setData(NavMenuModel data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
        notifyDataSetChanged();
    }

    public void setSelectedGroup(NavGroupItem group) {
        if (group == null)
            selectedItemPosition = -1;
        else
            selectedItemPosition = data.getCloseGroups().indexOf(group);
        notifyDataSetChanged();
    }

    public class NavGroupViewHolder extends RecyclerView.ViewHolder {
        TextView screen_title, screen_nav_count;

        public NavGroupViewHolder(View itemView) {
            super(itemView);
            screen_title = itemView.findViewById(R.id.screen_title);
            screen_nav_count = itemView.findViewById(R.id.screen_nav_count);
        }

        public void onBindView(int position) {
            if (data == null && data.getCloseGroups() == null) return;

            if (position == selectedItemPosition) {
                itemView.setSelected(true);
            } else {
                itemView.setSelected(false);
            }


            NavGroupItem group = data.getCloseGroups().get(position);
            screen_title.setText(group.getName());
            screen_nav_count.setText(String.format("(%d)", group.getMusicNum()));
            itemView.setTag(group);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedItemPosition = getAdapterPosition();
                    notifyDataSetChanged();
                    NavGroupItem group = (NavGroupItem) view.getTag();
                    if (mActivity != null) {
                        ((ScreenInterfaceListener) mActivity).setActivityNameNull();
                        ((ScreenInterfaceListener) mActivity).selectedGroup(group);
                    }
                }
            });
        }
    }
}
