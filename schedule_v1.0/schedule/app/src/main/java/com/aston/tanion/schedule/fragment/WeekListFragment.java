package com.aston.tanion.schedule.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aston.tanion.schedule.Handler.DatabaseHandler;
import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.activity.WeekActivity;
import com.aston.tanion.schedule.database.SharedPrefs;
import com.aston.tanion.schedule.database.TimetableLab;
import com.aston.tanion.schedule.database.WeekLab;
import com.aston.tanion.schedule.model.Day;
import com.aston.tanion.schedule.model.TimetableItem;
import com.aston.tanion.schedule.model.WeekItem;
import com.aston.tanion.schedule.service.AlarmService;
import com.aston.tanion.schedule.service.DateChangeService;
import com.aston.tanion.schedule.utility.Constant;
import com.aston.tanion.schedule.utility.ItemTouchHelperAdapter;
import com.aston.tanion.schedule.utility.ItemTouchHelperViewHolder;
import com.aston.tanion.schedule.utility.OnStartDragListener;
import com.aston.tanion.schedule.utility.SimpleItemTouchHelperCallback;

import java.util.Collections;
import java.util.List;

/**
 * Created by Aston Tanion on 07/06/2016.
 */
public class WeekListFragment extends Fragment implements OnStartDragListener {
    public static final String TAG = "WeekFragment";

    private static Callbacks mCallbacks;

    public interface Callbacks {
        void onItemSelected(WeekItem item);
        void onUpdateList();
        void onViewCreated();
    }

    private RecyclerView mRecyclerView;
    private static FloatingActionButton mFAB;

    // classes
    private SharedPrefs mPrefs;
    private DatabaseHandler<WeekItem> mDbHandler;
    private ItemAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    // general
    private Context mContext;
    private Resources mResources;

    public static WeekListFragment newInstance() {
        return new WeekListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            mCallbacks = (Callbacks) activity;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view_fab, container, false);

        // Initialise the empty recycler view
        TextView emptyRecyclerTextView = (TextView) view.findViewById(R.id.empty_recycler_view);
        emptyRecyclerTextView.setVisibility(View.GONE);

        // Initialise the FAB
        mFAB = (FloatingActionButton)
                view.findViewById(R.id.floating_action_button);
        mFAB.setVisibility(View.GONE);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = showAddDialog(mContext);
                dialog.show();
            }
        });

        // Initialise the recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // This method request the list of item in the database.
        // Call the above handler to update the recycler view.
        mDbHandler.queueRequest(
                null,
                Constant.IDENTIFIER_WEEK,
                Constant.DATABASE_GET_ITEMS);

        mCallbacks.onViewCreated();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // This method request the list of item in the database.
        // Call the above handler to update the recycler view.
        mDbHandler.queueRequest(
                null,
                Constant.IDENTIFIER_WEEK,
                Constant.DATABASE_GET_ITEMS);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private void setupAdapter(List<WeekItem> items) {
        if (mAdapter == null) {
            mAdapter = new ItemAdapter(this, items);
            mRecyclerView.setAdapter(mAdapter);

            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        } else {
            mAdapter.setItem(items);
            mAdapter.notifyDataSetChanged();
        }
    }

    public static AlertDialog showAddDialog(final Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_week_update_view, null, false);

        Button update = (Button) view.findViewById(R.id.week_detail_update);
        update.setVisibility(View.GONE);

        ViewGroup infoGroup = (ViewGroup) view.findViewById(R.id.week_detail_info);
        infoGroup.setVisibility(View.GONE);

        final EditText nameEditText = (EditText)
                view.findViewById(R.id.week_detail_name);
        nameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean haveBeenHandled = false;

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    InputMethodManager imm = (InputMethodManager)
                            context.getSystemService(context.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);

                    haveBeenHandled = true;
                }

                return haveBeenHandled;
            }
        });
        final CheckBox currentWeekCheckBox = (CheckBox)
                view.findViewById(R.id.week_detail_check_box);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!nameEditText.getText().toString().isEmpty()) {

                            List<WeekItem> weeks = WeekLab
                                    .get(context).getItems();

                            WeekItem item = new WeekItem();
                            item.setTitle(nameEditText.getText().toString());
                            item.setPosition(weeks.size());

                            //Loop though weeks and check that weeks don't have the same title
                            for (int i = 0; i < weeks.size(); i++) {
                                if (weeks.get(i).getTitle().equals(item.getTitle())) {
                                    Toast.makeText(
                                            context,
                                            R.string.error_name_existent,
                                            Toast.LENGTH_SHORT)
                                            .show();
                                    return;
                                }
                            }

                            // This handler is user to update the recycler view.
                            Handler resultHandler = new Handler();
                            DatabaseHandler dbHandler = new DatabaseHandler<>(
                                    context, resultHandler, null, null, null);
                            dbHandler.start();
                            dbHandler.getLooper();

                            // Add the new week to the database
                            dbHandler.queueRequest(
                                    item,
                                    Constant.IDENTIFIER_WEEK,
                                    Constant.DATABASE_ADD_ITEM);

                            mCallbacks.onUpdateList();

                            if (currentWeekCheckBox.isChecked()) {
                                String lastWeek = (String)
                                        SharedPrefs.get(context)
                                                .read(Constant.WEEK_CURRENT_ID_PREF, "");

                                // Update the week
                                DateChangeService.updateWeek(context, item);

                                // Initial all alarms.
                                new AlarmService.InitTimetableAlarmThread(
                                        context, lastWeek, true).start();
                            }

                        }
                    }
                })
                .setNeutralButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

        return builder.create();
    }

    private class ItemHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        private TextView mWeekNameTextView;
        private TextView mDividerLine;
        private View mView;

        private WeekItem mItem;

        public ItemHolder(final View itemView) {
            super(itemView);
            mView = itemView;

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallbacks.onItemSelected(mItem);
                }
            });
        }

        public void bindItems(WeekItem item) {
            mItem = item;

            mWeekNameTextView = (TextView) mView.findViewById(R.id.week_recycler_view_name);
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

            mWeekNameTextView.setText(item.getTitle());
        }

        @Override
        public void onItemSelected() {
            mView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            mView.setBackgroundColor(0);
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder> implements ItemTouchHelperAdapter {
        private List<WeekItem> mItems;
        private final OnStartDragListener mDragListener;

        public ItemAdapter(OnStartDragListener onStartDragListener, List<WeekItem> items) {
            mItems = items;
            mDragListener = onStartDragListener;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.week_recycler_view_item, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(final ItemHolder holder, int position) {
            WeekItem item = mItems.get(position);
            holder.bindItems(item);

            holder.mView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_BUTTON_PRESS) {
                        mDragListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mItems, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mItems, i, i - 1);
                }
            }

            for (int i = 0; i < mItems.size(); i++) {
                WeekItem item = mItems.get(i);
                item.setPosition(i);

                // Update the position if the current week have moved.
                if (item.getUUID().toString()
                        .equals(mPrefs.read(Constant.WEEK_CURRENT_ID_PREF, ""))) {

                    mPrefs.write(Constant.WEEK_CURRENT_POSITION_PREF, item.getPosition());
                }

                mDbHandler.queueRequest(
                        item,
                        Constant.IDENTIFIER_WEEK,
                        Constant.DATABASE_UPDATE_ITEM);

            }

            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {
            WeekItem weekItem = mItems.get(position);

            // Check if this week is the current week.
            // Note : You can't delete the current week.
            if (!weekItem.getUUID().toString()
                    .equals(mPrefs.read(Constant.WEEK_CURRENT_ID_PREF, ""))) {

                DatabaseHandler<TimetableItem> dbHandler = new DatabaseHandler<>(
                        mContext, null, null, weekItem.getUUID().toString(), null);
                dbHandler.start();
                dbHandler.getLooper();

                // Get this week event by day (to be used if the user have undo the snack bar).
                List<TimetableItem>[] lists = new List[Day.values().length];

                // Loop through the days of this week.
                for (int i = 0; i < lists.length; i++) {
                    // Get the each day
                    String day = Day.values()[i].toString();
                    // Get the list of event of each day.
                    List<TimetableItem> items = TimetableLab.get(mContext)
                            .getItems(day, weekItem.getUUID().toString());

                    // Save it into this array (to be used if the user have undo the snack bar).
                    lists[i] = items;

                    // Loop through each event of the current day.
                    for (int j = 0; j < items.size(); j++) {
                        // Remove each event from the database.
                        dbHandler.queueRequest(
                                items.get(j),
                                Constant.IDENTIFIER_TIMETABLE,
                                Constant.DATABASE_REMOVE_ITEM);
                    }

                }

                // Show a snack bar.
                Snackbar snackbar = Snackbar.make(
                        WeekActivity.getSnackBarView(),
                        mResources.getString(R.string.snack_bar_delete_message),
                        Snackbar.LENGTH_LONG);
                snackbar.show();
                snackbar.setAction(
                        mResources.getString(R.string.snack_bar_delete_action),
                        new SnackBarActionListener(weekItem, lists));

                // Remove this week from the database
                mDbHandler.queueRequest(
                        weekItem,
                        Constant.IDENTIFIER_WEEK,
                        Constant.DATABASE_REMOVE_ITEM);

                // Remove the item for the recycler view
                mItems.remove(position);
                notifyItemRemoved(position);

                // Update the position of all week.
                for (int i = 0; i < mItems.size(); i++) {
                    WeekItem item = mItems.get(i);
                    item.setPosition(i);

                    mDbHandler.queueRequest(
                            item,
                            Constant.IDENTIFIER_WEEK,
                            Constant.DATABASE_UPDATE_ITEM);
                }

            } else {
                // Update the the recycler view
                mDbHandler.queueRequest(
                        null,
                        Constant.IDENTIFIER_WEEK,
                        Constant.DATABASE_GET_ITEMS);

                Toast.makeText(
                        mContext,
                        mResources.getString(R.string.toast_delete_current_week),
                        Toast.LENGTH_SHORT).show();
            }
        }

        public void setItem(List<WeekItem> items) {
            mItems = items;
        }
    }

    private class SnackBarActionListener implements View.OnClickListener {
        private WeekItem mWeekItem;
        private List<TimetableItem>[] mLists;
        public SnackBarActionListener(WeekItem week, List<TimetableItem> ...lists) {
            mWeekItem = week;
            mLists = lists;
        }

        @Override
        public void onClick(View v) {
            // Add the week to the database
            mDbHandler.queueRequest(
                    mWeekItem,
                    Constant.IDENTIFIER_WEEK,
                    Constant.DATABASE_ADD_ITEM);

            // Loop through each day of the week
            for (int i = 0; i < mLists.length; i++) {
                // Get the day
                String day = Day.values()[i].toString();
                DatabaseHandler<TimetableItem> dbHandler = new DatabaseHandler<>(
                        mContext, null, day, mWeekItem.getUUID().toString(), null);
                dbHandler.start();
                dbHandler.getLooper();

                // Loop through each events  of the corresponding day
                for (int j = 0; j < mLists[i].size(); j++) {
                    // get the events.
                    TimetableItem item = mLists[i].get(j);
                    // Add each event in the database.
                    dbHandler.queueRequest(
                            item,
                            Constant.IDENTIFIER_TIMETABLE,
                            Constant.DATABASE_ADD_ITEM);
                }
            }

            // Update the recycler view
            mDbHandler.queueRequest(
                    null,
                    Constant.IDENTIFIER_WEEK,
                    Constant.DATABASE_GET_ITEMS);

        }
    }

    public static void setFABVisibility(int visibility) {
        if (mFAB != null) {
            mFAB.setVisibility(visibility);
        }
    }
}