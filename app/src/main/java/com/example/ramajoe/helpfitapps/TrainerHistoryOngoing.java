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
import android.widget.TextView;
import android.widget.Toast;

import com.example.ramajoe.helpfitapps.Model.Training;
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
    private DatabaseReference training;
    private DatabaseReference users;
    private RecyclerView trainingRV;
    private FirebaseAuth auth;

    private FirebaseRecyclerAdapter<Training,TrainerHistoryOngoing.TrainingHolder> firebaseRecyclerAdapter;

    //private RecyclerView.LayoutManager layoutManager;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.trainer_history_ongoing, container, false);

        users = FirebaseDatabase.getInstance().getReference().child("UserData").getRef();
        training = FirebaseDatabase.getInstance().getReference().child("Training");
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
                        Toast.makeText(getContext(), model.getTitle(), Toast.LENGTH_SHORT).show();
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
            //dateTV = itemView.findViewById(R.id.itemTrainingDate);
           // timeTV = itemView.findViewById(R.id.itemTrainingTime);
            //feeTV = itemView.findViewById(R.id.itemTrainingFee);
            //statusTV = itemView.findViewById(R.id.itemTrainingStatus);
            modeTV = itemView.findViewById(R.id.historyTrainingType);
            finishBtn = itemView.findViewById(R.id.finishBtn);
            cardView = itemView.findViewById(R.id.trainingCardViewHistory);

        }

        void setTraining(Training tr){
            String title = tr.getTitle();
            titleTV.setText(title);
            //String date = tr.getDate();
            //dateTV.setText(date);
            //String time = tr.getTime();
            //timeTV.setText(time);
            //String fee = tr.getFee();
            //feeTV.setText(fee);
            //String status = tr.getStatus();
            //statusTV.setText(status);
            String mode = tr.getMode();
            modeTV.setText(mode);

            //String traTime = tr.getTime();
            ///List<String> TimeList = Arrays.asList(traTime.split(","));

            //timeTV.setText(TimeList.get(0)+" - "+TimeList.get(1));

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
