package com.serhatsgr.petcareapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editUserName, editUserEmail;
    private Button saveChangesBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editUserName = findViewById(R.id.editUserName);
        editUserEmail = findViewById(R.id.editUserEmail);
        saveChangesBtn = findViewById(R.id.saveChangesBtn);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mFireStore = FirebaseFirestore.getInstance();

        // Kullanıcı bilgilerini yükle
        loadUserProfile();

        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

    private void loadUserProfile() {
        if (mUser != null) {
            mFireStore.collection("Users").document(mUser.getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot document = task.getResult();
                                editUserName.setText(document.getString("UserName"));
                                editUserEmail.setText(document.getString("email"));
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Profil yüklenemedi.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void saveUserProfile() {
        String newUserName = editUserName.getText().toString().trim();
        String newUserEmail = editUserEmail.getText().toString().trim();

        if (TextUtils.isEmpty(newUserName)) {
            Toast.makeText(this, "Kullanıcı adı boş olamaz.", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> updatedData = new HashMap<>();
        updatedData.put("UserName", newUserName);
        updatedData.put("email", newUserEmail);

        mFireStore.collection("Users").document(mUser.getUid()).update(updatedData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Profil güncellendi.", Toast.LENGTH_SHORT).show();
                            finish(); // Profil sayfasına geri dön
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Güncelleme başarısız.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}