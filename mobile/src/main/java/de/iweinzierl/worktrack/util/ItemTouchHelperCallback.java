package de.iweinzierl.worktrack.util;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import de.iweinzierl.worktrack.ManageWorkplacesActivity;
import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.persistence.Workplace;
import de.iweinzierl.worktrack.view.adapter.ItemToucheHelperAdapter;

/**
 * Item touch helper to remove existing workplaces.
 */
public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    /**
     * Callback for removal of workplaces.
     */
    public interface WorkplaceCallback {
        void onDeleteWorkplace(Workplace item);
    }

    private ManageWorkplacesActivity manageWorkplacesActivity;

    private final ItemToucheHelperAdapter<Workplace> adapter;
    private final WorkplaceCallback workplaceCallback;

    public ItemTouchHelperCallback(ManageWorkplacesActivity manageWorkplacesActivity, ItemToucheHelperAdapter<Workplace> adapter, WorkplaceCallback workplaceCallback) {
        this.manageWorkplacesActivity = manageWorkplacesActivity;
        this.adapter = adapter;
        this.workplaceCallback = workplaceCallback;
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
        final Workplace workplace = adapter.getItem(viewHolder.getAdapterPosition());

        new AlertDialog.Builder(manageWorkplacesActivity)
                .setTitle(R.string.activity_manage_workplaces_action_delete_title)
                .setMessage(R.string.activity_manage_workplaces_action_delete_message)
                .setNegativeButton(R.string.activity_manage_workplaces_action_delete_cancel, new DialogInterface.OnClickListener() {
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
                .setPositiveButton(R.string.activity_manage_workplaces_action_delete_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        workplaceCallback.onDeleteWorkplace(workplace);
                        adapter.onItemDismiss(viewHolder.getAdapterPosition());
                    }
                })
                .show();
    }
}
