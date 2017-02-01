package de.iweinzierl.worktrack.view.drawer;

import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;

interface DrawerItem {

    View getView(View view, ViewGroup viewGroup);

    void onSelect(DrawerLayout drawerLayout, View view);
}
