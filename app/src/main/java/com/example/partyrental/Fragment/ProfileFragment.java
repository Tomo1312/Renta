package com.example.partyrental.Fragment;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partyrental.Adapter.UserReservationAdapter;
import com.example.partyrental.Common.Common;
import com.example.partyrental.Common.SpacesItemDecoration;
import com.example.partyrental.CustomDialogs.CreateNewHouseDialog;
import com.example.partyrental.Interface.IAddHouseImagesListener;
import com.example.partyrental.Interface.IAddNewHouseClickListener;
import com.example.partyrental.Interface.IReservationDeleteListener;
import com.example.partyrental.Interface.IUserReservationLoadListener;
import com.example.partyrental.Model.BookingInformation;
import com.example.partyrental.Model.HouseCard;
import com.example.partyrental.Model.HouseImage;
import com.example.partyrental.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dmax.dialog.SpotsDialog;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements IUserReservationLoadListener, IAddNewHouseClickListener, IAddHouseImagesListener, AdapterView.OnItemSelectedListener {

    TextView txt_name, txt_phone, txt_reservations;

    Button btn_add_house;
    RecyclerView recycler_user_reservation;
    List<Uri> uriImages;
    IUserReservationLoadListener iUserReservationLoadListener;

    IReservationDeleteListener iReservationDeleteListener;

    IAddNewHouseClickListener iAddNewHouseClickListener;
    IAddHouseImagesListener iAddHouseImagesListener;

    AdapterView.OnItemSelectedListener onItemSelectedListener;

    AlertDialog dialog;

    StorageReference storageHouseReference;
    UploadTask uploadTask;

    boolean firstImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iUserReservationLoadListener = this;

        iAddNewHouseClickListener = this;

        iAddHouseImagesListener = this;

        onItemSelectedListener = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        txt_name = view.findViewById(R.id.txt_user_name);
        txt_phone = view.findViewById(R.id.txt_user_phone);
        recycler_user_reservation = view.findViewById(R.id.recycler_user_reservation);
        btn_add_house = view.findViewById(R.id.btn_add_house);
        txt_reservations = view.findViewById(R.id.txt_reservations);

        if (!Common.currentUser.getOwnHouseId().equals("")) {
            btn_add_house.setVisibility(View.GONE);
        }

        btn_add_house.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewHouseDialog.getInstance().showDialog(getContext(), iAddNewHouseClickListener, iAddHouseImagesListener, onItemSelectedListener);
            }
        });

        initView();
        loadUserReservations();
        storageHouseReference = FirebaseStorage.getInstance().getReference().child("Houses");


        return view;


    }

    private void initView() {
        recycler_user_reservation.setHasFixedSize(true);
        recycler_user_reservation.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recycler_user_reservation.addItemDecoration(new SpacesItemDecoration(8));
    }

    public void loadUserReservations() {
        txt_reservations.setText("Moje Rezervacije:");
        dialog.show();

        //get curr date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Common.currentUser.getUserId())
                .collection("Reservation")
                .whereGreaterThanOrEqualTo("startDateTimeStamp", toDayTimeStamp)
                .whereEqualTo("done", true)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<BookingInformation> reservations = new ArrayList<>();
                    for (DocumentSnapshot reservationSnapshot : task.getResult()) {
                        BookingInformation reservation = reservationSnapshot.toObject(BookingInformation.class);
                        reservations.add(reservation);
                    }
                    if (reservations.size() == 0) {
                        onUserReservationEmpty();
                    } else {
                        onUserReservationSuccess(reservations);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onUserReservationFailed(e.getMessage());
            }
        });
    }

    @Override
    public void onUserReservationEmpty() {
        txt_reservations.setText("Trenutno nema rezervacija");
        if (dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void onUserReservationSuccess(List<BookingInformation> reservations) {
        if (dialog.isShowing())
            dialog.dismiss();
        UserReservationAdapter reservationsAdapter = new UserReservationAdapter(getContext(), reservations);
        recycler_user_reservation.setAdapter(reservationsAdapter);
    }

    @Override
    public void onUserReservationFailed(String message) {
        if (dialog.isShowing())
            dialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onAddNewHouseConfirmConfirm(DialogInterface dialogInterface, HouseCard houseInfo) {
        if (uriImages != null) {
            dialog.show();
            firstImage = true;
            DocumentReference houseRef = FirebaseFirestore.getInstance()
                    .collection("Kuce")
                    .document();

            houseInfo.setOwnerName(Common.currentUser.getName() + " " + Common.currentUser.getSurname());
            houseInfo.setOwnerPhone(Common.currentUser.getMobile());
            houseInfo.setId(houseRef.getId());

            houseRef.set(houseInfo).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(Common.currentUser.getUserId())
                                .update("ownHouseId", houseInfo.getId());
                        Common.currentUser.setOwnHouseId(houseInfo.getId());
                        startUploadingImages(dialogInterface, houseInfo, houseRef);
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Postavite barem jednu sliku!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startUploadingImages(DialogInterface dialogInterface, HouseCard houseInfo, DocumentReference houseRef) {
        int i = 1;
        for (Uri uriTmp : uriImages) {

            final StorageReference fileRef = storageHouseReference.child(houseInfo.getId()).child("image" + String.valueOf(i) + ".jpg");
            uploadTask = fileRef.putFile(uriTmp);

            int finalI = i;
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        dialog.dismiss();
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = (Uri) task.getResult();
                        String myUri = downloadUri.toString();
                        HouseImage houseImage = new HouseImage(myUri);
                        if (firstImage) {
                            firstImage = false;
                            houseRef.update("picture", myUri);
                        }
                        DocumentReference imageRef = houseRef.collection("Slike")
                                .document();
                        houseImage.setName("image" + finalI + ".jpg");
                        imageRef.set(houseImage);

                        if (uriImages.size() == finalI) {
                            if (dialog.isShowing())
                                dialog.dismiss();
                            dialogInterface.dismiss();
                            btn_add_house.setVisibility(View.GONE);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onAddHouseImagesClick(DialogInterface dialogInterface) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            uriImages = new ArrayList<>();
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    uriImages.add(clipData.getItemAt(i).getUri());
                }
            } else {
                uriImages.add(data.getData());
            }
        }

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Common.county = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Common.county = parent.getItemAtPosition(0).toString();
    }
}