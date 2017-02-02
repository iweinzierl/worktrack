package de.iweinzierl.worktrack.view.adapter;

public interface ItemToucheHelperAdapter<T> {

    void onItemDismiss(int position);

    T getItem(int position);
}
