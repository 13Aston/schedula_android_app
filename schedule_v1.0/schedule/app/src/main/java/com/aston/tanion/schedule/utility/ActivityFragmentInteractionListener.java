package com.aston.tanion.schedule.utility;

import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by Aston Tanion on 08/08/2016.
 */
public interface ActivityFragmentInteractionListener {

    <L extends Fragment, D extends Fragment> void
    onFragmentDetailReady(L masterList, D masterDetail);

    /**
     * Listen to the menu event.
     * @param masterList an instance of the master list fragment which contains the detail menu.
     *                   This is used to access methods and fields and update the list
     * @param masterDetail an instance of master detail where the menu have been clicked.
     *                     This is used to access methods and fields and update the detail.
     */
    <L extends Fragment, D extends Fragment> void
    onFragmentDetailMenuClick(L masterList, D masterDetail);

    /**
     * listen to master list item click
     * @param masterList an instance of the master list fragment where the click happened.
     *                   This is used to access methods and fields and update the list
     * @param masterDetail an instance of the item (master detail) which have been clicked.
     *                     This is used to access methods and fields and update the detail.
     * */
    <L extends Fragment, D extends Fragment> void
    onFragmentListItemClick(L masterList, D masterDetail);

    /**
     * This is call when the dialog started from an activity want to send the result back to the
     * activity.
     * @param requestCode constant used to identify which dialog.
     * @param data the result data that need bo be sent back to the activity.
     * */
    void onDialogFragmentResult(int requestCode, Intent data);
}
