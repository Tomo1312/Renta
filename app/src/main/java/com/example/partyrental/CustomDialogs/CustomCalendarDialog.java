package com.example.partyrental.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.partyrental.Common.Common;
import com.example.partyrental.Interface.IDialogClickListener;
import com.example.partyrental.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class CustomCalendarDialog {

    public static CustomCalendarDialog mDialog;
    public IDialogClickListener iDialogClickListener;
    boolean blocked = false;
    TextView txt_title;
    CalendarView calendarView;

    public static CustomCalendarDialog getInstance() {
        if (mDialog == null)
            mDialog = new CustomCalendarDialog();
        return mDialog;
    }

    public void showCalendarDialog(String title, Context context, List<Calendar> disabledDays, IDialogClickListener iDialogClickListener) {
        this.iDialogClickListener = iDialogClickListener;
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.calendar_layout);

        txt_title = dialog.findViewById(R.id.txt_calendar_title);
        txt_title.setText(title);

        calendarView = dialog.findViewById(R.id.calendarView);

        calendarView.setDisabledDays(disabledDays);
        calendarView.setHeaderColor(R.color.colorButton);

        Calendar now = Calendar.getInstance();
        if (title.equals(context.getResources().getString(R.string.end_date)))
            calendarView.setMinimumDate(Common.startDate);
        else
            calendarView.setMinimumDate(now);

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                for (Calendar calendarTmp : disabledDays) {
                    if (calendarTmp.equals(eventDay.getCalendar())) {
                        blocked = true;
                        break;
                    }
                    if (blocked)
                        break;
                    blocked = false;
                }
                if (title.equals(context.getResources().getString(R.string.end_date))) {
                    checkIfRangeIsInDisabledDays(eventDay.getCalendar(), disabledDays);
                }
                if (!blocked) {
                    iDialogClickListener.onOnDayClickListener(dialog, eventDay.getCalendar());
                } else {
                    Toast.makeText(context, "Datum nije dostupan", Toast.LENGTH_SHORT).show();
                    blocked = false;
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void checkIfRangeIsInDisabledDays(Calendar pickedDay, List<Calendar> disabledDays) {


        Calendar startCalendar = new GregorianCalendar();
        startCalendar.setTime(Common.startDate.getTime());

        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(pickedDay.getTime());

        while (startCalendar.before(endCalendar)) {
            for (Calendar calendarTmp : disabledDays) {
                Log.d("CALENDAR_KURAC", "startCalendar TIME:" + startCalendar.getTime().toString() + " calendatTMP TIME:" + calendarTmp.getTime().toString());
                if (calendarTmp.getTime().equals(startCalendar.getTime())) {
                    startCalendar = null;
                    endCalendar= null;
                    blocked = true;
                    break;
                }
            }
            if (blocked)
                break;
            blocked = false;
            startCalendar.add(Calendar.DATE, 1);
        }
    }
}
