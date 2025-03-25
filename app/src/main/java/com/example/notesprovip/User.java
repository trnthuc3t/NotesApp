package com.example.notesprovip;

import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;

public class User {
    static CollectionReference getCollectionrf(){
        FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance().collection("notes").document(currentUser.getUid())
                .collection("my_notes");

    }
    static String timeToString(Timestamp timestamp){
        return new SimpleDateFormat("dd/MM/YYYY").format(timestamp.toDate());
    }
}
