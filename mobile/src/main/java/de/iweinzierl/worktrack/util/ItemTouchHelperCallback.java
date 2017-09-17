package de.iweinzierl.worktrack.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.view.adapter.ItemToucheHelperAdapter;

/**
 * Item touch helper to remove existing workplaces.
 */
public class ItemTouchHelperCallback<T> extends ItemTouchHelper.Callback {

    /**
     * Callback for removal of workplaces.
     */
    public interface ItemCallback<T> {
        void onDeleteItem(T item);
    }

    private Context context;

    private int discardDialogTitle = R.string.util_delete_title;
    private int discardConfirmButtonTitle = R.string.util_delete_action_confirm;
    private int discardCancelButtonTitle = R.string.util_delete_action_cancel;

    private final ItemToucheHelperAdapter<T> adapter;
    private final ItemCallback<T> itemCallback;

    public ItemTouchHelperCallback(Context context, ItemToucheHelperAdapter<T> adapter, ItemCallback<T> itemCallback) {
        this.context = context;
        this.adapter = adapter;
        this.itemCallback = itemCallback;
    }


    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
        final T item = adapter.getItem(viewHolder.getAdapterPosition());

        new AlertDialog.Builder(context)
                .setTitle(discardDialogTitle)
                .setNegativeButton(discardCancelButtonTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();
                        adapter.onItemDismissRevert(viewHolder.getAdapterPosition());
                    }
                })
                .setPositiveButton(discardConfirmButtonTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemCallback.onDeleteItem(item);
                        adapter.onItemDismiss(viewHolder.getAdapterPosition());
                    }
                })
                .show();
    }

    public void setDiscardDialogTitle(int discardDialogTitle) {
        this.discardDialogTitle = discardDialogTitle;
    }

    public void setDiscardConfirmButtonTitle(int discardConfirmButtonTitle) {
        this.discardConfirmButtonTitle = discardConfirmButtonTitle;
    }

    public void setDiscardCancelButtonTitle(int discardCancelButtonTitle) {
        this.discardCancelButtonTitle = discardCancelButtonTitle;
    }
}
