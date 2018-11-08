package com.hellodemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hellodemo.R;

/**
 * Created by Mahnoor on 10/04/2018.
 */



public class Settings_Social_Adaptor extends BaseAdapter {

    private Context mContext;
    private String[]  Title;
    private int[] imge;

    public Settings_Social_Adaptor(Context context, String[] text1,int[] imageIds) {
        //  int[] imageIds
        mContext = context;
        Title = text1;
        imge = imageIds;

    }

    public int getCount() {
        // TODO Auto-generated method stub
        return Title.length;
    }

    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {


        if (position == 0 || position == 1 || position == 2 || position == 3 || position == 4) {
            //LayoutInflater inflater = null;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row;
            row = inflater.inflate(R.layout.sociallistrow, parent, false);
            TextView title;
            ImageView i1;
            i1 = (ImageView) row.findViewById(R.id.icon);
            title = (TextView) row.findViewById(R.id.Itemname);
            title.setText(Title[position]);
            i1.setImageResource(imge[position]);

            return (row);

        }

        //LayoutInflater inflater = null;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowdifferent;
        rowdifferent = inflater.inflate(R.layout.accountslistrow, parent, false);
        TextView title;
        ImageView i1;
        i1 = (ImageView)  rowdifferent.findViewById(R.id.icon);
        title = (TextView)  rowdifferent.findViewById(R.id.Itemname);
        title.setText(Title[position]);
        i1.setImageResource(imge[position]);

        return ( rowdifferent);
    }
}

