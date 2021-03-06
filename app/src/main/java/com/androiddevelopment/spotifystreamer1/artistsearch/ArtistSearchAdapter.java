package com.androiddevelopment.spotifystreamer1.artistsearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androiddevelopment.spotifystreamer1.R;
import com.androiddevelopment.spotifystreamer1.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Artist;

public class ArtistSearchAdapter extends BaseAdapter {

    static final String TAG = ArtistSearchAdapter.class.getSimpleName();

    Context mContext;
    LayoutInflater mLayoutInflater;
    List<Artist> mArtistsList;

    public ArtistSearchAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        mArtistsList = new ArrayList<>();

    }

    public void setArtistsList(List<Artist> artistsList) {
        mArtistsList = artistsList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mArtistsList.size();
    }

    @Override
    public Artist getItem(int i) {
        if(mArtistsList.size() == 0)
            return null;

        return mArtistsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mLayoutInflater.inflate(R.layout.list_artist, viewGroup, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        Artist artist = mArtistsList.get(position);

        String thumbnailUrl = null;
        if(artist.images.size() > 0) {
            thumbnailUrl = Utils.getThumbnailUrl(artist.images, 200);
            if(thumbnailUrl == null) {
                thumbnailUrl = Utils.getThumbnailUrl(artist.images, 0);
            }
        }
        if(thumbnailUrl != null)
            Picasso.with(mContext).load(thumbnailUrl).into(holder.thumbnail);
        else
            holder.thumbnail.setImageBitmap(null);

        holder.name.setText(artist.name);


        return convertView;
    }

   static class ViewHolder {
        @InjectView(R.id.name) TextView name;
        @InjectView(R.id.thumbnail) ImageView thumbnail;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


}
