package com.aston.tanion.schedule.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aston.tanion.schedule.Handler.DatabaseHandler;
import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.database.SharedPrefs;
import com.aston.tanion.schedule.model.WeekItem;
import com.aston.tanion.schedule.utility.ActivityFragmentInteractionListener;
import com.aston.tanion.schedule.utility.Constant;

import java.util.List;

/**
 * Created by Aston Tanion on 30/03/2016.
 */
public class WeekPreviewDialog extends DialogFragment {
    public static final String TAG = "WeekPreviewDialog";
    public static final String EXTRA_TITLE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.dialog.TITLE";
    public static final String EXTRA_WEEK_ID =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.dialog.WEEK_ID";

    private ActivityFragmentInteractionListener mCallbacks;

    private ItemAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DatabaseHandler<WeekItem> mDbHandler;
    private Context mContext;
    private SharedPrefs mPrefs;
    private String mTitle = "";
    private String mWeekPreviewIdString = "";
    private Resources mResources;

    public static WeekPreviewDialog newInstance() {
        return new WeekPreviewDialog();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallbacks = (ActivityFragmentInteractionListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            try {
                mCallbacks = (ActivityFragmentInteractionListener) activity;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mResources = getResources();
        mPrefs = SharedPrefs.get(mContext);

        // This handler is user to update the recycler view.
        Handler resultHandler = new Handler();
        mDbHandler = new DatabaseHandler<>(mContext, resultHandler, null, null, null);
        mDbHandler.setOnItemRequest(new DatabaseHandler.RequestItems() {
            @Override
            public void onItemsRequest(List<?> items) {
                setupAdapter((List<WeekItem>) items);
            }
        });
        mDbHandler.start();
        mDbHandler.getLooper();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_week_priview, null, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.week_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // This method request the list of item from the database.
        // Call the above handler to update the recycler view.
        mDbHandler.queueRequest(
                null,
                Constant.IDENTIFIER_WEEK,
                Constant.DATABASE_GET_ITEMS);

        return new AlertDialog.Builder(mContext)
                .setView(view)
                .create();
    }

    /**
     * Set up the recycler view's adapter
     * @param items the list of week to populate the
     *              recycler view
     */
    private void setupAdapter(List<WeekItem> items) {
        if (mAdapter == null) {
            mAdapter = new ItemAdapter(items);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setItem(items);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void sendResult() {
        Intent data = new Intent();
        data.putExtra(EXTRA_WEEK_ID, mWeekPreviewIdString);
        data.putExtra(EXTRA_TITLE, mTitle);

        mCallbacks.onDialogFragmentResult(Constant.REQUEST_WEEK_PREVIEW_DIALOG, data);
    }

    @Override
    public void onDetach() {

        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        private TextView mWeekNameTextView;
        private TextView mDividerLine;

        private View mView;
        private WeekItem mItem;

        public ItemHolder(final View itemView) {
            super(itemView);
            mView = itemView;
            mWeekNameTextView = (TextView) mView.findViewById(R.id.week_recycler_view_name);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTitle = mItem.getTitle();
                    mWeekPreviewIdString = mItem.getUUID().toString();
                    // Let the user know that we are now in week preview.
                    Toast.makeText(
                            mContext,
                            mResources.getString(R.string.toast_enter_preview),
                            Toast.LENGTH_SHORT)
                            .show();

                    sendResult();

                }
            });
        }

        public void bindItems(WeekItem item) {
            mItem = item;
            mWeekNameTextView.setText(item.getTitle());

            mDividerLine = (TextView) mView.findViewById(R.id.week_recycler_view_divider_line);

            // Set the color of the current week as blue.
            if (item.getUUID().toString().equals(mPrefs.read(Constant.WEEK_CURRENT_ID_PREF, ""))) {
                mDividerLine.setBackgroundColor(mResources.getColor(R.color.colorAccent));
                mWeekNameTextView.setTextColor(mResources.getColor(R.color.colorAccent));
            }
            // Keep the other week color as black.
            else {
                mDividerLine.setBackgroundColor(mResources.getColor(android.R.color.black));
                mWeekNameTextView.setTextColor(mResources.getColor(android.R.color.black));
            }
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {
        private List<WeekItem> mItems;

        public ItemAdapter(List<WeekItem> items) {
            mItems = items;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.week_recycler_view_item, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            WeekItem item = mItems.get(position);
            holder.bindItems(item);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public void setItem(List<WeekItem> items) {
            mItems = items;
        }
    }
}