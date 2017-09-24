package de.iweinzierl.worktrack.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.github.iweinzierl.android.utils.UiUtils;

import org.joda.time.Period;

import de.iweinzierl.worktrack.R;

public class WorkingTimeStatisticsView extends ConstraintLayout {

    private TextView workingHoursView;
    private TextView overHoursView;

    private Period workingHours;
    private Period overHours;

    public WorkingTimeStatisticsView(Context context) {
        super(context);
    }

    public WorkingTimeStatisticsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WorkingTimeStatisticsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        workingHoursView = UiUtils.getGeneric(TextView.class, this, R.id.workingHoursView);
        overHoursView = UiUtils.getGeneric(TextView.class, this, R.id.overHoursView);

        if (workingHours != null) {
            workingHoursView.setText(workingHours.toString());
        }

        if (overHours != null) {
            overHoursView.setText(overHours.toString());
        }
    }

    public void setWorkingHours(Period workingHours) {
        this.workingHours = workingHours;
        if (workingHoursView != null) {
            workingHoursView.setText(workingHours.toString());
        }
    }

    public void setOverHours(Period overHours) {
        this.overHours = overHours;
        if (overHoursView != null) {
            overHoursView.setText(overHours.toString());
        }
    }
}
