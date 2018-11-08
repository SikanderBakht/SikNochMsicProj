package com.hellodemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hellodemo.NotesActivity;
import com.hellodemo.R;
import com.hellodemo.models.ChatNote;
import com.hellodemo.ui.MemphisEditTextView;

import java.util.ArrayList;

/**
 * Created by new user on 2/25/2018.
 */

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String TAG = "HelloDemoNotesAdapter";
    private static final int TYPE_NOTE = 0;
    private static final int TYPE_FOOTER = 1;
    private ArrayList<ChatNote> mNotesList = new ArrayList<>();
    private Context ctx;
    public int focusPosition = -1;

    public NotesAdapter(Context ctx, ArrayList<ChatNote> mNotesList) {
        this.ctx = ctx;
        this.mNotesList = mNotesList;
    }

    public String constructNotesString() {
//        Log.v(TAG, "Saving " + mNotesList.size() + " Notes...");

        String notes = "";
        for (int i = 0; i < mNotesList.size(); i++) {
//            Log.v(TAG, "Notes Test: " + mNotesList.get(i).getNote());
            if (mNotesList.get(i).getNote().length() != 0) {
                if (i != 0) {
                    notes = notes + "\n";
                }
                notes = notes + mNotesList.get(i).getNote();
            }
        }

//        Log.v(TAG, "Saving Final Note Text: " + notes);

        //notes=notes+"/n";
        return notes;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case TYPE_NOTE:
                View view = LayoutInflater.from(ctx).inflate(R.layout.item_notes_list, parent, false);
                holder = new NotesViewHolder(view, new MyCustomEditTextListener());
                break;
            case TYPE_FOOTER:
                View v2 = LayoutInflater.from(ctx).inflate(R.layout.footer_notes_list, parent, false);
                holder = new FooterViewHolder(v2);
                break;
            default:
                Log.e(TAG, "type not supported");
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.v(TAG, "onBindViewHolder started");
        switch (getItemViewType(position)) {
            case TYPE_NOTE:
                bindNoteViewHolder(holder, position);
                break;
            case TYPE_FOOTER:
                bindFooterViewHolder(holder, position);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mNotesList.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_NOTE;
        }
    }

    private void bindNoteViewHolder(RecyclerView.ViewHolder  rholder, int position) {
        NotesViewHolder holder = (NotesViewHolder) rholder;


        // update MyCustomEditTextListener every time we bind a new item
        // so that it knows what item in mDataset to update
        holder.myCustomEditTextListener.updatePosition(holder.getAdapterPosition());
        holder.edtNote.setText(mNotesList.get(holder.getAdapterPosition()).getNote());




        if (position == focusPosition) {
            holder.edtNote.requestFocus();
            holder.edtNote.append("");
        }
//        holder.edtNote.setText(mNotesList.get(position).getNote());
    }

    private void bindFooterViewHolder(RecyclerView.ViewHolder rholder, int position) {
        FooterViewHolder holder = (FooterViewHolder) rholder;
    }

    @Override
    public int getItemCount() {
        return mNotesList.size() + 1;
    }

    class NotesViewHolder extends RecyclerView.ViewHolder {
        private View viewBar;
        public MemphisEditTextView edtNote;
        public MyCustomEditTextListener myCustomEditTextListener;

        NotesViewHolder(View itemView, MyCustomEditTextListener myCustomEditTextListener) {
            super(itemView);
            this.myCustomEditTextListener = myCustomEditTextListener;
            viewBar = itemView.findViewById(R.id.view_note_bar);
            edtNote = itemView.findViewById(R.id.edttxt_note);

            edtNote.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (event.getAction() != KeyEvent.ACTION_UP && getAdapterPosition() != -1 && v.getText().toString().length() != 0) {
                        ((NotesActivity) (ctx)).addNewNote(getAdapterPosition() + 1);
                    }
                    return true;
                }
            });
            edtNote.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        if (getAdapterPosition() != -1 && edtNote.getText().toString().length() == 0) {
                            ((NotesActivity) (ctx)).removeNote(getAdapterPosition());
                            return false;
                        }
                    }

                    return false;
                }

            });

            // This will ensure that whenever text is changed, our custom listener is called...
            // which in turn, will update model...
            edtNote.addTextChangedListener(myCustomEditTextListener);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txtWrite;

        FooterViewHolder(View itemView) {
            super(itemView);
            txtWrite = itemView.findViewById(R.id.write_new_text);
            txtWrite.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ((NotesActivity) (ctx)).addNewNote(mNotesList.size());
        }
    }

    // we make TextWatcher to be aware of the position it currently works with
    // this way, once a new item is attached in onBindViewHolder, it will
    // update current position MyCustomEditTextListener, reference to which is kept by ViewHolder
    private class MyCustomEditTextListener implements TextWatcher {
        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // no op
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            mNotesList.get(position).setNote(charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // no op
        }
    }
}


