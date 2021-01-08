package com.example.partyrental.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partyrental.Adapter.HouseAdapter;
import com.example.partyrental.Common.SpacesItemDecoration;
import com.example.partyrental.Interface.IHouseLoadListener;
import com.example.partyrental.Model.HouseCard;
import com.example.partyrental.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class FeedFragment extends Fragment implements IHouseLoadListener, NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recycler_house;

    AlertDialog dialog;

    CollectionReference housesRef;
    IHouseLoadListener iHouseLoadListener;


    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;


    public FeedFragment() {
        // Required empty public constructor
        housesRef = FirebaseFirestore.getInstance().collection("Kuce");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        // Inflate the layout for this fragment
        recycler_house = view.findViewById(R.id.recycler_home_rental);
        navigationView = view.findViewById(R.id.filter_county);
        drawerLayout = view.findViewById(R.id.drawer);
        toolbar = view.findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_closed);
        //img_filter = view.findViewById(R.id.img_filter);
        //navigationView.getMenu().getItem(1).getSubMenu().setGroupVisible(0, false);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_baseline_filter_alt_24));
        initView();

        init();

        loadHouses();

        return view;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_county, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    private void loadHouses() {
        dialog.show();
        FirebaseFirestore.getInstance().collection("Kuce").limitToLast(10).orderBy("id").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<HouseCard> houseCards = new ArrayList<>();
                    for (DocumentSnapshot houseSnapshot : task.getResult()) {
                        HouseCard houseCard = houseSnapshot.toObject(HouseCard.class);
                        houseCard.setId(houseSnapshot.getId());
                        if(!(houseCard.getPicture().equals("")) && houseCard.getPicture()!= null)
                            houseCards.add(houseCard);
                    }
                    iHouseLoadListener.onHouseLoadSuccess(houseCards);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onHouseLoadFailed(e.getMessage());
            }
        });
    }

    private void loadHouses(String county) {
        dialog.show();
        FirebaseFirestore.getInstance().collection("Kuce").whereEqualTo("county", county).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<HouseCard> houseCards = new ArrayList<>();
                    for (DocumentSnapshot houseSnapshot : task.getResult()) {
                        HouseCard houseCard = houseSnapshot.toObject(HouseCard.class);
                        houseCard.setId(houseSnapshot.getId());
                        if(!(houseCard.getPicture().equals("")) && houseCard.getPicture()!= null)
                            houseCards.add(houseCard);
                    }
                    iHouseLoadListener.onHouseLoadSuccess(houseCards);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onHouseLoadFailed(e.getMessage());
            }
        });
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        iHouseLoadListener = this;
    }

    private void initView() {
        recycler_house.setHasFixedSize(true);
        recycler_house.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recycler_house.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onHouseLoadSuccess(List<HouseCard> houseList) {
        recycler_house.removeAllViews();
        if (dialog.isShowing())
            dialog.dismiss();
        HouseAdapter houseAdapter = new HouseAdapter(getContext(), houseList);
        recycler_house.setAdapter(houseAdapter);
    }

    @Override
    public void onHouseLoadFailed(String message) {
        if (dialog.isShowing())
            dialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.countyall:
                loadHouses();
            case R.id.county1:
                loadHouses(item.toString());
            case R.id.county2:
                loadHouses(item.toString());
            case R.id.county3:
                loadHouses(item.toString());
            case R.id.county4:
                loadHouses(item.toString());
            case R.id.county5:
                loadHouses(item.toString());
            case R.id.county6:
                loadHouses(item.toString());
            case R.id.county7:
                loadHouses(item.toString());
            case R.id.county8:
                loadHouses(item.toString());
            case R.id.county9:
                loadHouses(item.toString());
            case R.id.county10:
                loadHouses(item.toString());
            case R.id.county11:
                loadHouses(item.toString());
            case R.id.county12:
                loadHouses(item.toString());
            case R.id.county13:
                loadHouses(item.toString());
            case R.id.county14:
                loadHouses(item.toString());
            case R.id.county15:
                loadHouses(item.toString());
            case R.id.county16:
                loadHouses(item.toString());
            case R.id.county17:
                loadHouses(item.toString());
            case R.id.county18:
                loadHouses(item.toString());
            case R.id.county19:
                loadHouses(item.toString());
            case R.id.county20:
                loadHouses(item.toString());
        }
        return true;
    }
}