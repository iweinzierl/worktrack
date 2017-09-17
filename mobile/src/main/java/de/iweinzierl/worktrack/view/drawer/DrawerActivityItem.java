package de.iweinzierl.worktrack.view.drawer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.iweinzierl.worktrack.R;

class DrawerActivityItem implements DrawerItem {

    private final int label;
    private final int icon;
    private final Class activity;


    DrawerActivityItem(int label, int icon, Class activity) {
        this.label = label;
        this.icon = icon;
        this.activity = activity;
    }

    private int getLabel() {
        return label;
    }

    private Class getActivity() {
        return activity;
    }

    @Override
    public View getView(View view, ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        View item = LayoutInflater.from(context).inflate(R.layout.drawer_activity_item, viewGroup, false);

        TextView labelView = (TextView) item.findViewById(R.id.label);
        labelView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        labelView.setText(context.getString(getLabel()));

        return item;
    }

    @Override
    public void onSelect(DrawerLayout drawerLayout, View view) {
        view.getContext().startActivity(new Intent(view.getContext(), getActivity()));
    }
}
