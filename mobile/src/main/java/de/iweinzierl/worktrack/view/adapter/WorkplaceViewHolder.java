package de.iweinzierl.worktrack.view.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.persistence.Workplace;
import de.iweinzierl.worktrack.view.maps.GoogleMapsImage;

class WorkplaceViewHolder extends RecyclerView.ViewHolder {

    private final Context context;
    private final ActionCallback<Workplace> actionCallback;

    private final GoogleMapsImage googleMapsImage;
    private final TextView titleView;
    private final TextView locationView;
    private final TextView radiusView;
    private final View renameButton;
    private final View deleteButton;

    WorkplaceViewHolder(Context context, View itemView, final ActionCallback<Workplace> actionCallback) {
        super(itemView);
        this.context = context;
        this.actionCallback = actionCallback;

        googleMapsImage = (GoogleMapsImage) itemView.findViewById(R.id.mapImage);
        titleView = (TextView) itemView.findViewById(R.id.title);
        locationView = (TextView) itemView.findViewById(R.id.location);
        radiusView = (TextView) itemView.findViewById(R.id.radius);
        renameButton = itemView.findViewById(R.id.rename);
        deleteButton = itemView.findViewById(R.id.delete);
    }

    void apply(final Workplace workplace) {
        titleView.setText(workplace.getName());
        locationView.setText(createLocationText(workplace));
        radiusView.setText(context.getString(R.string.radius_template, workplace.getRadius()));
        googleMapsImage.latlon(workplace.getLat(), workplace.getLon());
        googleMapsImage.size(192, 192);
        googleMapsImage.zoom(15);

        new ImageLoader(googleMapsImage).execute(workplace);

        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionCallback.onRenameItem(workplace);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionCallback.onDeleteItem(workplace);
            }
        });
    }

    private String createLocationText(Workplace workplace) {
        return context.getString(R.string.location_template, workplace.getLat(), workplace.getLon());
    }

    private class ImageLoader extends AsyncTask<Workplace, Void, Drawable> {
        private final GoogleMapsImage googleMapsImage;

        private ImageLoader(GoogleMapsImage googleMapsImage) {
            this.googleMapsImage = googleMapsImage;
        }

        @Override
        protected Drawable doInBackground(Workplace... workplaces) {
            return googleMapsImage.fetchImage();
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            googleMapsImage.setImageDrawable(drawable);
        }
    }
}
