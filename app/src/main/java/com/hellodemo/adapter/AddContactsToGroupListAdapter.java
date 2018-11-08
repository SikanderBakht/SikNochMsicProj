package com.hellodemo.adapter;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hellodemo.AddContactsToGroupActivity;
import com.hellodemo.R;
import com.hellodemo.models.ContactToAddInGroupModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddContactsToGroupListAdapter extends RecyclerView.Adapter<AddContactsToGroupListAdapter.ContactToAddInGroupViewHolder> {

    private List<ContactToAddInGroupModel> mSelectedContacts = new ArrayList<>();
    private List<ContactToAddInGroupModel> mOriginalContacts = new ArrayList<>();
    private AddContactsToGroupActivity mActivity;

    public AddContactsToGroupListAdapter(AddContactsToGroupActivity activity) {
        mActivity = activity;
    }

    @Override
    public ContactToAddInGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.contact_to_add_in_group, parent, false);
        return new ContactToAddInGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactToAddInGroupViewHolder holder, int position) {
        holder.bindValues(position);
    }

    @Override
    public int getItemCount() {
        return mSelectedContacts.size();
    }

    public void setDataNotify(List<ContactToAddInGroupModel> contactsList) {
        mSelectedContacts.clear();
        mOriginalContacts.clear();
        mSelectedContacts.addAll(contactsList);
        mOriginalContacts.addAll(contactsList);
        notifyDataSetChanged();
    }

    public void performSearch(String newText) {
        newText = newText.toLowerCase(Locale.getDefault());
        mSelectedContacts.clear();
        if (newText.length() == 0) {
            mSelectedContacts.addAll(mOriginalContacts);
        } else {
            for (ContactToAddInGroupModel contactToAdd : mOriginalContacts) {
                if (contactToAdd.getFullName().toLowerCase(Locale.getDefault()).contains(newText)) {
                    mSelectedContacts.add(contactToAdd);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ContactToAddInGroupViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView labelFirstLetter;
        private TextView tvContactName;
        private AppCompatCheckBox cbContactSelected;

        ContactToAddInGroupViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.avatar_image);
            labelFirstLetter = itemView.findViewById(R.id.label_first_letter);
            tvContactName = itemView.findViewById(R.id.contact_name);
            cbContactSelected = itemView.findViewById(R.id.contact_checkbox);
        }

        public void bindValues(int position) {
            if (position >= mSelectedContacts.size()) {
                return;
            }

            final ContactToAddInGroupModel contact = mSelectedContacts.get(position);

            // if the contact is already added, we don't show checkbox...
            if(contact.isInGroup()) {
             cbContactSelected.setVisibility(View.INVISIBLE);
               // mSelectedContacts.remove(position);
            }
            else{

                cbContactSelected.setVisibility(View.VISIBLE);
            }

            if(contact.getType().equals("label")) {
                labelFirstLetter.setText(contact.getFullName());
                ivAvatar.setVisibility(View.INVISIBLE);
            }
            else{
                try {
                    Picasso.with(mActivity).load(contact.getAvatar()).into(ivAvatar);
                    labelFirstLetter.setVisibility(View.INVISIBLE);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            tvContactName.setText(contact.getFullName());
            itemView.setTag(contact);

            cbContactSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        mActivity.onContactSelect(contact.getId());
                    } else {
                        mActivity.onContactDeselect(contact.getId());
                    }
                }

            });
        }

    }

}
