package de.iweinzierl.worktrack.view.drawer;

import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import de.iweinzierl.worktrack.DayOverviewActivity_;
import de.iweinzierl.worktrack.ManageBackupsActivity_;
import de.iweinzierl.worktrack.ManageWorkplacesActivity_;
import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.SettingsActivity_;
import de.iweinzierl.worktrack.WeekOverviewActivity_;
import de.iweinzierl.worktrack.YearOverviewActivity_;

public class DrawerAdapter extends BaseAdapter {

    private static final DrawerItem[] NAV_ITEMS = {
            new DrawerHeaderItem(),
            new DrawerActivityItem(R.string.activity_manage_workplaces, R.drawable.drawer_location, ManageWorkplacesActivity_.class),
            new DrawerActivityItem(R.string.activity_dayverview, R.drawable.drawer_view_day, DayOverviewActivity_.class),
            new DrawerActivityItem(R.string.activity_weekoverview, R.drawable.drawer_view_week, WeekOverviewActivity_.class),
            new DrawerActivityItem(R.string.activity_yearoverview, R.drawable.drawer_view_week, YearOverviewActivity_.class),
            new DrawerActivityItem(R.string.activity_manage_backups, R.drawable.drawer_backup, ManageBackupsActivity_.class),
            new DrawerActivityItem(R.string.activity_settings, R.drawable.drawer_settings, SettingsActivity_.class)
    };

    private final DrawerLayout drawerLayout;

    public DrawerAdapter(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
    }

    @Override
    public int getCount() {
        return NAV_ITEMS.length;
    }

    @Override
    public Object getItem(int i) {
        return NAV_ITEMS[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final DrawerItem drawerItem = (DrawerItem) getItem(i);

        View itemView = drawerItem.getView(view, viewGroup);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerItem.onSelect(drawerLayout, view);
            }
        });

        return itemView;
    }
}
