package com.aston.tanion.schedule.utility;

import android.support.v4.app.Fragment;

/**
 * Created by Aston Tanion on 08/08/2016.
 */
public abstract class MasterDetailFragment<F extends Fragment> extends Fragment {
    // Tell the master detail whether it can be use in landscape.
    protected abstract void onUseAsMasterDetail(boolean isMasterDetail);
    // Tell the master detail which master list fragment its is attached to.
    protected abstract void onMasterListUsed(F fragment);

    // Whether this fragment can be user as a master detail view (in landscape)
    public void useAsMasterDetail() {
        onUseAsMasterDetail(true);
    }

    // Set the master list fragment that the master detail fragment is attached to.
    public  void setMasterListUsed(F fragment) {
        onMasterListUsed(fragment);
    }
}
