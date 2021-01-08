package com.example.partyrental.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partyrental.Common.Common;
import com.example.partyrental.Interface.IOnImageHoldDeleteListener;
import com.example.partyrental.Model.HouseImage;
import com.example.partyrental.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OwnHouseAdapter extends RecyclerView.Adapter<OwnHouseAdapter.MyViewHoldAdapter> {
    Context context;

    List<HouseImage> houseImageList;

    public OwnHouseAdapter(Context context, List<HouseImage> houseImageList) {
        this.context = context;
        this.houseImageList = houseImageList;
    }

    @NonNull
    @Override
    public MyViewHoldAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.image_view_house, parent, false);
        return new MyViewHoldAdapter(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHoldAdapter holder, int position) {
        Picasso.get().load(houseImageList.get(position).getImage()).into(holder.houseImage);
        holder.setIOnImageHoldDeleteListener(new IOnImageHoldDeleteListener() {
            @Override
            public void onIOnImageHold(View view, int position) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage("")
                        .setPositiveButton("Obrisati sliku", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteImageFromHouse(position);
                            }
                        })
                        .setNegativeButton("Promjeni profilnu sliku kuće", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setProfileImage(position);
                            }
                        });
                AlertDialog alert = dialog.create();
                alert.show();
            }
        });
    }

    private void setProfileImage(int position) {
        FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(Common.currentUser.getOwnHouseId())
                .update("picture", houseImageList.get(position).getImage())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "Profilna slika promjenjena!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void deleteImageFromHouse(int position) {
        FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(Common.currentUser.getOwnHouseId())
                .collection("Slike")
                .document(houseImageList.get(position).getId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(Common.ownerHouse.getPicture().equals(houseImageList.get(position).getImage()))
                            FirebaseFirestore.getInstance().collection("Kuce").document(Common.ownerHouse.getId()).update("picture", "");
                        deleteImageFromStorage(position);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteImageFromStorage(int position) {
        final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("Houses").child(Common.currentUser.getOwnHouseId()).child(houseImageList.get(position).getName());
        fileRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//
//                cardViewList.clear();
                houseImageList.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return houseImageList.size();
    }

    public class MyViewHoldAdapter extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        ImageView houseImage;

        IOnImageHoldDeleteListener iOnImageHoldDeleteListener;

        public void setIOnImageHoldDeleteListener(IOnImageHoldDeleteListener iOnImageHoldDeleteListener) {
            this.iOnImageHoldDeleteListener = iOnImageHoldDeleteListener;
        }

        public MyViewHoldAdapter(@NonNull View itemView) {
            super(itemView);

            houseImage = itemView.findViewById(R.id.image_view_house);

            houseImage.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            iOnImageHoldDeleteListener.onIOnImageHold(v,getAdapterPosition());
            return false;
        }
    }
}
