package com.example.partyrental.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.partyrental.Common.Common;
import com.example.partyrental.Interface.IAddHouseImagesListener;
import com.example.partyrental.Interface.IAddNewHouseClickListener;
import com.example.partyrental.Model.HouseCard;
import com.example.partyrental.R;

public class CreateNewHouseDialog {

    public static CreateNewHouseDialog mDialog;

    TextView txt_house_street, txt_house_city, txt_house_description, txt_price;
    Button btn_add_images, btn_confirm;
    Spinner county_spinner;
    IAddHouseImagesListener iAddHouseImagesListener;
    IAddNewHouseClickListener iAddNewHouseClickListener;
    AdapterView.OnItemSelectedListener onItemSelectedListener;
    String county;

    public static CreateNewHouseDialog getInstance() {
        if (mDialog == null)
            mDialog = new CreateNewHouseDialog();
        return mDialog;
    }

    public void showDialog(Context context, IAddNewHouseClickListener iAddNewHouseClickListener, IAddHouseImagesListener iAddHouseImagesListener, AdapterView.OnItemSelectedListener onItemSelectedListener) {
        this.iAddNewHouseClickListener = iAddNewHouseClickListener;
        this.iAddHouseImagesListener = iAddHouseImagesListener;
        this.onItemSelectedListener = onItemSelectedListener;
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.new_house_layout);

        txt_house_street = dialog.findViewById(R.id.txt_house_street);
        txt_house_city = dialog.findViewById(R.id.txt_house_city);
        txt_house_description = dialog.findViewById(R.id.txt_house_description);
        txt_price = dialog.findViewById(R.id.txt_price);

        county_spinner = dialog.findViewById(R.id.county_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.zupanije, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        county_spinner.setPrompt("Zupanije");
        county_spinner.setAdapter(adapter);
        county_spinner.setOnItemSelectedListener(onItemSelectedListener);
        btn_add_images = dialog.findViewById(R.id.btn_add_images);
        btn_confirm = dialog.findViewById(R.id.btn_confirm);

        btn_add_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iAddHouseImagesListener.onAddHouseImagesClick(dialog);
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewNormal(context);
                if (textViewNotEmpty(context)) {
                    HouseCard house = new HouseCard(txt_house_city.getText().toString(), txt_house_street.getText().toString(), txt_house_description.getText().toString(), Integer.valueOf(txt_price.getText().toString()), Common.county);
                    iAddNewHouseClickListener.onAddNewHouseConfirmConfirm(dialog, house);
                }
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void setTextViewNormal(Context context) {
        txt_house_street.setBackground(ContextCompat.getDrawable(context, R.drawable.text_border_none));
        txt_house_city.setBackground(ContextCompat.getDrawable(context, R.drawable.text_border_none));
        txt_house_description.setBackground(ContextCompat.getDrawable(context, R.drawable.text_border_none));
        txt_price.setBackground(ContextCompat.getDrawable(context, R.drawable.text_border_none));
    }

    private boolean textViewNotEmpty(Context context) {

        if (TextUtils.isEmpty(txt_house_street.getText())) {
            txt_house_street.setBackground(ContextCompat.getDrawable(context, R.drawable.text_border_error));
            return false;
        } else if (TextUtils.isEmpty(txt_house_city.getText())) {
            txt_house_city.setBackground(ContextCompat.getDrawable(context, R.drawable.text_border_error));
            return false;
        } else if (TextUtils.isEmpty(txt_house_description.getText())) {
            txt_house_description.setBackground(ContextCompat.getDrawable(context, R.drawable.text_border_error));
            return false;
        } else if (TextUtils.isEmpty(txt_price.getText())) {
            txt_price.setBackground(ContextCompat.getDrawable(context, R.drawable.text_border_error));
            return false;
        }
        return true;
    }


}
