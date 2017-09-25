package de.iweinzierl.worktrack.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.iweinzierl.android.utils.UiUtils;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import de.iweinzierl.worktrack.R;

public class WorkingTimeStatisticsView extends FrameLayout {

    private static final PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
            .appendHours()
            .printZeroNever()
            .appendSuffix("h ")
            .appendMinutes()
            .printZeroAlways()
            .appendSuffix("min")
            .toFormatter();


    private View containerWorkingHoursBar;
    private View workingHoursBar;
    private View workingHoursLeftBar;
    private TextView workingHoursValue;

    private View containerOverHoursBar;
    private View overHoursBar;
    private View overHoursLeftBar;
    private TextView overHoursValue;

    private Duration expectedWorkingHours;
    private Duration workingHours;
    private Duration overHours;

    public WorkingTimeStatisticsView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_workingtime_statistics, this);
    }

    public WorkingTimeStatisticsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_workingtime_statistics, this);
    }

    public WorkingTimeStatisticsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_workingtime_statistics, this);
    }

    public void apply(Duration expectedWorkingHours, Duration workingHours, Duration overHours) {
        setExpectedWorkingHours(expectedWorkingHours);
        setWorkingHours(workingHours);
        setOverHours(overHours);

        adjustProgressBars();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        adjustProgressBars();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        containerWorkingHoursBar = UiUtils.getView(this, R.id.containerWorkingHoursBar);
        workingHoursBar = UiUtils.getGeneric(View.class, this, R.id.workingHoursBar);
        workingHoursLeftBar = UiUtils.getGeneric(View.class, this, R.id.workingHoursLeftBar);
        workingHoursValue = UiUtils.getGeneric(TextView.class, this, R.id.workingHoursValue);

        containerOverHoursBar = UiUtils.getView(this, R.id.containerOverHoursBar);
        overHoursBar = UiUtils.getGeneric(View.class, this, R.id.overHoursBar);
        overHoursLeftBar = UiUtils.getGeneric(View.class, this, R.id.overHoursLeftBar);
        overHoursValue = UiUtils.getGeneric(TextView.class, this, R.id.overHoursValue);

        if (workingHours != null) {
            setWorkingHours(workingHours);
        }

        if (overHours != null) {
            setOverHours(overHours);
        }
    }

    private void adjustProgressBars() {
        adjustProgressBarWorkingHours();
        adjustProgressBarOverHours();
    }

    private void adjustProgressBarWorkingHours() {
        if (workingHoursBar == null || workingHoursLeftBar == null
                || expectedWorkingHours == null || workingHours == null) {
            return;
        }

        double minutesExpected = (double) expectedWorkingHours.toStandardMinutes().getMinutes();
        double minutesWorking = (double) workingHours.toStandardMinutes().getMinutes();

        int totalWidth = containerWorkingHoursBar.getWidth();
        int widthWorking = (int) (totalWidth * (minutesWorking / minutesExpected));

        ViewGroup.LayoutParams layoutParamsBar = workingHoursBar.getLayoutParams();
        layoutParamsBar.width = widthWorking;
        workingHoursBar.setLayoutParams(layoutParamsBar);

        ViewGroup.LayoutParams layoutParamsLeftBar = workingHoursLeftBar.getLayoutParams();
        layoutParamsLeftBar.width = totalWidth - widthWorking;
        workingHoursLeftBar.setLayoutParams(layoutParamsLeftBar);
    }

    private void adjustProgressBarOverHours() {
        if (overHoursBar == null || overHoursLeftBar == null || expectedWorkingHours == null || overHours == null) {
            return;
        }

        double minutesExpected = (double) expectedWorkingHours.toStandardMinutes().getMinutes();
        double minutesOver = (double) overHours.toStandardMinutes().getMinutes();

        int totalWidth = containerOverHoursBar.getWidth();
        int widthOver = (int) (totalWidth * (minutesOver / minutesExpected));

        ViewGroup.LayoutParams layoutParamsBar = overHoursBar.getLayoutParams();
        layoutParamsBar.width = widthOver;
        overHoursBar.setLayoutParams(layoutParamsBar);

        ViewGroup.LayoutParams layoutParamsLeftBar = overHoursLeftBar.getLayoutParams();
        layoutParamsLeftBar.width = totalWidth - widthOver;
        overHoursLeftBar.setLayoutParams(layoutParamsLeftBar);
    }

    private void setWorkingHours(Duration workingHours) {
        this.workingHours = workingHours;
        if (workingHoursValue != null) {
            workingHoursValue.setText(workingHours.toPeriod().toString(periodFormatter));
        }
    }

    private void setOverHours(Duration overHours) {
        this.overHours = overHours;
        if (overHoursValue != null) {
            overHoursValue.setText(overHours.toPeriod().toString(periodFormatter));
        }
    }

    public void setExpectedWorkingHours(Duration expectedWorkingHours) {
        this.expectedWorkingHours = expectedWorkingHours;
    }
}
