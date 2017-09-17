package de.iweinzierl.worktrack.view.adapter;

public class NoOpActionCallback<T> implements ActionCallback<T> {

    @Override
    public void onRenameItem(T item) {
    }

    @Override
    public void onDeleteItem(T item) {
    }

    @Override
    public void onSelectItem(T item) {
    }
}
