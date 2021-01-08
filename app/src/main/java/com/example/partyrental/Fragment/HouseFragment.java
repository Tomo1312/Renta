package com.example.partyrental.Fragment;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partyrental.Adapter.HouseReservationAdapter;
import com.example.partyrental.Adapter.MySliderAdapter;
import com.example.partyrental.Adapter.OwnHouseAdapter;
import com.example.partyrental.Common.Common;
import com.example.partyrental.Common.SpacesItemDecoration;
import com.example.partyrental.Interface.IHouseImageLoadListener;
import com.example.partyrental.Interface.IHouseReservationLoadListener;
import com.example.partyrental.Interface.IReservationRequestLoadListener;
import com.example.partyrental.Model.BookingInformation;
import com.example.partyrental.Model.HouseCard;
import com.example.partyrental.Model.HouseImage;
import com.example.partyrental.R;
import com.example.partyrental.Services.PicassoImageLoadingService;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import customSlider.Slider;
import customSlider.event.OnSlideClickListener;
import dmax.dialog.SpotsDialog;

import static android.app.Activity.RESULT_OK;

public class HouseFragment extends Fragment implements AdapterView.OnItemSelectedListener, IHouseImageLoadListener, IHouseReservationLoadListener, IReservationRequestLoadListener {

    TextView txt_create_house, txt_item_count;
    ScrollView own_house;
    EditText txt_house_street, txt_house_city, txt_house_description, txt_price;

    Spinner county_spinner;
    RecyclerView recycler_house_reservations, recycler_house_reservations_request;
    Slider recycler_house_images;


    AlertDialog dialog;
    Button btn_add_images, btn_save_changes;

    public int selectedSlidePosition = 0;
    AdapterView.OnItemSelectedListener onItemSelectedListener;

    IHouseImageLoadListener iHouseImageLoadListener;
    IHouseReservationLoadListener iHouseReservationLoadListener;
    IReservationRequestLoadListener iReservationRequestLoadListener;

    UploadTask uploadTask;

    HouseCard ownerHouse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onItemSelectedListener = this;
        iHouseImageLoadListener = this;
        iHouseReservationLoadListener = this;
        iReservationRequestLoadListener = this;
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_house, container, false);

        txt_create_house = itemView.findViewById(R.id.txt_create_house);
        own_house = itemView.findViewById(R.id.own_house);
        txt_house_street = itemView.findViewById(R.id.txt_house_street);
        txt_house_city = itemView.findViewById(R.id.txt_house_city);
        county_spinner = itemView.findViewById(R.id.county_spinner);
        txt_house_description = itemView.findViewById(R.id.txt_house_description);
        txt_price = itemView.findViewById(R.id.txt_price);
        recycler_house_images = itemView.findViewById(R.id.recycler_house_images);
        txt_item_count = itemView.findViewById(R.id.txt_item_count);
        recycler_house_reservations = itemView.findViewById(R.id.recycler_house_reservations);
        recycler_house_reservations_request = itemView.findViewById(R.id.recycler_house_reservations_request);
        btn_add_images = itemView.findViewById(R.id.btn_add_images);
        btn_save_changes = itemView.findViewById(R.id.btn_save_changes);


        if (Common.currentUser.getOwnHouseId() != null) {
            if (!Common.currentUser.getOwnHouseId().equals("")) {

                FirebaseFirestore.getInstance()
                        .collection("Kuce")
                        .document(Common.currentUser.getOwnHouseId())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            ownerHouse = task.getResult().toObject(HouseCard.class);
                            setViewForHouse(ownerHouse);
                            Common.ownerHouse = ownerHouse;
                        }
                    }
                });
                initRecyclerView();

                loadHouseImages();

                loadHouseReservations();
            } else {
                own_house.setVisibility(View.GONE);
                txt_create_house.setVisibility(View.VISIBLE);
            }
        }

        return itemView;
    }

    private void loadHouseReservations() {
        dialog.show();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());
        FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(Common.currentUser.getOwnHouseId())
                .collection("Reservation")
                .whereGreaterThanOrEqualTo("startDateTimeStamp", toDayTimeStamp)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<BookingInformation> reservationsAccepted = new ArrayList<>();
                    ArrayList<BookingInformation> reservationRequests = new ArrayList<>();
                    for (QueryDocumentSnapshot bookingInformationSnapshot : task.getResult()) {
                        BookingInformation bookingInformation = bookingInformationSnapshot.toObject(BookingInformation.class);
                        if (bookingInformation.isDone())
                            reservationsAccepted.add(bookingInformation);
                        else
                            reservationRequests.add(bookingInformation);
                    }
                    onHouseReservationSuccess(reservationsAccepted);
                    onReservationRequestSuccess(reservationRequests);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onHouseReservationFailed(e.getMessage());
                onReservationRequestFailed(e.getMessage());
            }
        });

    }

    private void loadHouseImages() {
        FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(Common.currentUser.getOwnHouseId())
                .collection("Slike")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<HouseImage> houseImages = new ArrayList<>();
                    for (QueryDocumentSnapshot imageSnapshot : task.getResult()) {
                        HouseImage houseImage = imageSnapshot.toObject(HouseImage.class);
                        houseImage.setId(imageSnapshot.getId());
                        houseImages.add(houseImage);
                    }
                    onHouseImageLoadSuccess(houseImages);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onHouseImageLoadError(e.getMessage());
            }
        });


    }

    private void initRecyclerView() {
//        recycler_house_images.setHasFixedSize(true);
//        recycler_house_images.setLayoutManager(new GridLayoutManager(getContext(), 1));
//        recycler_house_images.addItemDecoration(new SpacesItemDecoration(8));

        recycler_house_reservations.setHasFixedSize(true);
        recycler_house_reservations.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recycler_house_reservations.addItemDecoration(new SpacesItemDecoration(8));

        recycler_house_reservations_request.setHasFixedSize(true);
        recycler_house_reservations_request.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recycler_house_reservations_request.addItemDecoration(new SpacesItemDecoration(8));
    }

    public void setViewForHouse(HouseCard ownerHouse) {
        own_house.setVisibility(View.VISIBLE);
        txt_create_house.setVisibility(View.GONE);

        txt_house_city.setText(ownerHouse.getCity());
        txt_house_street.setText(ownerHouse.getAddress());
        txt_house_description.setText(ownerHouse.getDescription());
        txt_price.setText(String.valueOf(ownerHouse.getPrice()));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.zupanije, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        county_spinner.setPrompt("Zupanije");
        county_spinner.setAdapter(adapter);
        county_spinner.setSelection(getOwnCounty(ownerHouse));
        county_spinner.setOnItemSelectedListener(onItemSelectedListener);

        btn_save_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateHouseInfo();
            }
        });

        btn_add_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });
    }

    private void updateHouseInfo() {
        dialog.show();
        DocumentReference houseRef;

        houseRef = FirebaseFirestore.getInstance().collection("Kuce").document(Common.currentUser.getOwnHouseId());
        houseRef.update("address", txt_house_street.getText().toString());
        houseRef.update("city", txt_house_city.getText().toString());
        houseRef.update("description", txt_house_description.getText().toString());
        houseRef.update("price", Integer.valueOf(txt_price.getText().toString()));
        houseRef.update("county", Common.county).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
            }
        });

    }

    private int getOwnCounty(HouseCard ownerHouse) {
        String[] county = getResources().getStringArray(R.array.zupanije);
        int i;
        for (i = 0; i < county.length; i++) {
            if (county[i].equals(ownerHouse.getCounty())) {
                break;
            }
        }
        return i;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Common.county = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Common.county = parent.getItemAtPosition(0).toString();
    }

    @Override
    public void onHouseImageLoadSuccess(List<HouseImage> images) {

        if (dialog.isShowing())
            dialog.dismiss();
        //OwnHouseAdapter imageAdapter = new OwnHouseAdapter(getContext(), images);
        MySliderAdapter imageAdapter = new MySliderAdapter(images);
        Slider.init(new PicassoImageLoadingService());
        recycler_house_images.setAdapter(imageAdapter);
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        //recycler_house_images.setLayoutManager(layoutManager);
        recycler_house_images.setOnSlideClickListener(new OnSlideClickListener() {
            @Override
            public void onSlideClick(int position) {
                Log.e("SLIDEBANNER", "KLIKNUTO");
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setMessage("")
                        .setPositiveButton("Obrisati sliku", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteImageFromHouse(images, images.get(position));
                            }
                        })
                        .setNegativeButton("Promjeni profilnu sliku kuće", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setProfileImage(images.get(position));
                            }
                        });
                AlertDialog alert = dialog.create();
                alert.show();
            }
        });
    }


    private void setProfileImage(HouseImage houseImage) {
        FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(Common.currentUser.getOwnHouseId())
                .update("picture", houseImage.getImage())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getContext(), "Profilna slika promjenjena!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteImageFromHouse(List<HouseImage> images, HouseImage houseImage) {
        FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(Common.currentUser.getOwnHouseId())
                .collection("Slike")
                .document(houseImage.getId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(Common.ownerHouse.getPicture().equals(houseImage.getImage()))
                            FirebaseFirestore.getInstance().collection("Kuce").document(Common.ownerHouse.getId()).update("picture", "");
                        deleteImageFromStorage(images, houseImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteImageFromStorage(List<HouseImage> images, HouseImage houseImage) {
        final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("Houses").child(Common.currentUser.getOwnHouseId()).child(houseImage.getName());
        fileRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                images.remove(houseImage);
                onHouseImageLoadSuccess(images);
            }
        });
    }

    @Override
    public void onHouseImageLoadError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK) {
            dialog.show();
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {

                    uploadImage(clipData.getItemAt(i).getUri(), clipData.getItemCount(), i);
                }
            } else {
                uploadImage(data.getData(), 1, 0);
            }
        }
    }

    private void uploadImage(Uri image, int size, int finalI) {
        DocumentReference houseRef = FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(Common.currentUser.getOwnHouseId());
        DocumentReference imageRef = houseRef.collection("Slike").document();
        final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("Houses").child(Common.currentUser.getOwnHouseId()).child(imageRef.getId() + ".jpg");
        uploadTask = fileRef.putFile(image);
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
                    if (ownerHouse.getPicture().equals("")) {
                        houseRef.update("picture", myUri);
                    }
                    houseImage.setName(imageRef.getId() + ".jpg");
                    imageRef.set(houseImage);

                    if (size - 1 == finalI) {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }
                    loadHouseImages();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (dialog.isShowing())
                    dialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onHouseReservationSuccess(List<BookingInformation> reservations) {
        if (dialog.isShowing())
            dialog.dismiss();
        HouseReservationAdapter reservationAdapter = new HouseReservationAdapter(getContext(), reservations);
        recycler_house_reservations.setAdapter(reservationAdapter);

    }

    @Override
    public void onHouseReservationEmpty() {
        if (dialog.isShowing())
            dialog.dismiss();

    }

    @Override
    public void onHouseReservationFailed(String message) {
        if (dialog.isShowing())
            dialog.dismiss();
        Log.e("FIREBASEERROR", message);

    }

    @Override
    public void onReservationRequestSuccess(List<BookingInformation> reservations) {
        if (dialog.isShowing())
            dialog.dismiss();
        HouseReservationAdapter reservationRequestAdapter = new HouseReservationAdapter(getContext(), reservations);
        recycler_house_reservations_request.setAdapter(reservationRequestAdapter);

    }

    @Override
    public void onReservationRequestEmpty() {
        if (dialog.isShowing())
            dialog.dismiss();

    }

    @Override
    public void onReservationRequestFailed(String message) {
        if (dialog.isShowing())
            dialog.dismiss();
        Log.e("FIREBASEERROR", message);
    }
}