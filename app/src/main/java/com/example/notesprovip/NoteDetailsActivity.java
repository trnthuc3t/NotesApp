package com.example.notesprovip;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

public class NoteDetailsActivity extends AppCompatActivity {
    EditText titleEdt, contentEdt;
    ImageButton savenoteBtn, deleteNoteBtn,backtomainBtn, shareBtn;
    TextView tieudetrangTv;
    Button uploadImageBtn, deleteImageBtn;
    ImageView noteImage;
    String title, content, Id, imageUrl;
    boolean isEditMode = false;
    private static final int REQUEST_IMAGE_PICK = 100;
    private Uri imageUri;
    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        initbd();

        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        imageUrl = getIntent().getStringExtra("imageUrl");
        Id = getIntent().getStringExtra("docId");

        if (Id != null && !Id.isEmpty()) {
            isEditMode = true;
        }

        titleEdt.setText(title != null ? title : "");
        contentEdt.setText(content != null ? content : "");

        if (isEditMode) {
            tieudetrangTv.setText("Sửa ghi chú");
            deleteNoteBtn.setVisibility(View.VISIBLE);
            shareBtn.setVisibility(View.VISIBLE);
        }

        loadImage(imageUrl);

        savenoteBtn.setOnClickListener(v -> saveNote());
        uploadImageBtn.setOnClickListener(v -> pickImage());
        deleteImageBtn.setOnClickListener(v -> deleteImage());
        deleteNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletenotefromFireBase();
            }
        });
        backtomainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(Intent.ACTION_SEND);
                i.setType("*/*");
                i.putExtra(Intent.EXTRA_SUBJECT,"Tiêu đề: "+title);
//                i.putExtra(Intent.EXTRA_TEXT,"Nội dung: "+ content);
//                i.putExtra(Intent.EXTRA_STREAM, imageUrl);
                i.putExtra(Intent.EXTRA_TEXT, "Nội dung: "+content+"\nHình ảnh: "+ imageUrl);
                startActivity(Intent.createChooser(i,"Chọn nền tảng share"));
            }
        });
    }

    private void deletenotefromFireBase() {
        DocumentReference documentReference;

        documentReference = User.getCollectionrf().document(Id);


        documentReference.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Không xóa thành công", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                uploadImageToDrive();
            } else {
                Toast.makeText(this, "Không thể lấy ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToDrive() {
        isUploading = true;
        uploadImageBtn.setEnabled(false);
        new Thread(() -> {
            try {
                InputStream inputStream = getResources().openRawResource(R.raw.service_account);
                GoogleCredential credentials = GoogleCredential.fromStream(inputStream)
                        .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

                HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
                JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

                Drive driveService = new Drive.Builder(transport, jsonFactory, credentials)
                        .setApplicationName("NotesProVIP")
                        .build();

                File fileMetadata = new File();
                fileMetadata.setName(System.currentTimeMillis() + ".jpg");
                fileMetadata.setParents(Collections.singletonList("1WjkOcB62UP30GwTAEkHPGXY5sKi0FHDd"));

                java.io.File filePath;
                try (InputStream imageStream = getContentResolver().openInputStream(imageUri)) {
                    filePath = new java.io.File(getCacheDir(), System.currentTimeMillis() + ".jpg");
                    try (OutputStream outputStream = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = imageStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }

                FileContent mediaContent = new FileContent("image/jpeg", filePath);
                File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id, webContentLink")
                        .execute();

                imageUrl = uploadedFile.getWebContentLink();
                filePath.delete();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Upload ảnh thành công: " + imageUrl, Toast.LENGTH_LONG).show();
                    Log.d("NoteDetails", "Image URL: " + imageUrl);
                    // Xóa cache Glide và load ảnh mới
                    Glide.with(this).clear(noteImage);  // Xóa cache trước khi load
                    loadImage(imageUrl);
                    isUploading = false;
                    uploadImageBtn.setEnabled(true);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Upload ảnh thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    isUploading = false;
                    uploadImageBtn.setEnabled(true);
                });
            }
        }).start();
    }

    private void loadImage(String url) {
        if (url != null && !url.isEmpty()) {
            noteImage.setVisibility(View.VISIBLE);
            deleteImageBtn.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(url)
//                    .error(R.drawable.baseline_error_24)
                    .into(noteImage);
        } else {
            noteImage.setVisibility(View.GONE);
            deleteImageBtn.setVisibility(View.GONE);
        }
    }

    private void deleteImage() {
        imageUrl = null;
        Glide.with(this).clear(noteImage);  // Xóa ảnh hiện tại khỏi ImageView
        loadImage(null);  // Cập nhật giao diện
//        saveNote();
        Toast.makeText(this, "Đã xóa ảnh khỏi ghi chú", Toast.LENGTH_SHORT).show();
    }

    private void saveNote() {
        String noteTitle = titleEdt.getText().toString();
        String noteContent = contentEdt.getText().toString();
        if (noteTitle.isEmpty()) {
            titleEdt.setError("Hãy nhập tiêu đề");
            return;
        }

        if (isUploading) {
            Toast.makeText(this, "Đang upload ảnh, vui lòng chờ...", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note();
        note.setTitle(noteTitle);
        note.setContent(noteContent);
        note.setTimestamp(Timestamp.now());
        note.setImageUrl(imageUrl);
        saveNoteToFirebase(note);
    }

    void saveNoteToFirebase(Note note) {
        DocumentReference documentReference;
        if (isEditMode) {
            documentReference = User.getCollectionrf().document(Id);
        } else {
            documentReference = User.getCollectionrf().document();
        }

        documentReference.set(note).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (isEditMode) {
                    Toast.makeText(this, "Đã sửa ghi chú", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Đã thêm ghi chú", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Không thêm được ghi chú", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initbd() {
        titleEdt = findViewById(R.id.titletext);
        contentEdt = findViewById(R.id.contenttext);
        savenoteBtn = findViewById(R.id.save);
        tieudetrangTv = findViewById(R.id.tieude);
        uploadImageBtn = findViewById(R.id.uploadImageBtn);
        noteImage = findViewById(R.id.noteImage);
        deleteImageBtn = findViewById(R.id.deleteImageBtn);
        deleteNoteBtn = findViewById(R.id.delete_note_btn);
        backtomainBtn= findViewById(R.id.back);
        shareBtn= findViewById(R.id.share);
    }
}