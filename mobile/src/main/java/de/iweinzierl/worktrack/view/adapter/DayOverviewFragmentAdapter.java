package de.iweinzierl.worktrack.view.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.google.common.collect.Maps;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

import de.iweinzierl.worktrack.DayOverviewFragment;
import de.iweinzierl.worktrack.DayOverviewFragment_;

public class DayOverviewFragmentAdapter extends FragmentStatePagerAdapter {

    private final List<LocalDate> dates;

    private final Map<Integer, Fragment> fragments = Maps.newHashMap();

    public DayOverviewFragmentAdapter(FragmentManager fm, List<LocalDate> dates) {
        super(fm);
        this.dates = dates;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = fragments.get(position);

        if (fragment == null) {
            LocalDate date = dates.get(position);

            Bundle args = new Bundle();
            args.putInt(DayOverviewFragment.ARGS_YEAR, date.getYear());
            args.putInt(DayOverviewFragment.ARGS_MONTH, date.getMonthOfYear());
            args.putInt(DayOverviewFragment.ARGS_DAY, date.getDayOfMonth());

            fragment = DayOverviewFragment_.builder().arg(args).build();
            fragments.put(position, fragment);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return dates.size();
    }

    public int findPosition(final LocalDate date) {
        return dates.indexOf(date);
    }
}
