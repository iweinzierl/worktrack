package de.iweinzierl.worktrack.view.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import de.iweinzierl.worktrack.YearOverviewFragment;
import de.iweinzierl.worktrack.YearOverviewFragment_;
import de.iweinzierl.worktrack.model.Year;

public class YearOverviewFragmentAdapter extends FragmentStatePagerAdapter {

    private final List<Year> years;

    private final Map<Integer, Fragment> fragments = Maps.newHashMap();

    public YearOverviewFragmentAdapter(FragmentManager fm, List<Year> years) {
        super(fm);
        this.years = years;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = fragments.get(position);

        if (fragment == null) {
            Year year = years.get(position);

            Bundle args = new Bundle();
            args.putInt(YearOverviewFragment.ARGS_YEAR, year.getYear());

            fragment = YearOverviewFragment_.builder().arg(args).build();
            fragments.put(position, fragment);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return years.size();
    }

    public int findPosition(final Year yearToFind) {
        for (int i = 0; i < years.size(); i++) {
            Year year = years.get(i);

            if (year.getYear() == yearToFind.getYear()) {
                return i;
            }
        }

        return -1;
    }
}
