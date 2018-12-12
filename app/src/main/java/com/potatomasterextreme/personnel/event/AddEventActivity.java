package com.potatomasterextreme.personnel.event;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.potatomasterextreme.personnel.R;
import com.potatomasterextreme.personnel.infrastructure.BaseActivity;
import com.potatomasterextreme.personnel.infrastructure.DataManager;
import com.potatomasterextreme.personnel.infrastructure.FileManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AddEventActivity extends BaseActivity {

    private EditText eventName;
    private EditText eventDesc;
    private EditText workerCount;
    private EditText sHour;
    private EditText sMinute;
    private EditText eHour;
    private EditText eMinute;
    private CalendarView calendar;
    private String finalDate;
    private int id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        eventName = (EditText) findViewById(R.id.event_name);
        eventName.requestFocus();
        eventDesc = (EditText) findViewById(R.id.event_desc);

        eventDesc.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        eventDesc.setRawInputType(InputType.TYPE_CLASS_TEXT);

        workerCount = (EditText) findViewById(R.id.workers_count);
        sHour = (EditText) findViewById(R.id.b_time_hour);
        sMinute = (EditText) findViewById(R.id.b_time_minute);
        eHour = (EditText) findViewById(R.id.e_time_hour);
        eMinute = (EditText) findViewById(R.id.e_time_minute);
        calendar = (CalendarView) findViewById(R.id.calendar);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        finalDate = formatter.format(date);

        time_change();

        edit();

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month,
                                            int date) {
                if (date / 10 == 0) {
                    finalDate = "0" + date + "/" + (month + 1) + "/" + year;
                } else {
                    finalDate = date + "/" + (month + 1) + "/" + year;
                }
            }
        });
    }

    //Placed here to make a check in the menu
    HashMap<String, String> event;

    private void edit() {
        //Checks if trying to edit the event and not add
        event = gson.fromJson(getIntent().getStringExtra("event"), HashMap.class);
        if (event != null) {
            if (event.containsKey("name")) {
                eventName.setText(event.get("name"));
            }

            if (event.containsKey("desc")) {
                eventDesc.setText(event.get("desc"));
            }

            if (event.containsKey("worker_count")) {
                workerCount.setText(event.get("worker_count"));
            }

            if (event.containsKey("time")) {
                try {
                    String tempTime = event.get("time");
                    sHour.setText(tempTime.substring(0, 3));
                    sMinute.setText(tempTime.substring(3, 5));
                    eHour.setText(tempTime.substring(6, 8));
                    eMinute.setText(tempTime.substring(9, 11));
                } catch (Exception e) {
                    //Do nothing
                }
            }

            if (event.containsKey("date")) {
                try {
                    Date theSameDate = new SimpleDateFormat("dd/MM/yyyy").parse(event.get("date"));
                    calendar.setDate(theSameDate.getTime());
                    finalDate = event.get("date");
                } catch (Exception e) {

                }
            }

            if (event.containsKey("id")) {
                id = Integer.parseInt(event.get("id"));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Add the add menu button
        getMenuInflater().inflate(R.menu.add_menu, menu);
        if (event != null) {
            menu.findItem(R.id.add_menu_button).setTitle(getString(R.string.edit));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_menu_button:
                //Getting all the data from the activity
                //id
                //name
                //desc
                //worker_count
                //time
                //date
                HashMap<String, String> event = new HashMap<>();
                if (eventName.getText().toString().trim().length() > 0) {
                    //Title is a must check if it exists
                    event.put("name", eventName.getText().toString());
                    if (eventDesc.getText().toString().trim().length() > 0) {
                        event.put("desc", eventDesc.getText().toString());
                    }
                    if (workerCount.getText().toString().trim().length() > 0) {
                        event.put("worker_count", workerCount.getText().toString());
                    }
                    String string = sHour.getText().toString().trim();
                    //Fixes empty areas in time and then adds it into the event if start time is available
                    if (string.length() > 0) {
                        if (string.length() == 1) {
                            string = "0" + string;
                        }
                        string += ":";
                        if (sMinute.getText().toString().length() == 0) {
                            string += "00";
                        } else {
                            if (sMinute.getText().toString().length() == 1) {
                                string += "0";
                            }
                            string += sMinute.getText().toString();
                        }
                        if (eHour.getText().toString().length() > 0) {
                            string += "-";
                            if (eHour.getText().toString().length() == 1) {
                                string += "0";
                            }
                            string += eHour.getText().toString();
                            string += ":";
                            if (eMinute.getText().toString().length() == 0) {
                                string += "00";
                            } else {
                                if (eMinute.getText().toString().length() == 1) {
                                    string += "0";
                                }
                                string += eMinute.getText().toString();
                            }
                        }
                        event.put("time", string);
                    }
                    event.put("date", finalDate);
                    //Adding the id to the event
                    HashMap<String, HashMap<String, String>> finishedEvent = new HashMap<>();
                    if (id == -1) {
                        id = getEventID();
                    }
                    event.put("id", String.valueOf(id));
                    finishedEvent.put(String.valueOf(id), event);
                    //Saving the event to a file
                    FileManager.fileManager.add(this, FileManager.WhatToDo.ADD, getString(R.string.events_file), finishedEvent);
                    DataManager.updateEvents(this);
                    //Closing the page
                    finish();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.title_waring), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private int getEventID() {
        int id;
        if (!preferences.contains("event_id")) {
            id = 0;
            editor.putInt("event_id", 0);
            editor.apply();
        } else {
            id = preferences.getInt("event_id", 0) + 1;
            editor.putInt("event_id", id);
            editor.apply();
        }
        return id;
    }

    private void time_change() {
        sHour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (Integer.parseInt(sHour.getText().toString()) > 23) {
                        //Don't let the hour go over 23
                        sHour.setText("00");
                        sHour.setSelection(sHour.getText().length());
                    }
                    if (sHour.getText().toString().length() > 1) {
                        //Jumps to the next EditText
                        sMinute.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(sMinute, InputMethodManager.SHOW_IMPLICIT);
                    }
                } catch (Exception e) {
                }
            }
        });

        sHour.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                //When focus is lost check if the time in the correct format if not fix it
                if (!hasFocus) {
                    if (sHour.getText().toString().length() == 1) {
                        sHour.setText("0" + sHour.getText().toString());
                    }
                }
            }
        });

        sMinute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (Integer.parseInt(sMinute.getText().toString()) > 59) {
                        //Don't let the minuets go over 59
                        sMinute.setText("59");
                        sMinute.setSelection(sMinute.getText().length());
                    }
                    if (sMinute.getText().toString().length() > 1) {
                        //Jumps to the next EditText
                        eHour.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(eHour, InputMethodManager.SHOW_IMPLICIT);
                    }
                } catch (Exception e) {
                }

            }
        });

        sMinute.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                //When focus is lost check if the time in the correct format if not fix it
                if (!hasFocus) {
                    if (sMinute.getText().toString().length() == 1) {
                        sMinute.setText("0" + sMinute.getText().toString());
                    }
                }
            }
        });

        eHour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (Integer.parseInt(eHour.getText().toString()) > 23) {
                        //Don't let the hour go over 23
                        eHour.setText("00");
                        eHour.setSelection(eHour.getText().length());
                    }
                    if (eHour.getText().toString().length() > 1) {
                        //Jumps to the next EditText
                        eMinute.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(eMinute, InputMethodManager.SHOW_IMPLICIT);
                    }
                } catch (Exception e) {
                }
                if (sMinute.getText().toString().length() == 1) {
                    sMinute.setText("0" + sMinute.getText().toString());
                }
            }
        });

        eHour.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                //When focus is lost check if the time in the correct format if not fix it
                if (!hasFocus) {
                    if (eHour.getText().toString().length() == 1) {
                        eHour.setText("0" + eHour.getText().toString());
                    }
                }
            }
        });

        eMinute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (Integer.parseInt(eMinute.getText().toString()) > 59) {
                        //Don't let the minuets go over 59
                        eMinute.setText("59");
                        eMinute.setSelection(eMinute.getText().length());
                    }
                } catch (Exception e) {
                }
            }
        });

        eMinute.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //When the done action key is pressed check if the time in the correct format if not fix it
                    if (eMinute.isFocused()) {
                        if (eMinute.getText().toString().length() == 1) {
                            eMinute.setText("0" + eMinute.getText().toString());
                        }
                        eMinute.setSelection(eMinute.getText().length());
                    }
                }
                return false;
            }
        });
    }
}
