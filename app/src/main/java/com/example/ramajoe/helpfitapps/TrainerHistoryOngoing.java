package com.example.ramajoe.helpfitapps;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ramajoe.helpfitapps.Model.Training;
import com.example.ramajoe.helpfitapps.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainerHistoryOngoing extends Fragment {
    private DatabaseReference training, users, mainUsers;
    private RecyclerView trainingRV;
    private FirebaseAuth auth;
    private FirebaseRecyclerAdapter<Training,TrainerHistoryOngoing.TrainingHolder> firebaseRecyclerAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.trainer_history_ongoing, container, false);

        users = FirebaseDatabase.getInstance().getReference().child("UserData").getRef();
        training = FirebaseDatabase.getInstance().getReference().child("Training");
        mainUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        training.keepSynced(true);
        auth = FirebaseAuth.getInstance();


        trainingRV = (RecyclerView) rootView.findViewById(R.id.trainerOngoingTrainingRecycler);
        trainingRV.setHasFixedSize(true);
        trainingRV.setLayoutManager(new LinearLayoutManager(getContext()));

        displayAllTraining();

        return rootView;
    }

    private void displayAllTraining() {
        Query item = users.child(auth.getCurrentUser().getUid().toString()).child("trainingList").orderByValue().equalTo("Open");
        FirebaseRecyclerOptions<Training> firebaseRecyclerOptions = new
                FirebaseRecyclerOptions.Builder<Training>().setIndexedQuery(item,training,Training.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Training, TrainerHistoryOngoing.TrainingHolder>(firebaseRecyclerOptions) {
            @NonNull
            @Override
            public TrainerHistoryOngoing.TrainingHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.trainer_history_item,viewGroup,false);
                return new TrainingHolder(view);
            }


            @Override
            protected void onBindViewHolder(@NonNull final TrainerHistoryOngoing.TrainingHolder holder, int position, @NonNull final Training model) {
                final String key = model.getSessionID().toString();
                holder.setTraining(model);
                holder.finishBtn.setVisibility(View.VISIBLE);

                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View detailTrainingLayout = inflater.inflate(R.layout.training_detail,null);

                        final TextView title, date, timeStart, timeEnd, type, status, fee, maxParticipant, classType, notes, trainerName;
                        LinearLayout groupMode, personalMode;

                        title = detailTrainingLayout.findViewById(R.id.itemTrainingTitleDetail);
                        date = detailTrainingLayout.findViewById(R.id.itemTrainingDateDetail);
                        timeStart = detailTrainingLayout.findViewById(R.id.itemTrainingTimeStartDetail);
                        timeEnd = detailTrainingLayout.findViewById(R.id.itemTrainingTimeEndDetail);
                        type = detailTrainingLayout.findViewById(R.id.itemTrainingTypeDetail);
                        status = detailTrainingLayout.findViewById(R.id.itemTrainingStatusStatus);
                        fee = detailTrainingLayout.findViewById(R.id.itemTrainingFeeDetail);
                        classType = detailTrainingLayout.findViewById(R.id.itemTrainingClassTypeDetail);
                        maxParticipant = detailTrainingLayout.findViewById(R.id.itemTrainingMaxParticipantDetail);
                        notes = detailTrainingLayout.findViewById(R.id.notesFromDetail);
                        trainerName = detailTrainingLayout.findViewById(R.id.itemTrainingTrainerNameDetail);
                        groupMode = detailTrainingLayout.findViewById(R.id.trainingDetailGroup);
                        personalMode = detailTrainingLayout.findViewById(R.id.notesTrainingDetailLinear);

                        //mencari nama pemilik training (trainer)
                        DatabaseReference DR =  training.child(model.getSessionID()).child("trainingOwner").getRef();
                        DR.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String ownerKey = dataSnapshot.getValue().toString();
                                mainUsers.child(ownerKey).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        trainerName.setText(user.getFullname());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        title.setText(model.getTitle());
                        date.setText(model.getDate());
                        String traTime = model.getTime();
                        List<String> TimeList = Arrays.asList(traTime.split(","));
                        timeStart.setText(TimeList.get(0));
                        timeEnd.setText(TimeList.get(1));
                        type.setText(model.getMode());
                        status.setText(model.getStatus());
                        fee.setText(model.getFee());


                        if(model.getMode().equals("Group")){
                            groupMode.setVisibility(View.VISIBLE);
                            String maxPart = model.getMaxParticipant();
                            List<String> maxPar2 = Arrays.asList(maxPart.split(","));
                            maxParticipant.setText(maxPar2.get(1));
                            classType.setText(model.getClassType());
                        }
                        else{
                            personalMode.setVisibility(View.VISIBLE);
                            notes.setText(model.getNotes());
                        }

                        dialog.setView(detailTrainingLayout);
                        dialog.show();
                    }
                });

                holder.finishBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog.setMessage("Are you sure to finish ?");
                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                training.child(key).child("status").setValue("Finished");
                                users.child(auth.getCurrentUser().getUid().toString()).child("trainingList").child(key).setValue("Finished");

                                final Map<String, Object> setFinished = new HashMap<String, Object>();
                                training.child(key).child("memberList").orderByValue()
                                        .equalTo("Joined")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()){
                                                    for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                                        setFinished.put(childSnapshot.getKey(), "Finished"); //
                                                    }
                                                    training.child(key).child("memberList").setValue(setFinished);
                                                    for (String keys : setFinished.keySet()) {
                                                        users.child(keys).child("trainingList").child(key).setValue("Finished");
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                            }
                        });
                        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                            }
                        });
                        alertDialog.show();
                    }
                });
            }
        };
        trainingRV.setAdapter(firebaseRecyclerAdapter);
    }

    private class TrainingHolder extends RecyclerView.ViewHolder{
        public TextView titleTV, dateTV, timeTV, feeTV, statusTV, modeTV, classTypeTV, notesTV, maxParticipantTV;
        public Button finishBtn;
        public CardView cardView;

        TrainingHolder(View itemView){
            super(itemView);
            titleTV = itemView.findViewById(R.id.historyTrainingTitle);
            modeTV = itemView.findViewById(R.id.historyTrainingType);
            finishBtn = itemView.findViewById(R.id.finishBtn);
            cardView = itemView.findViewById(R.id.trainingCardViewHistory);
        }

        void setTraining(Training tr){
            String title = tr.getTitle();
            titleTV.setText(title);
            String mode = tr.getMode();
            modeTV.setText(mode);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();

    };

    @Override
    public void onStop(){
        super.onStop();

        if(firebaseRecyclerAdapter != null){
            firebaseRecyclerAdapter.stopListening();
        }
    }

}
