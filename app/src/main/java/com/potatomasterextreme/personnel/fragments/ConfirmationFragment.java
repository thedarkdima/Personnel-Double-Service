package com.potatomasterextreme.personnel.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.event.CustomMessagesActivity;
import com.potatomasterextreme.personnel.event.EventGroupActivity;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ConfirmationFragment extends Fragment implements View.OnClickListener {

    ImageView edit;
    TextView dateView;
    public EditText editText;
    public EditText editReminder;

    String tempDate;
    String date;

    public String getDate() {
        return date;
    }

    String tempTime;
    String time;

    public String getTime() {
        return time;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_confirmation, container, false);

        dateView = view.findViewById(R.id.date);
        editText = view.findViewById(R.id.message_edit_text);
        editReminder = view.findViewById(R.id.edit_reminder);
        edit = view.findViewById(R.id.edit_date);

        edit.setOnClickListener(this);
        editText.setOnClickListener(this);
        view.findViewById(R.id.edit_text_view).setOnClickListener(this);

        EventGroupActivity parent = (EventGroupActivity) getActivity();
        HashMap<String, String> tempGroup = parent.eventGroupFragment.tempGroup;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        Date currentTime = Calendar.getInstance().getTime();
        if (!tempGroup.containsKey("group_status")) {
            date = dateFormat.format(currentTime);
            time = timeFormat.format(currentTime);
        } else {
            editText.setText(tempGroup.get("group_message"));
            editReminder.setText(tempGroup.get("remind_every"));
            if (currentTime.getTime() < Long.parseLong(tempGroup.get("send_date"))) {
                date = dateFormat.format(Long.parseLong(tempGroup.get("send_date")));
                time = timeFormat.format(Long.parseLong(tempGroup.get("send_date")));
                dateView.setText(displayFormat.format(Long.parseLong(tempGroup.get("send_date"))));
            } else {
                date = dateFormat.format(currentTime);
                time = timeFormat.format(currentTime);
                dateView.setText(getString(R.string.immediately));
            }
        }

        final View mRootView = getActivity().findViewById(R.id.main_layout);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (getActivity() != null) {
                    if (((EventGroupActivity) getActivity()).pagePosition == 2) {
                        Rect measureRect = new Rect(); //you should cache this, onGlobalLayout can get called often
                        mRootView.getWindowVisibleDisplayFrame(measureRect);
                        // measureRect.bottom is the position above soft keypad
                        int keypadHeight = mRootView.getRootView().getHeight() - measureRect.bottom;
                        BaseActivity baseActivity = (BaseActivity) getActivity();
                        if (keypadHeight > mRootView.getRootView().getHeight() / 3) {
                            // keyboard is opened
                            baseActivity.hideTitleBar();
                            if (editText.hasFocus()) {
                                view.findViewById(R.id.date_card).setVisibility(View.GONE);
                                view.findViewById(R.id.card_reminder).setVisibility(View.GONE);
                            }
                        } else {
                            //store keyboard state to use in onBackPress if you need to
                            baseActivity.showTitleBar();
                            view.findViewById(R.id.date_card).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.card_reminder).setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onClick(View view) {
        final AlertDialog.Builder builder;
        switch (view.getId()) {
            case R.id.message_edit_text:
                if (editText.getText().toString().equals(getString(R.string.confirmation_edit_text_message))) {
                    editText.setText("");
                }
                break;
            case R.id.edit_text_view:
                Intent intent = new Intent(getActivity(), CustomMessagesActivity.class);
                intent.putExtra("name", ((EventGroupActivity) getActivity()).groupName);
                startActivityForResult(intent, 3);
                break;
            case R.id.edit_date:
                builder = new AlertDialog.Builder(getActivity());

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                Date currentTime = Calendar.getInstance().getTime();
                tempDate = dateFormat.format(currentTime);
                tempTime = timeFormat.format(currentTime);

                final CalendarView calendarView = new CalendarView(getContext());
                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(CalendarView view, int year, int month,
                                                    int dayOfMonth) {
                        if (dayOfMonth < 10) {
                            tempDate = "0" + dayOfMonth;
                        } else {
                            tempDate = "" + dayOfMonth;
                        }
                        tempDate += "/" + (month + 1);
                        tempDate += "/" + year;
                    }
                });
                builder.setTitle("Select tempDate");
                builder.setView(calendarView);
                builder.setPositiveButton("select", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        final TimePicker timePicker = new TimePicker(getContext());
                        timePicker.setIs24HourView(true);
                        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                            @Override
                            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                                if (i < 10) {
                                    tempTime = "0" + i;
                                } else {
                                    tempTime = "" + i;
                                }

                                if (i1 < 10) {
                                    tempTime += ":0" + i1;
                                } else {
                                    tempTime += ":" + i1;
                                }
                            }
                        });
                        builder.setView(timePicker);
                        builder.setPositiveButton("select", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                date = tempDate;
                                time = tempTime;
                                dateView.setText(time + " " + date);
                            }
                        });
                        builder.show();
                    }
                });
                builder.show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            switch (requestCode) {
                case 3:
                    editText.setText(data.getStringExtra("message"));
                    editText.setSelection(editText.getText().length());
                    editText.requestFocus();
                    break;
            }
        }
    }
}
