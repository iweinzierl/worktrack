package de.iweinzierl.worktrack.view.maps;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import de.iweinzierl.worktrack.R;

public class GoogleMapsImage extends AppCompatImageView {

    private static final String BASE_URI = "https://maps.googleapis.com/maps/api/staticmap";
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleMapsImage.class);

    private double lat;
    private double lon;
    private int zoom;
    private int width;
    private int height;

    public GoogleMapsImage(Context context) {
        super(context);
    }

    public GoogleMapsImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GoogleMapsImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void latlon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public void zoom(int zoom) {
        this.zoom = zoom;
    }

    public void size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Drawable fetchImage() {
        try {
            return createImageDrawable();
        } catch (URISyntaxException e) {
            LOGGER.error("Wrong URI syntax!", e);
        } catch (IOException e) {
            LOGGER.error("Unable to load image from GoogleMaps", e);
        }

        // TODO set default image
        return null;
    }

    private Drawable createImageDrawable() throws URISyntaxException, IOException {
        URI uri = new UriBuilder()
                .center(lat, lon)
                .zoom(zoom)
                .size(width, height)
                .build();

        InputStream in = uri.toURL().openConnection().getInputStream();
        return Drawable.createFromStream(in, "GoogleMapsSrc");
    }

    private class UriBuilder {
        private double lat;
        private double lon;
        private int zoom;
        private int width;
        private int height;

        UriBuilder center(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
            return this;
        }

        UriBuilder zoom(int zoom) {
            this.zoom = zoom;
            return this;
        }

        UriBuilder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        URI build() throws URISyntaxException {
            String imageUri = new StringBuilder(BASE_URI)
                    .append("?center=").append(lat).append(",").append(lon)
                    .append("&zoom=").append(zoom)
                    .append("&size=").append(width).append("x").append(height)
                    .append("&scale=2")
                    .append("&key=").append(getResources().getString(R.string.google_maps_key))
                    .toString();

            LOGGER.info("Load map image from: {}", imageUri);
            return new URI(imageUri);
        }
    }
}
