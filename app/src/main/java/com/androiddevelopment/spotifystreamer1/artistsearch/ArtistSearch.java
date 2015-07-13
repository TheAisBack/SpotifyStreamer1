package com.androiddevelopment.spotifystreamer1.artistsearch;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androiddevelopment.spotifystreamer1.R;
import com.androiddevelopment.spotifystreamer1.artisttopsongs.ArtistTopTracks;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

public class ArtistSearch extends AppCompatActivity {

    static final String TAG = ArtistSearch.class.getSimpleName();

    public static final String SEARCH_RESULTS = "search_results";
    public static final int SEARCH_DELAY = 500;

    SearchArtistTask mCurrentTask;
    ArtistSearchAdapter mAdapter;
    List<Artist> mArtistsList;

    @InjectView(R.id.search) EditText mSearchInput;
    @InjectView(R.id.search_results) ListView mSearchResultsListView;
    @InjectView(R.id.bar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_search);
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Configure search input
        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                searchArtists();
            }
        });

        mSearchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Artist artist = mAdapter.getItem(position);
                startActivity(ArtistTopTracks.launchIntent(ArtistSearch.this, artist.id, artist.name));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelSearch();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String jsonSearchResults = new Gson ().toJson(mArtistsList);
        outState.putString(SEARCH_RESULTS, jsonSearchResults);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String jsonSearchResults = savedInstanceState.getString(SEARCH_RESULTS);
        if(jsonSearchResults != null) {

            Type type = new TypeToken<List<Artist>>() {}.getType();
            mArtistsList = new Gson().fromJson(jsonSearchResults, type);

            if(mArtistsList == null)
                mArtistsList = new ArrayList<>();

            showResults();
        }
    }

    private void showResults() {
        mAdapter = new ArtistSearchAdapter(ArtistSearch.this);
        mAdapter.setArtistsList(mArtistsList);
        mSearchResultsListView.setAdapter(mAdapter);
    }

    private void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void searchArtists() {
        cancelSearch();

        showLoading();
        mCurrentTask = new SearchArtistTask();
        mCurrentTask.execute(mSearchInput.getText().toString());
    }

    private void cancelSearch() {
        if(mCurrentTask == null)
            return;
        mCurrentTask.cancel(false);
    }

    class SearchArtistTask extends AsyncTask<String, Void, List<Artist>> {
        @Override
        protected List<Artist> doInBackground(String... strings) {
            String queryString = strings[0];

            try {
                Thread.sleep(SEARCH_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(isCancelled() || queryString.length() == 0)
                return new ArrayList<Artist>();

            queryString = "*" + queryString + "*";

            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotifyService = api.getService();
                ArtistsPager artistsPager = spotifyService.searchArtists(queryString);
                return artistsPager.artists.items;
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }

        }
        @Override
        protected void onPostExecute(List<Artist> artistsList) {
            hideLoading();
            if(artistsList == null) {
                Toast.makeText(ArtistSearch.this, R.string.network_error, Toast.LENGTH_LONG).show();
                return;
            }
            if(artistsList.size() == 0 && mSearchInput.getText().toString().length() > 0) {
                Toast.makeText(ArtistSearch.this, R.string.no_artists_found, Toast.LENGTH_LONG).show();
            }
            mArtistsList = artistsList;
            showResults();
        }
    }
}
