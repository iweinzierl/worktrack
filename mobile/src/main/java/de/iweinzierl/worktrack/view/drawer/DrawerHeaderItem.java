package de.iweinzierl.worktrack.view.drawer;

import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.WorktrackApplication;
import de.iweinzierl.worktrack.util.ProductFlavor;

class DrawerHeaderItem implements DrawerItem {

    @Override
    public View getView(View view, ViewGroup viewGroup) {
        View header = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_header, viewGroup, false);

        displayLabelPro(header.findViewById(R.id.proLabel));
        displayLabelDev(header.findViewById(R.id.devLabel));

        return header;
    }

    private void displayLabelPro(View proLabel) {
        if (proLabel != null && WorktrackApplication.getInstance().getProductFlavor() == ProductFlavor.PRO) {
            proLabel.setVisibility(View.VISIBLE);
        }
    }

    private void displayLabelDev(View devLabel) {
        if (devLabel != null && WorktrackApplication.getInstance().getProductFlavor() == ProductFlavor.DEV) {
            devLabel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSelect(DrawerLayout drawerLayout, View view) {
        drawerLayout.closeDrawers();
    }
}
