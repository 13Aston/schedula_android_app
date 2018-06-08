package com.aston.tanion.schedule.utility;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.fragment.TaskListFragment;
import com.aston.tanion.schedule.fragment.TimetableListFragment;
import com.aston.tanion.schedule.model.Day;
import com.aston.tanion.schedule.model.State;

/**
 * Created by Aston Tanion on 09/08/2016.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = "ViewPagerAdapter";

    private Callbacks mCallbacks;

    public interface Callbacks {
        void onViewPagerFragmentReady(Fragment fragment, int position);
    }

    private SparseArray<Fragment> mMap = new SparseArray<>();
    private int mIdentifier = 0;
    private String mCurrentWeekId = "";

    private Resources mResources;

    public ViewPagerAdapter(Context context, FragmentManager fm, Callbacks callbacks) {
        super(fm);
        mResources = context.getResources();
        mCallbacks = callbacks;
    }

    @Override
    public Fragment getItem(int position) {
        if (mIdentifier == Constant.IDENTIFIER_TIMETABLE) {
            String day = Day.values()[position].toString();
            return TimetableListFragment.newInstance(day, mCurrentWeekId);
        } else if (mIdentifier == Constant.IDENTIFIER_TASK) {
            String state = State.values()[position].toString();
            return TaskListFragment.newInstance(state);
        } else {
            return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment listFragment = (Fragment) super.instantiateItem(container, position);
        mMap.put(position, listFragment);
        mCallbacks.onViewPagerFragmentReady(listFragment, position);
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment listFragment = mMap.get(position);
        if (listFragment != null) {
            Fragment detailFragment = listFragment.getChildFragmentManager()
                    .findFragmentById(R.id.fragment_detail_container);

            if (detailFragment != null) {
                listFragment.getChildFragmentManager()
                        .beginTransaction()
                        .remove(detailFragment)
                        .commit();
            }
        }

        mMap.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mIdentifier == Constant.IDENTIFIER_TIMETABLE) {
            String[] dayOFWeek = mResources.getStringArray(R.array.timetable_tab);
            return dayOFWeek[position];
        } else if (mIdentifier == Constant.IDENTIFIER_TASK) {
            return mResources.getStringArray(R.array.task_tab)[position];
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        if (mIdentifier == Constant.IDENTIFIER_TIMETABLE) return 7;
        else if (mIdentifier == Constant.IDENTIFIER_TASK) return 2;
        else return 0;
    }

    /**
     * Return the master list fragment withing the view pager's adapter.
     * @param position the position of the requested fragment.*/
    public Fragment getFragment(int position) {
        return mMap.get(position);
    }

    public void setIdentifier(int identifier) {
        mIdentifier = identifier;
    }

    public void setCurrentWeekId(String currentWeekId) {
        mCurrentWeekId = currentWeekId;
    }
}
