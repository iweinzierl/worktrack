package de.iweinzierl.worktrack.view.drawer;

import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.iweinzierl.worktrack.R;

class DrawerHeaderItem implements DrawerItem {

    @Override
    public View getView(View view, ViewGroup viewGroup) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_header, viewGroup, false);
    }

    @Override
    public void onSelect(DrawerLayout drawerLayout, View view) {
        drawerLayout.closeDrawers();
    }
}
