package de.iweinzierl.worktrack.view.drawer;

import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import de.iweinzierl.worktrack.DayOverviewActivity_;
import de.iweinzierl.worktrack.WeekOverviewActivity_;
import de.iweinzierl.worktrack.R;

public class DrawerAdapter extends BaseAdapter {

    private static final DrawerItem[] NAV_ITEMS = {
            new DrawerHeaderItem(),
            new DrawerActivityItem(R.string.activity_overview, R.drawable.ic_view_day_white_24px, DayOverviewActivity_.class),
            new DrawerActivityItem(R.string.activity_overview_chart, R.drawable.ic_view_week_white_24px, WeekOverviewActivity_.class)
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
