package de.iweinzierl.worktrack.util;

import android.os.Bundle;

public class BundleBuilder {

    private Bundle bundle;

    public BundleBuilder() {
        bundle = new Bundle();
    }

    public BundleBuilder withString(String key, String value) {
        bundle.putString(key, value);
        return this;
    }

    public Bundle build() {
        return bundle;
    }
}
