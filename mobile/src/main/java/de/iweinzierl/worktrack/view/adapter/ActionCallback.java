package de.iweinzierl.worktrack.view.adapter;

public interface ActionCallback<T> {
    void onRenameItem(T item);

    void onDeleteItem(T item);

    void onSelectItem(T item);
}
