package com.example.partyrental.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.applandeo.materialcalendarview.CalendarView;
import com.example.partyrental.Common.Common;
import com.example.partyrental.Interface.IDialogConfirmDialog;
import com.example.partyrental.R;

public class CustomConfirmDialog {

    public static CustomConfirmDialog mDialog;
    IDialogConfirmDialog iDialogConfirmDialog;

    TextView txt_booking_time_text, txt_booking_name_text, txt_booking_house_location, txt_owner_phone;
    CalendarView calendarView;

    Button btn_confirm;

    public static CustomConfirmDialog getInstance() {
        if (mDialog == null)
            mDialog = new CustomConfirmDialog();
        return mDialog;
    }

    public void showConfirmDialog(Context context,
                                  IDialogConfirmDialog iDialogConfirmDialog) {
        this.iDialogConfirmDialog = iDialogConfirmDialog;
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_layout);

        txt_booking_time_text = dialog.findViewById(R.id.txt_booking_time_text);
        txt_booking_name_text = dialog.findViewById(R.id.txt_booking_name_text);
        txt_booking_house_location = dialog.findViewById(R.id.txt_booking_house_location);
        txt_owner_phone = dialog.findViewById(R.id.txt_owner_phone);
        btn_confirm = dialog.findViewById(R.id.btn_confirm);
        txt_booking_time_text.setText(Common.simpleFormatDateWithDot.format(Common.startDate.getTime()) + " 10:00 "  + "-" + Common.simpleFormatDateWithDot.format(Common.endDate.getTime())+  " 10:00 ");

        txt_booking_name_text.setText(Common.currentUser.getUsername());
        txt_booking_house_location.setText(Common.selectedHouse.getAddress() + ", " + Common.selectedHouse.getCity());

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                iDialogConfirmDialog.onConfirmClickListener(dialog);
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
