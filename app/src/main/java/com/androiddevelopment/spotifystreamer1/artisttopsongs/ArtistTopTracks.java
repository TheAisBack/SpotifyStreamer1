package com.androiddevelopment.spotifystreamer1.artisttopsongs;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androiddevelopment.spotifystreamer1.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class ArtistTopTracks extends AppCompatActivity {

    public static final String TAG = ArtistTopTracks.class.getSimpleName();

    private static final String ARTIST_ID = "artist_id";
    private static final String ARTIST_NAME = "artist_name";
    private static final String TOP_TRACKS_RESULTS = "top_tracks_results";
    private static final String SEARCH_DONE = "search_done";

    boolean mSearchDone;
    String mArtistId, mArtistName;
    List<Track> mTopTracksList;
    TopTracks mAdapter;
    GetArtistTopTracksTask mCurrentTask;

    @InjectView(R.id.top_tracks) ListView mTopTracksListView;
    @InjectView(R.id.bar) ProgressBar mProgressBar;

    public static Intent launchIntent(Context context, String artistId, String artistName) {
        Intent launchIntent = new Intent(context, ArtistTopTracks.class);
        launchIntent.putExtra(ARTIST_ID, artistId);
        launchIntent.putExtra(ARTIST_NAME, artistName);
        return launchIntent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artists_top_tracks);
        ButterKnife.inject(this);

        mArtistId = getIntent().getStringExtra(ARTIST_ID);
        mArtistName = getIntent().getStringExtra(ARTIST_NAME);

        setupToolbar();
    }
    //searching track
    @Override
    protected void onResume() {
        super.onResume();
        if(!mSearchDone)
            searchTopTracks();
        else
            hideLoading();
    }
    //Destroy search
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelSearch();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TOP_TRACKS_RESULTS, new Gson().toJson(mTopTracksList));
        outState.putBoolean(SEARCH_DONE, mSearchDone);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSearchDone = savedInstanceState.getBoolean(SEARCH_DONE);

        String jsonTopTracks = savedInstanceState.getString(TOP_TRACKS_RESULTS);
        if(jsonTopTracks != null) {

            Type type = new TypeToken<List<Track>>(){}.getType();
            mTopTracksList = new Gson().fromJson(jsonTopTracks, type);

            if(mTopTracksList == null)
                mTopTracksList = new ArrayList<>();

            showTopTracks();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // setting the actionbar of tracks
    private void setupToolbar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.top_ten_tracks);
        actionBar.setSubtitle(mArtistName);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
    //progress bar of search
    private void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }
    // This helps to search top tracks
    private void searchTopTracks() {
        cancelSearch();
        mCurrentTask = new GetArtistTopTracksTask();
        mCurrentTask.execute(mArtistId);
    }
    // This code cancels search
    private void cancelSearch() {
        if(mCurrentTask == null)
            return;
        mCurrentTask.cancel(false);
    }
    class GetArtistTopTracksTask extends AsyncTask<String, Void, List<Track>> {
        @Override
        protected List<Track> doInBackground(String... strings) {
            String artistId = strings[0];
            //Country orgins
            Map<String, Object> options = new HashMap<>();
            options.put("country", "CA");
            //Find track
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotifyService = api.getService();
                Tracks tracks = spotifyService.getArtistTopTrack(artistId, options);
                return tracks.tracks;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(List<Track> tracksList) {
            hideLoading();
            //Network connection.
            if(tracksList == null) {
                Toast.makeText(ArtistTopTracks.this, R.string.network_error, Toast.LENGTH_LONG).show();
                return;
            }
            //These tracks so no tracks.
            if(tracksList.size() == 0) {
                Toast.makeText(ArtistTopTracks.this, R.string.no_top_tracks, Toast.LENGTH_LONG).show();
                return;
            }
            //Code lets us see the top tracks
            mSearchDone = true;
            mTopTracksList = tracksList;
            showTopTracks();
        }

    }
    //Show Top Tracks
    private void showTopTracks() {
        mAdapter = new TopTracks(ArtistTopTracks.this);
        mAdapter.setTopTracksList(mTopTracksList);
        mTopTracksListView.setAdapter(mAdapter);
    }
}