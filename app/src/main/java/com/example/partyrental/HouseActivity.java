package com.example.partyrental;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.applandeo.materialcalendarview.CalendarView;
import com.example.partyrental.Adapter.MySliderAdapter;
import com.example.partyrental.Common.Common;
import com.example.partyrental.CustomDialogs.CustomCalendarDialog;
import com.example.partyrental.CustomDialogs.CustomConfirmDialog;
import com.example.partyrental.Interface.IDialogClickListener;
import com.example.partyrental.Interface.IDialogConfirmDialog;
import com.example.partyrental.Interface.IHouseImageLoadListener;
import com.example.partyrental.Model.BookingInformation;
import com.example.partyrental.Model.HouseImage;
import com.example.partyrental.Notification.FCMResponse;
import com.example.partyrental.Notification.FCMSendData;
import com.example.partyrental.Notification.MyNotification;
import com.example.partyrental.Notification.MyToken;
import com.example.partyrental.Notification.NotificationCommon;
import com.example.partyrental.Retrofit.IFCMService;
import com.example.partyrental.Retrofit.RetrofitClient;
import com.example.partyrental.Services.PicassoImageLoadingService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import customSlider.Slider;

public class HouseActivity extends AppCompatActivity implements IHouseImageLoadListener, IDialogClickListener, IDialogConfirmDialog {

    Slider house_image_slider;
    IDialogClickListener iDialogClickListener;
    IDialogConfirmDialog iDialogConfirmDialog;
    ImageView back;

    boolean start;
    TextView txt_name, txt_house_description, txt_house_street, txt_house_price, txt_owner_phone;
    IHouseImageLoadListener iHouseImageLoadListener;

    ImageView startDate, endDate;
    TextView txt_start_date, txt_end_date;
    Button reservation;

    AlertDialog loading;
    CalendarView calendarView;
    List<Calendar> reservedDays = new ArrayList<>();
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IFCMService ifcmApi;

    ImageView img_show_map;

    LinearLayout layout_start_end_pick, layout_start_date_pick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house);

        ifcmApi = RetrofitClient.getInstance().create(IFCMService.class);
        setUiView();

        loadImageSlider();
    }

    private void setUiView() {
        house_image_slider = findViewById(R.id.house_image_slide);
        back = findViewById(R.id.back);

        txt_name = findViewById(R.id.txt_name);
        txt_house_description = findViewById(R.id.txt_house_description);
        txt_house_street = findViewById(R.id.txt_house_street);
        txt_house_price = findViewById(R.id.txt_house_price);
        txt_owner_phone = findViewById(R.id.txt_owner_phone);
        reservation = findViewById(R.id.btn_reservation);
        img_show_map = findViewById(R.id.img_show_map);

        loading = new SpotsDialog.Builder().setCancelable(false).setContext(HouseActivity.this).build();
        loading.show();
        setTxt();
        setCalendarPick();
        Slider.init(new PicassoImageLoadingService());

        iHouseImageLoadListener = this;
        iDialogClickListener = this;
        iDialogConfirmDialog = this;


        getReservedDays();

        img_show_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = Common.selectedHouse.getAddress();
                String[] addressSplit = address.split(" ");
                String number = "";
                String finalAddress = "";
                for (String stringTmp : addressSplit) {
                    if (stringTmp.matches(".*\\d.*")) {
                        number = stringTmp;
                    } else {
                        finalAddress = finalAddress + stringTmp + "+";
                    }
                }
                if (number != null && !number.equals("")) {
                    finalAddress = finalAddress + number + "+" + Common.selectedHouse.getCity();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + finalAddress));
                    startActivity(intent);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.selectedHouse = null;
                startActivity(new Intent(HouseActivity.this, MainActivity.class));
                finish();
            }
        });
        reservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(txt_start_date.getText().toString().equals("")) && !(txt_end_date.getText().toString().equals(""))) {
                    CustomConfirmDialog.getInstance().showConfirmDialog(HouseActivity.this, iDialogConfirmDialog);
                } else {
                    Toast.makeText(HouseActivity.this, "Izaberite datum!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getReservedDays() {
        FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(Common.selectedHouse.getId())
                .collection("Reservation")
                .whereEqualTo("done", true)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot reservationSnapshot : task.getResult()) {
                        BookingInformation bookingInformation = reservationSnapshot.toObject(BookingInformation.class);
                        getAllDates(bookingInformation.getStartDateTimeStamp().toDate(), bookingInformation.getEndDateTimeStamp().toDate());

                    }
                }
            }
        });
    }

    private void setCalendarPick() {
        txt_start_date = findViewById(R.id.txt_start_date);
        txt_end_date = findViewById(R.id.txt_end_date);
        //startDate = findViewById(R.id.start_date_pick);
        //endDate = findViewById(R.id.start_end_pick);
        calendarView = findViewById(R.id.calendarView);

        layout_start_date_pick = findViewById(R.id.layout_start_date_pick);
        layout_start_end_pick = findViewById(R.id.layout_start_end_pick);

        layout_start_date_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = true;
                CustomCalendarDialog.getInstance().showCalendarDialog(getResources().getString(R.string.start_date), HouseActivity.this, reservedDays, iDialogClickListener);
            }
        });
        layout_start_end_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!txt_start_date.getText().toString().equals("")) {
                    start = false;
                    CustomCalendarDialog.getInstance().showCalendarDialog(getResources().getString(R.string.end_date), HouseActivity.this, reservedDays, iDialogClickListener);
                } else {
                    Toast.makeText(HouseActivity.this, "Prvo izabrite pocetni datum!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setTxt() {
        txt_name.setText(Common.selectedHouse.getCity());
        txt_house_description.setText(Common.selectedHouse.getDescription());
        txt_house_street.setText(Common.selectedHouse.getAddress() + ", " + Common.selectedHouse.getCity());
        txt_house_price.setText(Common.selectedHouse.getPrice() + getResources().getString(R.string.add_to_price));
        txt_owner_phone.setText(Common.selectedHouse.getOwnerPhone());
    }

    private void loadImageSlider() {
        FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(Common.selectedHouse.getId())
                .collection("Slike")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<HouseImage> houseImages = new ArrayList<>();
                    for (DocumentSnapshot houseImagesSnapshot : task.getResult()) {
                        HouseImage houseImage = houseImagesSnapshot.toObject(HouseImage.class);
                        houseImages.add(houseImage);
                    }
                    iHouseImageLoadListener.onHouseImageLoadSuccess(houseImages);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iHouseImageLoadListener.onHouseImageLoadError(e.getMessage());
            }
        });
    }

    @Override
    public void onHouseImageLoadSuccess(List<HouseImage> houseImages) {
        if (loading.isShowing())
            loading.dismiss();
        house_image_slider.setAdapter(new MySliderAdapter(houseImages));

    }

    @Override
    public void onHouseImageLoadError(String message) {
        if (loading.isShowing())
            loading.dismiss();
    }

    @Override
    public void onOnDayClickListener(DialogInterface dialogInterface, Calendar clickedDayCalendar) {

//        loading.show();
//        loading.dismiss();
        String dayOfMonth = String.valueOf(clickedDayCalendar.get(Calendar.DAY_OF_MONTH));
        String mont = String.valueOf(clickedDayCalendar.get(Calendar.MONTH) + 1);
        String year = String.valueOf(clickedDayCalendar.get(Calendar.YEAR));
        String date = dayOfMonth + "_" + mont + "_" + year;
        if (start) {
            txt_start_date.setText(date);
            Common.startDate = clickedDayCalendar;
        } else {
            txt_end_date.setText(date);
            Common.endDate = clickedDayCalendar;
        }
        dialogInterface.dismiss();
    }

    @Override
    public void onConfirmClickListener(DialogInterface dialogInterface) {
        if (!Common.startDate.equals(Common.endDate)) {
            sendAllDates(dialogInterface);
        }

        //loading.dismiss();
    }

    private void sendAllDates(DialogInterface dialogInterface) {
        //Making today for timestamp
        Timestamp startDateTimeStamp = new Timestamp(Common.startDate.getTime());
        Timestamp endDateTimeStamp = new Timestamp(Common.endDate.getTime());

        //New Booking info for every date
        BookingInformation bookingInformation = new BookingInformation();
        bookingInformation.setStartDateTimeStamp(startDateTimeStamp);
        bookingInformation.setEndDateTimeStamp(endDateTimeStamp);

        bookingInformation.setCustomerName(Common.currentUser.getName() + " " + Common.currentUser.getSurname());
        bookingInformation.setCustomerPhone(Common.currentUser.getMobile());
        bookingInformation.setCustomerEmail(Common.currentUser.getEmail());
        bookingInformation.setCustomerId(Common.currentUser.getUserId());

        bookingInformation.setHouseAddress(Common.selectedHouse.getAddress());
        bookingInformation.setHouseId(Common.selectedHouse.getId());
        bookingInformation.setHouseCity(Common.selectedHouse.getCity());
        bookingInformation.setHouseImage(Common.selectedHouse.getPicture());

        bookingInformation.setStartDate(Common.simpleFormatDateWithDot.format(Common.startDate.getTime()).toString());
        bookingInformation.setEndDate(Common.simpleFormatDateWithDot.format(Common.endDate.getTime()).toString());
        bookingInformation.setDone(false);
        bookingInformation.setOwnerPhone(Common.selectedHouse.getOwnerPhone());

        DocumentReference bookingRef = FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(Common.selectedHouse.getId())
                .collection("Reservation")
                .document();

        bookingInformation.setReservationId(bookingRef.getId());
        bookingRef.set(bookingInformation).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dialogInterface.dismiss();
                //addToUserBooking(bookingInformation);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialogInterface.dismiss();
                Toast.makeText(HouseActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Common.currentUser.getUserId())
                .collection("Reservation")
                .document(bookingInformation.getReservationId());

        String content = "Imate Novu rezervaciju od: " + Common.currentUser.getName() + "\nNa datum: " +
                Common.simpleFormatDateWithDot.format(Common.startDate.getTime()).toString() + " 10:00 - " +
                Common.simpleFormatDateWithDot.format(Common.endDate.getTime()).toString() + " 10:00";
        MyNotification myNotification = new MyNotification();
        myNotification.setUid(UUID.randomUUID().toString());
        myNotification.setTitle("Nova rezervacija");
        myNotification.setContent(content);
        myNotification.setRead(false); //We will only filter notification with 'read is false on barber staff
        myNotification.setServerTimestamp(FieldValue.serverTimestamp());
        userRef.set(bookingInformation)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseFirestore.getInstance().collection("Tokens")
                                .whereEqualTo("user", Common.selectedHouse.getOwnerPhone())
                                .limit(1)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful() && task.getResult().size() > 0) {
                                            MyToken myToken = new MyToken();
                                            for (DocumentSnapshot tokenSnapshot : task.getResult()) {
                                                myToken = tokenSnapshot.toObject(MyToken.class);

                                                FCMSendData sendRequest = new FCMSendData();
                                                Map<String, String> dataSend = new HashMap<>();
                                                dataSend.put(NotificationCommon.TITLE_KEY, "Nova rezervacija");
                                                dataSend.put(NotificationCommon.CONTENT_KEY, content);

                                                sendRequest.setTo(myToken.getToken());
                                                sendRequest.setData(dataSend);

                                                compositeDisposable.add
                                                        (ifcmApi.sendNotification(sendRequest)
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(new Consumer<FCMResponse>() {
                                                                    @Override
                                                                    public void accept(FCMResponse fcmResponse) throws Exception {

                                                                        Common.selectedHouse = null;
                                                                        startActivity(new Intent(HouseActivity.this, MainActivity.class));
                                                                        finish();

                                                                    }
                                                                }, new Consumer<Throwable>() {
                                                                    @Override
                                                                    public void accept(Throwable throwable) throws Exception {
                                                                        Log.d("NOTIFICATION_ERROR", throwable.getMessage());

                                                                        Common.selectedHouse = null;
                                                                        startActivity(new Intent(HouseActivity.this, MainActivity.class));
                                                                        finish();
                                                                    }
                                                                }));

                                            }
                                        }
                                    }
                                });
                    }
                });
    }


    private void getAllDates(Date startDate, Date endDate) {
        Calendar startCalendar = new GregorianCalendar();
        startCalendar.setTime(startDate);
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(endDate);

        while (startCalendar.before(endCalendar)) {
            Calendar calendar3 = new GregorianCalendar();
            calendar3.setTime(startCalendar.getTime());
            reservedDays.add(calendar3);
            startCalendar.add(Calendar.DATE, 1);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Common.selectedHouse = null;
        startActivity(new Intent(HouseActivity.this, MainActivity.class));
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}