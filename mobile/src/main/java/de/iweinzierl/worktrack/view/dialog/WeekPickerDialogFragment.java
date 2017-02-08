package de.iweinzierl.worktrack.view.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import com.github.iweinzierl.android.utils.UiUtils;
import com.google.common.collect.Lists;

import java.util.List;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.view.adapter.WeekSpinnerAdapter;
import de.iweinzierl.worktrack.view.adapter.YearSpinnerAdapter;

public class WeekPickerDialogFragment extends DialogFragment {

    public static final String ARGS_MIN_WEEK = "weekpickerdialogfragment.args.min.week";
    public static final String ARGS_MIN_YEAR = "weekpickerdialogfragment.args.min.year";
    public static final String ARGS_MAX_WEEK = "weekpickerdialogfragment.args.max.week";
    public static final String ARGS_MAX_YEAR = "weekpickoerdialogfragment.args.max.year";

    public interface Callback {
        void onWeekSelected(int week, int year);

        void onDismiss();
    }

    private WeekSpinnerAdapter weekAdapter;
    private YearSpinnerAdapter yearAdapter;
    private Callback callback;

    public static WeekPickerDialogFragment newInstance(int minWeek, int minYear, int maxWeek, int maxYear) {
        Bundle args = new Bundle();
        args.putInt(ARGS_MIN_WEEK, minWeek);
        args.putInt(ARGS_MIN_YEAR, minYear);
        args.putInt(ARGS_MAX_WEEK, maxWeek);
        args.putInt(ARGS_MAX_YEAR, maxYear);

        WeekPickerDialogFragment fragment = new WeekPickerDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        int minYear = args.getInt(ARGS_MIN_YEAR);
        int maxYear = args.getInt(ARGS_MAX_YEAR);

        weekAdapter = new WeekSpinnerAdapter(getContext(), 1, 52);
        yearAdapter = new YearSpinnerAdapter(getContext(), createYearList(minYear, maxYear));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_weekpicker, container, false);

        final Spinner weekSpinner = UiUtils.getGeneric(Spinner.class, contentView, R.id.weekSpinner);
        final Spinner yearSpinner = UiUtils.getGeneric(Spinner.class, contentView, R.id.yearSpinner);
        final Button positive = UiUtils.getGeneric(Button.class, contentView, R.id.positive);
        final Button negative = UiUtils.getGeneric(Button.class, contentView, R.id.negative);

        weekSpinner.setAdapter(weekAdapter);
        yearSpinner.setAdapter(yearAdapter);

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onWeekSelected(
                            (Integer) weekSpinner.getSelectedItem(),
                            (Integer) yearSpinner.getSelectedItem());
                }
                dismiss();
            }
        });

        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onDismiss();
                }
                dismiss();
            }
        });

        return contentView;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private List<Integer> createYearList(int minYear, int maxYear) {
        List<Integer> years = Lists.newArrayList();
        for (int idx = minYear; idx <= maxYear; idx++) {
            years.add(idx);
        }
        return years;
    }
}
