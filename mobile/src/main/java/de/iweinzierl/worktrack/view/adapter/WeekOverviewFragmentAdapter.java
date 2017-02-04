package de.iweinzierl.worktrack.view.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import de.iweinzierl.worktrack.WeekOverviewFragment;
import de.iweinzierl.worktrack.WeekOverviewFragment_;
import de.iweinzierl.worktrack.model.Week;

public class WeekOverviewFragmentAdapter extends FragmentStatePagerAdapter {

    private final List<Week> weeks;

    private final Map<Integer, Fragment> fragments = Maps.newHashMap();

    public WeekOverviewFragmentAdapter(FragmentManager fm, List<Week> weeks) {
        super(fm);
        this.weeks = weeks;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = fragments.get(position);

        if (fragment == null) {
            Week week = weeks.get(position);

            Bundle args = new Bundle();
            args.putInt(WeekOverviewFragment.ARGS_YEAR, week.getYear());
            args.putInt(WeekOverviewFragment.ARGS_WEEKNUM, week.getWeekNum());

            fragment = WeekOverviewFragment_.builder().arg(args).build();
            fragments.put(position, fragment);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return weeks.size();
    }

    public int findPosition(final Week week) {
        return weeks.indexOf(week);
    }
}
