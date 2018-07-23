package com.example.ramajoe.helpfitapps;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ramajoe.helpfitapps.Common.Common;
import com.example.ramajoe.helpfitapps.Model.Review;
import com.example.ramajoe.helpfitapps.Model.Training;
import com.example.ramajoe.helpfitapps.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MemberHistoryFinished extends Fragment{

    private DatabaseReference training, users, mainUsers;
    private RecyclerView trainingRV;
    private FirebaseAuth auth;

    private FirebaseRecyclerAdapter<Training,MemberHistoryFinished.TrainingHolder> firebaseRecyclerAdapter;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.trainer_history_ongoing, container, false);

        users = FirebaseDatabase.getInstance().getReference().child("UserData");
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
        Query item = users.child(auth.getCurrentUser().getUid().toString()).child("trainingList").orderByValue().equalTo("Finished");
        FirebaseRecyclerOptions<Training> firebaseRecyclerOptions = new
                FirebaseRecyclerOptions.Builder<Training>().setIndexedQuery(item,training,Training.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Training, MemberHistoryFinished.TrainingHolder>(firebaseRecyclerOptions) {
            @NonNull
            @Override
            public MemberHistoryFinished.TrainingHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.trainer_history_item,viewGroup,false);
                return new MemberHistoryFinished.TrainingHolder(view);
            }


            @Override
            protected void onBindViewHolder(@NonNull final MemberHistoryFinished.TrainingHolder holder, int position, @NonNull final Training model) {
                holder.setTraining(model);
                holder.finishBtn.setVisibility(View.GONE);
                holder.giveRatingBtn.setVisibility(View.VISIBLE);

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

                holder.giveRatingBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        giveRating(model.getTitle(),model.getSessionID());
                    }
                });

                //mengecek apakah usert sudah pernah memberi rating & comment apa belum
                DatabaseReference hasChild = training.child(model.getSessionID()).child("memberList").child(auth.getCurrentUser().getUid()).getRef();
                hasChild.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.hasChild("comment")) {
                            holder.giveRatingBtn.setText("Reviewed");
                            holder.giveRatingBtn.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }



        };
        trainingRV.setAdapter(firebaseRecyclerAdapter);
    }

    private void giveRating(String title, final String key) {
        Calendar calander = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd,MM,yyyy-hh,mm");
        final String strDate = sdf.format(calander.getTime());

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getContext());
        alertDialog.setTitle(title);
        alertDialog.setMessage("Give rating & review");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_give_review = inflater.inflate(R.layout.give_review,null);


        final TextInputEditText comment = (TextInputEditText) layout_give_review.findViewById(R.id.commentRev);
        final SimpleRatingBar rating = (SimpleRatingBar) layout_give_review.findViewById(R.id.starRating);

        alertDialog.setView(layout_give_review);

        //set Button
        alertDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final Review review = new Review();
                review.setName(Common.currentUser.getFullname());
                String ratings = String.valueOf(rating.getRating());
                review.setRating(ratings);
                String timesDate = String.valueOf(strDate);
                review.setTimeStamp(timesDate);
                review.setComment(comment.getText().toString());

                //Save review & comment to memberList
                training.child(key).child("memberList").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(review)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                DatabaseReference totalRev = training.child(key).child("totalReviewed").getRef();
                                totalRev.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            Long numOfPeople = (Long) dataSnapshot.getValue();
                                            float peoples = Float.valueOf(numOfPeople);
                                            float addPeoples = peoples+1;
                                            training.child(key).child("totalReviewed").setValue(addPeoples);
                                        }
                                        else{
                                            training.child(key).child("totalReviewed").setValue(1);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //+++++++++
                                DatabaseReference hasChild = training.child(key).child("averageRating").getRef();
                                hasChild.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            DatabaseReference totalRev = training.child(key).child("totalReviewed").getRef();
                                            totalRev.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    Long numOfPeople = (Long) dataSnapshot.getValue();
                                                    float peoples = Float.valueOf(numOfPeople);
                                                    double af = (double) snapshot.getValue();
                                                    float oldRating = (float)af;
                                                    float newRating = rating.getRating();
                                                    float average = (oldRating+newRating)/peoples;
                                                    training.child(key).child("averageRating").setValue(average); //bermasalah
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });


                            /*Long af = (Long) snapshot.getValue();
                            float oldRating = Float.valueOf(af);
                            float newRating = rating.getRating();
                            float average = (oldRating+newRating)/totalUser;
                            training.child(key).child("averageRating").setValue(average);*/
                                        }
                                        else {
                                            training.child(key).child("averageRating").setValue(rating.getRating());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                //+++++++++

                                Toast.makeText(getContext(), "Success submit ! ", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Failed submit !, "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                //training.child(key).child("averageRating").setValue(rating.getRating());
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        //show dialog
        alertDialog.show();
    }

    private class TrainingHolder extends RecyclerView.ViewHolder{
        public TextView titleTV, dateTV, timeTV, feeTV, statusTV, modeTV, classTypeTV, notesTV, maxParticipantTV;
        public Button finishBtn, giveRatingBtn;
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
            giveRatingBtn = itemView.findViewById(R.id.giveRatingBtn);

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
