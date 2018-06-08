package com.aston.tanion.schedule.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.database.TasksLab;
import com.aston.tanion.schedule.model.TaskItem;
import com.aston.tanion.schedule.utility.ActivityFragmentInteractionListener;
import com.aston.tanion.schedule.utility.CommonMethod;
import com.aston.tanion.schedule.utility.Constant;
import com.aston.tanion.schedule.utility.MasterDetailFragment;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Aston Tanion on 13/04/2016.
 */
public class TaskCompletedDetailFragment extends MasterDetailFragment<TaskListFragment> {
    private static final String TAG ="CompletedDetailFragment";
    private static final String ARG_ID = "id";

    public static final String EXTRA_STATE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.STATE";
    public static final String EXTRA_UUID =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.UUID";
    public static final String EXTRA_ITEM_REQUEST =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.ITEM_REQUEST";

    private TaskItem mItem;
    private SimpleDateFormat mDateFormat;
    private Resources mResources;
    private Fragment mMasterListFragment = null;
    private boolean mIsUsedAsMasterDetail = false;
    private ActivityFragmentInteractionListener mInteractionListener;

    public static TaskCompletedDetailFragment newInstance(UUID uuid) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ID, uuid);

        TaskCompletedDetailFragment fragment = new TaskCompletedDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mInteractionListener = (ActivityFragmentInteractionListener) context;
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
                mInteractionListener = (ActivityFragmentInteractionListener) activity;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResources = getResources();
        UUID id = (UUID) getArguments().getSerializable(ARG_ID);
        mItem = TasksLab.get(getActivity()).getItem(id);
        mDateFormat = new SimpleDateFormat(
                mResources.getString(R.string.date_format),
                Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_completed_detail, container, false);

        TextView subjectTextView = (TextView) view.findViewById(R.id.task_complete_detail_subject);
        subjectTextView.setText(String.format(
                getResources().getString(R.string.field_value_format),
                mResources.getString(R.string.title),
                mItem.getTitle()));

        TextView locationTextView = (TextView) view.findViewById(R.id.task_complete_detail_location);
        locationTextView.setText(String.format(
                getResources().getString(R.string.field_value_format),
                mResources.getString(R.string.location),
                mItem.getLocation()));

        TextView setTextView = (TextView) view.findViewById(R.id.task_complete_detail_set_on);
        setTextView.setText(String.format(
                getResources().getString(R.string.field_value_format),
                mResources.getString(R.string.due_on),
                mDateFormat.format(mItem.getDueDate())));

        TextView completeTextView = (TextView)
                view.findViewById(R.id.task_complete_detail_completed_on);
        completeTextView.setText(String.format(
                getResources().getString(R.string.field_value_format),
                mResources.getString(R.string.complete_on),
                mDateFormat.format(mItem.getCompleteDate())));

        EditText messageEditText = (EditText) view.findViewById(R.id.task_complete_detail_message);
        messageEditText.setText(
                CommonMethod.readFile(getActivity(), mItem.getUUID().toString()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMasterListFragment != null) {
            mInteractionListener.onFragmentDetailReady(mMasterListFragment, this);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!mIsUsedAsMasterDetail) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_UUID, mItem.getUUID());
            intent.putExtra(EXTRA_STATE, mItem.getState());
            intent.putExtra(EXTRA_ITEM_REQUEST, Constant.REQUEST_ITEM_CREATE);
            // send this to the caller activity
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }

    @Override
    protected void onUseAsMasterDetail(boolean isMasterDetail) {
        mIsUsedAsMasterDetail = isMasterDetail;
    }

    @Override
    protected void onMasterListUsed(TaskListFragment fragment) {
        mMasterListFragment = fragment;
    }
}