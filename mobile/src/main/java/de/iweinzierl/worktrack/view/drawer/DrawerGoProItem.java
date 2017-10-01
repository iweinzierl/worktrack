package de.iweinzierl.worktrack.view.drawer;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import de.iweinzierl.worktrack.WorktrackApplication;

public class DrawerGoProItem extends DrawerActivityItem {

    DrawerGoProItem(int label, int icon) {
        super(label, icon, null);
    }

    @Override
    public void onSelect(DrawerLayout drawerLayout, View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + WorktrackApplication.PACKAGE_NAME_PRO));
        view.getContext().startActivity(intent);
    }
}
