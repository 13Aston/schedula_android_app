package com.aston.tanion.schedule.utility;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Aston Tanion on 03/08/2016.
 */
public interface OnStartDragListener {

    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}