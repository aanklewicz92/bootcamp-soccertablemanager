package com.droidsonroids.bootcamp.soccertablemanager.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.droidsonroids.bootcamp.soccertablemanager.Constants;
import com.droidsonroids.bootcamp.soccertablemanager.R;
import com.droidsonroids.bootcamp.soccertablemanager.api.model.Table;
import com.droidsonroids.bootcamp.soccertablemanager.event.CreateTableRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.CreateTableResponseEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.GetTablesRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.GetTablesResponseEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.JoinRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.JoinResponseEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.LeaveRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.LeaveResponseEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.RegisterRequestEvent;
import com.droidsonroids.bootcamp.soccertablemanager.event.RegisterResponseEvent;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    @Bind(R.id.edit_text_user_name)
    public EditText mEditTextUserName;
    @Bind(R.id.edit_text_join_table_id)
    public EditText mEditTextJoinTableId;
    @Bind(R.id.edit_text_leave_table_id)
    public EditText mEditTextLeaveTableId;
    @Bind(R.id.edit_text_reservation_time)
    public EditText mEditTextReservationTime;
    @Bind(R.id.text_view_user_id)
    public TextView mTextViewUserId;
    @Bind(R.id.list_view_tables)
    public ListView mListViewTables;

    private int mUserId;
    private String mUserName;
    private List<Table> mTables = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        setListViewListener();
    }

    @OnClick(R.id.button_register)
    public void onClickRegister() {
        if (mEditTextUserName.getText().toString().length() > 0) {
            mUserName = mEditTextUserName.getText().toString();
            EventBus.getDefault().post(new RegisterRequestEvent(mUserName));
        } else
            Toast.makeText(this, R.string.empty_name, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_create_table)
    public void onClickCreateTable() {
        if (mEditTextReservationTime.getText().toString().length() > 0)
            EventBus.getDefault().post(new CreateTableRequestEvent(mEditTextReservationTime.getText().toString(), mUserId));
        else
            Toast.makeText(this, R.string.empty_time, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_refresh)
    public void onClickRefresh() {
        EventBus.getDefault().post(new GetTablesRequestEvent());
    }

    @OnClick(R.id.button_join_table)
    public void onClickJoin() {
        if (mEditTextJoinTableId.getText().toString().length() > 0) {
            int selectedTableId = Integer.parseInt(mEditTextJoinTableId.getText().toString());
            Table selectedTable = null;
            for (Table table : mTables)
                if (table.getTableId() == selectedTableId)
                    selectedTable = table;
            if (selectedTable != null) {
                for (String name : selectedTable.getUserNameList())
                    if (name.equals(mUserName)) {
                        Toast.makeText(this, R.string.already_in, Toast.LENGTH_SHORT).show();
                        return;
                    }
            }
            EventBus.getDefault().post(new JoinRequestEvent(selectedTableId, mUserId));
        } else
            Toast.makeText(this, R.string.empty_table_id, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_leave_table)
    public void onClickLeave() {
        if (mEditTextLeaveTableId.getText().toString().length() > 0)
            EventBus.getDefault().post(new LeaveRequestEvent(Integer.parseInt(mEditTextLeaveTableId.getText().toString()), mUserId));
        else
            Toast.makeText(this, R.string.empty_table_id, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        onClickRefresh();
    }

    @Override
    protected void onResume() {
        loadUserDataFromSharedPrefs();
        super.onResume();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(RegisterResponseEvent event) {
        if (event.getApiError() == null) {
            onRegistrationSuccess(event);
        } else {
            onRegistrationError(event);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CreateTableResponseEvent event) {
        if (event.getApiError() == null) {
            onCreateTableSuccess(event);
        } else {
            onCreateTableError(event);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(GetTablesResponseEvent event) {
        if (event.getApiError() == null) {
            onGetTablesSuccess(event);
        } else {
            onGetTablesError(event);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(JoinResponseEvent event) {
        if (event.getApiError() == null) {
            onJoinTableSuccess();
        } else {
            onJoinTableError(event);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LeaveResponseEvent event) {
        if (event.getApiError() == null) {
            onLeaveTableSuccess();
        } else {
            onLeaveTableError(event);
        }
    }

    private void onRegistrationSuccess(RegisterResponseEvent event) {
        mUserId = event.getUserId();
        Toast.makeText(this, getString(R.string.register_success) + " id: " + mUserId, Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putInt(Constants.SHARED_PREFS_USER_ID, mUserId).apply();
        sharedPreferences.edit().putString(Constants.SHARED_PREFS_USER_NAME, mUserName).apply();
        mTextViewUserId.setText(getString(R.string.user_id_label) + mUserId);
    }

    private void onRegistrationError(RegisterResponseEvent event) {
        Toast.makeText(this, R.string.register_error, Toast.LENGTH_SHORT).show();
        Log.e(TAG, event.getApiError().getErrorCode() + " " + event.getApiError().getErrorMessage());
    }

    private void onCreateTableSuccess(CreateTableResponseEvent event) {
        int tableId = event.getTableId();
        Toast.makeText(this, getString(R.string.create_table_success) + " id: " + tableId, Toast.LENGTH_SHORT).show();
        populateListView();
    }

    private void onCreateTableError(CreateTableResponseEvent event) {
        Toast.makeText(this, R.string.create_table_error, Toast.LENGTH_SHORT).show();
        Log.e(TAG, event.getApiError().getErrorCode() + " " + event.getApiError().getErrorMessage());
    }

    private void onGetTablesSuccess(GetTablesResponseEvent event) {
        mTables = event.getTables();
        populateListView();
        Toast.makeText(this, R.string.get_tables_success, Toast.LENGTH_SHORT).show();
    }

    private void onGetTablesError(GetTablesResponseEvent event) {
        Toast.makeText(this, R.string.get_tables_error, Toast.LENGTH_SHORT).show();
        Log.e(TAG, event.getApiError().getErrorCode() + " " + event.getApiError().getErrorMessage());
    }

    private void onJoinTableSuccess() {
        onClickRefresh();
        Toast.makeText(this, R.string.join_table_success, Toast.LENGTH_SHORT).show();
    }

    private void onJoinTableError(JoinResponseEvent event) {
        Toast.makeText(this, R.string.join_table_error, Toast.LENGTH_SHORT).show();
        Log.e(TAG, event.getApiError().getErrorCode() + " " + event.getApiError().getErrorMessage());
    }

    private void onLeaveTableSuccess() {
        onClickRefresh();
        Toast.makeText(this, R.string.leave_table_success, Toast.LENGTH_SHORT).show();
    }

    private void onLeaveTableError(LeaveResponseEvent event) {
        Toast.makeText(this, R.string.leave_table_error, Toast.LENGTH_SHORT).show();
        Log.e(TAG, event.getApiError().getErrorCode() + " " + event.getApiError().getErrorMessage());
    }

    private void loadUserDataFromSharedPrefs() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int userIdFromSharedPreferences = sharedPreferences.getInt(Constants.SHARED_PREFS_USER_ID, -1);
        if (userIdFromSharedPreferences != -1) {
            mTextViewUserId.setText(getString(R.string.user_id_label) + " " + userIdFromSharedPreferences);
            mUserId = userIdFromSharedPreferences;
            mUserName = sharedPreferences.getString(Constants.SHARED_PREFS_USER_NAME, "");
            mEditTextUserName.setText(mUserName);
        }
    }

    private void setListViewListener() {
        mListViewTables.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), mTables.get(position).toStringLong(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateListView() {
        ArrayList<String> tablesInfo = new ArrayList<>();
        for (Table table : mTables)
            tablesInfo.add(table.toStringShort());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, tablesInfo);
        mListViewTables.setAdapter(adapter);
    }
}
