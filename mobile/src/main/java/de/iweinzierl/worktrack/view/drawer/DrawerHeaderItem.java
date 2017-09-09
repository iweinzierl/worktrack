package de.iweinzierl.worktrack.view.drawer;

import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.iweinzierl.worktrack.R;

class DrawerHeaderItem implements DrawerItem {

    @Override
    public View getView(View view, ViewGroup viewGroup) {
        View header = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_header, viewGroup, false);
        ((TextView) header.findViewById(R.id.app_name)).setText(R.string.app_name);

        return header;
    }

    @Override
    public void onSelect(DrawerLayout drawerLayout, View view) {
        drawerLayout.closeDrawers();
    }
}
