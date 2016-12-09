package com.beginners.myapplication;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.kickflip.sdk.Kickflip;
import io.kickflip.sdk.Share;
import io.kickflip.sdk.api.KickflipApiClient;
import io.kickflip.sdk.api.KickflipCallback;
import io.kickflip.sdk.api.json.Response;
import io.kickflip.sdk.api.json.Stream;
import io.kickflip.sdk.api.json.StreamList;
import io.kickflip.sdk.api.json.User;
import io.kickflip.sdk.exception.KickflipException;

import static com.beginners.myapplication.SigninActivity.mKickflip;


// the myProfile fragment
public class MyprofileFragment extends Fragment implements AbsListView.OnItemClickListener, AbsListView.OnItemLongClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = "StreamListFragment";
    private static final String SERIALIZED_FILESTORE_NAME = "streams";
    private static final boolean VERBOSE = true;

    private StreamListFragmenListener mListener;
    private SwipeRefreshLayout mSwipeLayout;

    private List<Stream> mStreams;
    private boolean mRefreshing;

    private int mCurrentPage = 1;
    private static final int ITEMS_PER_PAGE = 10;

    private DatabaseReference upload;

    private TextView musername;

    private AbsListView mListView;

    private StreamAdapter mAdapter;


    //on long click, the video will be delete from the database
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        final Stream stream = mAdapter.getItem(position);
        KickflipCallback cb = new KickflipCallback() {
            @Override
            public void onSuccess(Response response) {
                if (getActivity() != null) {
                    if (mKickflip.activeUserOwnsStream(stream)) {
                        mAdapter.remove(stream);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.stream_flagged), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onError(KickflipException error) {}
        };

        if (mKickflip.activeUserOwnsStream(stream)) {
            stream.setDeleted(true);
            mKickflip.setStreamInfo(stream, cb);
        } else {
            mKickflip.flagStream(stream, cb);
        }
        return false;
    }

    //on item click, the video will be played
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Stream stream = mAdapter.getItem(position);
        mListener.onStreamPlaybackRequested(stream.getStreamUrl());
    }

//enable the endless scroll
    private EndlessScrollListener mEndlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            Log.i(TAG, "Loading more streams");
            getStreams(false);
        }
    };



    // Mandatory empty constructor for the fragment manager to instantiate the fragment
    public MyprofileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        loadPersistedStreams();
        getStreams(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        persistStreams();
    }


    // Load Streams from local RAM if available.
    private void loadPersistedStreams() {
        if (getActivity() != null) {
            Object streams = LocalPersistence.readObjectFromFile(getActivity(), SERIALIZED_FILESTORE_NAME);
            if (streams != null) {
                displayStreams((List<Stream>) streams, false);
            }
        }
    }

    //Serialize a few Streams to disk so the UI is quickly populated on application re-open
    // write Streams to the local RAM if available.
    private void persistStreams() {
        if (getActivity() != null) {
            while (mStreams.size() > 7) {
                mStreams.remove(mStreams.size()-1);
            }
            LocalPersistence.writeObjectToFile(getActivity(), mStreams, SERIALIZED_FILESTORE_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setOnScrollListener(mEndlessScrollListener);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));

        musername = (TextView) view.findViewById(R.id.username);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        mSwipeLayout.setOnRefreshListener(this);


        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        setupListViewAdapter();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    ///Fetch Streams and display in ListView
    private void getStreams(final boolean refresh) {
        if (mKickflip.getActiveUser() == null || mRefreshing) return;
        mRefreshing = true;
        if (refresh) mCurrentPage = 1;
        //Toast.makeText(getActivity(), "wahaha "+ mKickflip.getActiveUser().getName(),Toast.LENGTH_SHORT).show();

        musername.setText(mKickflip.getActiveUser().getName());


        mKickflip.getStreamsByUsername(mKickflip.getActiveUser().getName(), mCurrentPage, ITEMS_PER_PAGE, new KickflipCallback() {

            //mKickflip.getStreamsByUsername(mKickflip.getActiveUser().getName(), mCurrentPage, ITEMS_PER_PAGE, new KickflipCallback() {
            @Override
            public void onSuccess(Response response) {
                if (VERBOSE) Log.i("API", "request succeeded " + response);
                if (getActivity() != null) {
                    displayStreams(((StreamList) response).getStreams(), !refresh);
                }
                //Toast.makeText(getActivity(), mKickflip.getActiveUser().getName(),Toast.LENGTH_SHORT).show();
                mSwipeLayout.setRefreshing(false);
                mRefreshing = false;
                mCurrentPage++;
            }

            @Override
            public void onError(KickflipException error) {
                if (VERBOSE) Log.i("API", "request failed " + error.getMessage());
                if (getActivity() != null) {
                    showNetworkError();
                }
                //Toast.makeText(getActivity(),  mKickflip.getActiveUser().getName(),Toast.LENGTH_SHORT).show();
                mSwipeLayout.setRefreshing(false);
                mRefreshing = false;
            }
        });
    }

    private void setupListViewAdapter() {
        if (mAdapter == null) {
            mStreams = new ArrayList<>(0);
            mAdapter = new StreamAdapter(getActivity(), mStreams);
            mAdapter.setNotifyOnChange(false);
            mListView.setAdapter(mAdapter);
            if (mKickflip.getActiveUser() != null) {
                mAdapter.setUserName(mKickflip.getActiveUser().getName());
            }
        }
    }

    //Display the given List of Stream in the listview
    private void displayStreams(List<Stream> streams, boolean append) {
        if (append) {
            mStreams.addAll(streams);
        } else {
            mStreams = streams;
        }
        Collections.sort(mStreams);
        mAdapter.refresh(mListView, mStreams);
        if (mStreams.size() == 0) {
            showNoBroadcasts();
        }
    }

    //Inform the user that a network error has occured
    public void showNetworkError() {
        setEmptyListViewText(getString(R.string.no_network));
    }

    //Inform the user that no broadcasts were found
    public void showNoBroadcasts() {
        setEmptyListViewText(getString(R.string.no_broadcasts));
    }

    //If the ListView is hidden, show a text instread
    private void setEmptyListViewText(String text) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(text);
        }
    }

    @Override
    public void onRefresh() {
        if (!mRefreshing) {
            getStreams(true);
        }

    }


    public interface StreamListFragmenListener {
        public void onStreamPlaybackRequested(String url);
    }

}
