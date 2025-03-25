package com.example.notesprovip;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewholder> {
    Context context;
    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, Context context) {
        super(options);
        this.context= context;
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteViewholder noteViewholder, int i, @NonNull Note note) {
        noteViewholder.titleTextview.setText(note.title);
        noteViewholder.contentTextview.setText(note.content);
        noteViewholder.timeTextview.setText(User.timeToString(note.timestamp));

        noteViewholder.itemView.setOnClickListener(v -> {
            Intent intent= new Intent(context, NoteDetailsActivity.class);
            intent.putExtra("title", note.title);
            intent.putExtra("content", note.content);
            String docId= this.getSnapshots().getSnapshot(i).getId();
            intent.putExtra("docId", docId);
            //test lai
            context.startActivity(intent);

        });
    }

    @NonNull
    @Override
    public NoteViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_note_item,parent,false);
        return new NoteViewholder(view);

    }

    class NoteViewholder extends RecyclerView.ViewHolder {
        TextView titleTextview, contentTextview, timeTextview;

        public NoteViewholder(@NonNull View itemView) {
            super(itemView);
            initbd();
        }

         void initbd() {
            titleTextview = itemView.findViewById(R.id.note_title_tv);
            contentTextview = itemView.findViewById(R.id.note_content_tv);
            timeTextview = itemView.findViewById(R.id.note_time_tv);
        }
    }


}
