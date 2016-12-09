package com.beginners.myapplication;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.kickflip.sdk.api.json.Stream;


//StreamAdapter connects a List of Streamsto an Adapter backed view like ListView

public class StreamAdapter extends ArrayAdapter<Stream> {
    public static final int LAYOUT_ID = R.layout.stream_list_item;
    private AdapterView.OnItemLongClickListener mActionListener;
    private String mUsername;

    public StreamAdapter(final Context context, List<Stream> objects) {
        super(context, LAYOUT_ID, objects);
    }


     // Set a Kickflip username to enable this adapter
    public void setUserName(String userName) {
        mUsername = userName;
    }
//re-load the list of stream to the adapter.
    public void refresh(AbsListView listView, List<Stream> streams) {
        Parcelable state = listView.onSaveInstanceState();
        clear();
        addAll(streams);
        notifyDataSetChanged();
        listView.onRestoreInstanceState(state);
    }


//custom listview
    public View getView(int position, View convertView, ViewGroup parent) {
        Stream stream = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(LAYOUT_ID, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image);
            holder.userNameView = (TextView) convertView.findViewById(R.id.username);
            holder.titleView = (TextView) convertView.findViewById(R.id.title);
            holder.liveBannerView = (TextView) convertView.findViewById(R.id.liveLabel);
            holder.rightTitleView = (TextView) convertView.findViewById(R.id.rightTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int streamLengthSec = stream.getLengthInSeconds();
        if (streamLengthSec == 0) {
            // A Live Stream
            holder.userNameView.setText(stream.getOwnerName());
            holder.liveBannerView.setVisibility(View.VISIBLE);
            holder.rightTitleView.setText("started " + Util.getHumanRelativeDateStringFromString(stream.getTimeStarted()));
        } else {
            // A previously ended Stream
            holder.userNameView.setText(stream.getOwnerName());
            holder.liveBannerView.setVisibility(View.GONE);
            holder.rightTitleView.setText(String.format("%dm %ds",
                    TimeUnit.SECONDS.toMinutes(streamLengthSec),
                    TimeUnit.SECONDS.toSeconds(streamLengthSec) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(streamLengthSec))
            ));
        }

        if (stream.getThumbnailUrl() != null && stream.getThumbnailUrl().compareTo("") != 0) {
            Picasso.with(getContext())
                    .load(stream.getThumbnailUrl())
                    .placeholder(R.drawable.play)
                    .error(R.drawable.play)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.play);
        }
        holder.titleView.setText(stream.getTitle());

        return convertView;
    }

    public static class ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView liveBannerView;
        TextView rightTitleView;
        TextView userNameView;
    }

}
