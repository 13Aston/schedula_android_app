package com.aston.tanion.schedule.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.database.SharedPrefs;
import com.aston.tanion.schedule.database.TimetableLab;
import com.aston.tanion.schedule.database.WeekLab;
import com.aston.tanion.schedule.model.Day;
import com.aston.tanion.schedule.model.TimetableItem;
import com.aston.tanion.schedule.model.WeekItem;
import com.aston.tanion.schedule.service.AlarmService;
import com.aston.tanion.schedule.service.DateChangeService;
import com.aston.tanion.schedule.utility.Constant;

import java.util.List;

/**
 * Created by Aston Tanion on 06/08/2016.
 */
public class WeekDetailFragment extends Fragment {
    public static final String TAG = "WeekDetailFragment";
    private static final String ARG_POSITION = "id";
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onUpdateList();
    }

    private TextInputEditText mNameEditText;
    private CheckBox mCheckBox;
    private RecyclerView mRecyclerView;

    private Context mContext;
    private WeekItem mItem;
    private SharedPrefs mPrefs;

    public static WeekDetailFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        WeekDetailFragment fragment = new WeekDetailFragment();
        fragment.setArguments(args);
        return fragment;
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
        int position = getArguments().getInt(ARG_POSITION);
        mContext = getActivity();

        mItem = WeekLab.get(mContext).getItem(position);
        mPrefs = SharedPrefs.get(mContext);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_week_update_view, container, false);

        // Initialise the name container
        mNameEditText = (TextInputEditText) view.findViewById(R.id.week_detail_name);
        mNameEditText.setText(mItem.getTitle());
        mNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean haveBeenHandled = false;

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager)
                            mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
                    haveBeenHandled = true;
                }

                return haveBeenHandled;
            }
        });

        // Initialise the check box
        mCheckBox = (CheckBox) view.findViewById(R.id.week_detail_check_box);
        if (mItem.getUUID().toString().equals(mPrefs.read(Constant.WEEK_CURRENT_ID_PREF, ""))) {
            mCheckBox.setChecked(true);
        }

        // Initialise the update button
        Button update = (Button) view.findViewById(R.id.week_detail_update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mNameEditText.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(mContext, R.string.error_title_required, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                // Get all week in the database
                List<WeekItem> weeks = WeekLab.get(mContext).getItems();

                //Loop though weeks and check that weeks don't have the same title.
                for (int i = 0; i < weeks.size(); i++) {
                    if (weeks.get(i).getUUID().equals(mItem.getUUID())) continue;
                    if (weeks.get(i).getTitle().equals(title)) {

                        Toast.makeText(
                                mContext,
                                R.string.error_name_existent,
                                Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                }

                mItem.setTitle(title);
                WeekLab.get(mContext).updateItem(mItem);

                if (mCheckBox.isChecked()) {
                    String lastWeek = (String)
                            mPrefs.read(Constant.WEEK_CURRENT_ID_PREF, "");

                    // Update the week
                    DateChangeService.updateWeek(mContext, mItem);

                    // Initialise the alarms
                    new AlarmService.InitTimetableAlarmThread(mContext, lastWeek, true).start();
                }

                // Remove the keyboard
                InputMethodManager imm = (InputMethodManager)
                        mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);

                mCallbacks.onUpdateList();
            }
        });

        // Initialise the recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(new ViewAdapter());

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mDayTextView;
        private TextView mContentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mDayTextView = (TextView) itemView.findViewById(R.id.week_info_item_day);
            mContentTextView = (TextView) itemView.findViewById(R.id.week_info_item_content);
        }

        private void bindView(int position) {
            String day = Day.values()[position].toString();
            List<TimetableItem> items =
                    TimetableLab.get(mContext).getItems(day, mItem.getUUID().toString());

            String plural = (items.size() > 1) ? "s" : "";

            String info = String.format(
                    getResources().getString(R.string.week_detail_info_content),
                    items.size(),
                    plural);

            mDayTextView.setText(day);
            mContentTextView.setText(info);
        }
    }

    private class ViewAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup container, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.week_info_item, container, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindView(position);
        }

        @Override
        public int getItemCount() {
            return 7;
        }
    }
}
