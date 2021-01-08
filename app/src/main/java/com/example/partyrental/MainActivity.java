package com.example.partyrental;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.partyrental.Common.Common;
import com.example.partyrental.Common.Constants;
import com.example.partyrental.Fragment.FeedFragment;
import com.example.partyrental.Fragment.HouseFragment;
import com.example.partyrental.Fragment.ProfileFragment;
import com.example.partyrental.Model.User;
import com.example.partyrental.Notification.NotificationCommon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    CollectionReference userRef;
    FirebaseAuth firebaseAuth;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);
        if (Common.currentUser == null) {
            userRef = FirebaseFirestore.getInstance().collection("Users");
            firebaseAuth = FirebaseAuth.getInstance();
            Bundle extras = getIntent().getExtras();
            userId = extras.getString(Constants.KEY_USER_ID);
            if (TextUtils.isEmpty(userId)) {
                Paper.init(this);
                userId = Paper.book().read(Constants.LOGGED_KEY);
            }

            //FirebaseUser user = firebaseAuth.getCurrentUser();
            if (userId != null) {
                DocumentReference documentReference = userRef.document(userId);
                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userSnapshot = task.getResult();
                            if (userSnapshot.exists()) {
                                Common.currentUser = userSnapshot.toObject(User.class);
                                Common.currentUser.setUserId(userId);

                                FirebaseInstanceId.getInstance().getInstanceId()
                                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                if (task.isSuccessful()) {
                                                    NotificationCommon.updateToken(task.getResult().getToken());
                                                    Log.d("EDMTToken", task.getResult().getToken());
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                                init();
                                if (Common.currentUser.isBanned()) {
                                    android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(MainActivity.this);
                                    dialog.setMessage("Iz nekog razloga ste banani, za vise informacija obratite se administraciji na reservationapk@gmail.com!")
                                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    finish();
                                                    System.exit(0);
                                                }
                                            })
                                            .setCancelable(false);
                                    AlertDialog alert = dialog.create();
                                    alert.show();
                                }
                            }
                        }
                    }
                });
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                }
            }
        } else {
            init();
        }
    }

    private void init() {
        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment = null;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_feed) {
                    fragment = new FeedFragment();
                } else if (item.getItemId() == R.id.action_profile) {
                    fragment = new ProfileFragment();
                } else if (item.getItemId() == R.id.action_house) {
                    fragment = new HouseFragment();
                }
                return loadFragment(fragment);
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_feed);
    }


    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
        return false;
    }

}