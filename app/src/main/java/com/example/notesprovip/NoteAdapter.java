package com.example.notesprovip;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;  // Thêm import
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // Thêm import
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewholder> {
    Context context;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteViewholder holder, int position, @NonNull Note note) {
        holder.titleTextview.setText(note.title);
        holder.contentTextview.setText(note.content);
        holder.timeTextview.setText(User.timeToString(note.timestamp));

        // Hiển thị ảnh nếu có imageUrl
        if (note.getImageUrl() != null && !note.getImageUrl().isEmpty()) {
            holder.noteImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(note.getImageUrl())
                    .error(R.drawable.baseline_error_24)  // Ảnh lỗi nếu load thất bại
                    .into(holder.noteImage);
        } else {
            holder.noteImage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NoteDetailsActivity.class);
            intent.putExtra("title", note.title);
            intent.putExtra("content", note.content);
            intent.putExtra("imageUrl", note.getImageUrl());  // Truyền imageUrl
            String docId = this.getSnapshots().getSnapshot(position).getId();
            intent.putExtra("docId", docId);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public NoteViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_note_item, parent, false);
        return new NoteViewholder(view);
    }

    class NoteViewholder extends RecyclerView.ViewHolder {
        TextView titleTextview, contentTextview, timeTextview;
        ImageView noteImage;  // Thêm ImageView

        public NoteViewholder(@NonNull View itemView) {
            super(itemView);
            initbd();
        }

        void initbd() {
            titleTextview = itemView.findViewById(R.id.note_title_tv);
            contentTextview = itemView.findViewById(R.id.note_content_tv);
            timeTextview = itemView.findViewById(R.id.note_time_tv);
            noteImage = itemView.findViewById(R.id.note_image);  // Khởi tạo ImageView
        }
    }
}