package com.example.notesprovip;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class NoteDetailsActivity extends AppCompatActivity {
    EditText titleEdt, contentEdt;
    ImageButton savenoteBtn;
    TextView tieudetrangTv;
    String title, content, Id;
    boolean isEditMode=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note_details);
        initbd();
        title= getIntent().getStringExtra("title");
        content= getIntent().getStringExtra("content");
        Id= getIntent().getStringExtra("docId");

        if (Id != null&& !Id.isEmpty()) {
            isEditMode=true;
        }

        titleEdt.setText(title);
        contentEdt.setText(content);

        if (isEditMode) {
            tieudetrangTv.setText("Sửa ghi chú");
        }
        savenoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
    }

    private void saveNote() {
        String noteTitle = titleEdt.getText().toString();
        String noteContext = contentEdt.getText().toString();
        if (noteTitle == null || noteTitle.isEmpty()) {
            titleEdt.setError("Hãy nhập tiêu đề");
            return;
        }
        Note note = new Note();
        note.setTitle(noteTitle);
        note.setContent(noteContext);
        note.setTimestamp(Timestamp.now());
        saveNoteToFirebase(note);
    }

    void saveNoteToFirebase(Note note) {
        DocumentReference documentReference;
        if (isEditMode) {
            documentReference = User.getCollectionrf().document(Id);
        }else {
            documentReference = User.getCollectionrf().document();
        }

        documentReference.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NoteDetailsActivity.this, "Đã thêm ghi chú"
                            , Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(NoteDetailsActivity.this, "Không thêm được ghi chú"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void initbd() {
        titleEdt = findViewById(R.id.titletext);
        contentEdt = findViewById(R.id.contenttext);
        savenoteBtn = findViewById(R.id.save);
        tieudetrangTv= findViewById(R.id.tieude);
    }
}