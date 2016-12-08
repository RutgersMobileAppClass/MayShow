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
import android.widget.EditText;
import android.widget.ImageButton;
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


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 */
public class StreamListFragment extends Fragment implements AbsListView.OnItemClickListener, AbsListView.OnItemLongClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = "StreamListFragment";
    private static final String SERIALIZED_FILESTORE_NAME = "streams";
    private static final boolean VERBOSE = true;

    private StreamListFragmenListener mListener;
    private SwipeRefreshLayout mSwipeLayout;

    private List<Stream> mStreams;
    private boolean mRefreshing;

    private int mCurrentPage = 1;
    private static final int ITEMS_PER_PAGE = 10;

    private EditText username;
    private ImageButton search;


    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private StreamAdapter mAdapter;


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
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Stream stream = mAdapter.getItem(position);
        mListener.onStreamPlaybackRequested(stream.getStreamUrl());
    }




    private EndlessScrollListener mEndlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            Log.i(TAG, "Loading more streams");
            getStreams(false);
        }
    };


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StreamListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            search =(ImageButton) getActivity().findViewById(R.id.btn_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = (EditText) getActivity().findViewById(R.id.search_name);
                if (mKickflip.getActiveUser() == null || mRefreshing) return;
                mRefreshing = true;
                if (true) mCurrentPage = 1;
                Toast.makeText(getActivity(), "search text: "+ username.getText().toString(),Toast.LENGTH_SHORT).show();

               // mKickflip.getStreamsByKeyword(null, mCurrentPage, ITEMS_PER_PAGE, new KickflipCallback() {

                    mKickflip.getStreamsByUsername(username.getText().toString(), mCurrentPage, ITEMS_PER_PAGE, new KickflipCallback() {
                    @Override
                    public void onSuccess(Response response) {
                        if (VERBOSE) Log.i("API", "request succeeded " + response);
                        if (getActivity() != null) {
                            displayStreams(((StreamList) response).getStreams(), false);
                        }
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
                        mSwipeLayout.setRefreshing(false);
                        mRefreshing = false;
                    }
                });
            }
        });


        getStreams(true);

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


    /**
     * Load persisted Streams from disk if available.
     */
    private void loadPersistedStreams() {
        if (getActivity() != null) {
            Object streams = LocalPersistence.readObjectFromFile(getActivity(), SERIALIZED_FILESTORE_NAME);
            if (streams != null) {
                displayStreams((List<Stream>) streams, false);
            }
        }
    }

    /**
     * Serialize a few Streams to disk so the UI is quickly populated
     * on application re-launch
     *
     * If we had reason to keep a robust local copy of the data, we'd use sqlite
     */
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
        View view = inflater.inflate(R.layout.fragment_stream_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setOnScrollListener(mEndlessScrollListener);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));
        // Why does this selection remain if I long press, release
        // without activating onListItemClick?
        //mListView.setSelector(R.drawable.stream_list_selector_overlay);
        //mListView.setDrawSelectorOnTop(true);

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
        try {
            mListener = (StreamListFragmenListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement StreamListFragmenListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }




    /**
     * Fetch Streams and display in ListView
     *
     * @param refresh whether this fetch is for a subsequent page
     *                      or to refresh the first page
     */
    private void getStreams(final boolean refresh) {
        if (mKickflip.getActiveUser() == null || mRefreshing) return;
        mRefreshing = true;
        if (refresh) mCurrentPage = 1;
        Toast.makeText(getActivity(), "wahaha "+ mKickflip.getActiveUser().getName(),Toast.LENGTH_SHORT).show();


               /* mKickflip.setUserInfo("password", "email", "displayName", null,
                new KickflipCallback() {
                    @Override
                    public void onSuccess(Response response) {
                        User newUser = (User) response;
                        Log.i("Hooray", "You modified user: " + newUser.getName());
                    }

                    @Override
                    public void onError(KickflipException error) {
                        Log.w(TAG, "setUserInfo Error: " + error.getMessage());
                    }
                });*/
/*       pair_name.put("JunjieFeng",mKickflip.getActiveUser().getName());
        //pair_name.put("XueweiLi",mKickflip.getActiveUser().getName());
        pair_name.put("ShiWang","psrcui4ywvm7");
        pair_name.put("RuotianZhang","heruhjxb1w8a");
        match_name.put(mKickflip.getActiveUser().getName(),"JunjieFeng");
        //match_name.put(mKickflip.getActiveUser().getName(),"XueweiLi");
        match_name.put("psrcui4ywvm7","ShiWang");
        match_name.put("heruhjxb1w8a","RuotianZhang");

*/



        //Toast.makeText(getActivity(), match_name.get(mKickflip.getActiveUser().getName()), Toast.LENGTH_SHORT).show();

            mKickflip.getStreamsByKeyword(null, mCurrentPage, ITEMS_PER_PAGE, new KickflipCallback() {

            //mKickflip.getStreamsByUsername(mKickflip.getActiveUser().getName(), mCurrentPage, ITEMS_PER_PAGE, new KickflipCallback() {
            @Override
            public void onSuccess(Response response) {
                if (VERBOSE) Log.i("API", "request succeeded " + response);
                if (getActivity() != null) {
                    displayStreams(((StreamList) response).getStreams(), !refresh);
                }
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

    /**
     * Display the given List of {@link io.kickflip.sdk.api.json.Stream} Objects
     *
     * @param streams a List of {@link io.kickflip.sdk.api.json.Stream} Objects
     * @param append whether to append the given streams to the current list
     *               or use the given streams as the absolute dataset.
     */
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

    /**
     * Inform the user that a network error has occured
     */
    public void showNetworkError() {
        setEmptyListViewText(getString(R.string.no_network));
    }

    /**
     * Inform the user that no broadcasts were found
     */
    public void showNoBroadcasts() {
        setEmptyListViewText(getString(R.string.no_broadcasts));
    }

    /**
     * If the ListView is hidden, show the
     *
     * @param text
     */
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
