package com.serhatsgr.petcareapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class addPetPage extends AppCompatActivity {



        private TextInputEditText textName, textType, textRace, textGender, textWeight, textBirthDate, textAdoptionDate;
        private Button btnSave;

        private FirebaseAuth mAuth;
        private FirebaseFirestore mFireStore;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_pet_page);

            textName = findViewById(R.id.name);
            textType = findViewById(R.id.type);
            textRace = findViewById(R.id.race);
            textGender = findViewById(R.id.gender);
            textWeight = findViewById(R.id.weight);
            textBirthDate = findViewById(R.id.birthDate);
            textAdoptionDate = findViewById(R.id.adoptionDate);
            btnSave = findViewById(R.id.buttonSavePet);

            mAuth = FirebaseAuth.getInstance();
            mFireStore = FirebaseFirestore.getInstance();

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addAnimal();
                }
            });
        }

        private void addAnimal() {
            String name = textName.getText().toString().trim();
            String type = textType.getText().toString().trim();
            String race = textRace.getText().toString().trim();
            String gender = textGender.getText().toString().trim();
            String weight = textWeight.getText().toString().trim();
            String birthDate = textBirthDate.getText().toString().trim();
            String adoptionDate = textAdoptionDate.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(type) || TextUtils.isEmpty(race) ||
                    TextUtils.isEmpty(gender) || TextUtils.isEmpty(weight) ||
                    TextUtils.isEmpty(birthDate) || TextUtils.isEmpty(adoptionDate)) {
                Toast.makeText(addPetPage.this, "Tüm alanları doldurmalısınız.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser mUser = mAuth.getCurrentUser();
            if (mUser != null) {
                String userId = mUser.getUid();
                String animalId = mFireStore.collection("animals").document().getId(); // Yeni hayvan için benzersiz ID

                HashMap<String, Object> mData = new HashMap<>();
                mData.put("name", name);
                mData.put("type", type);
                mData.put("race", race);
                mData.put("gender", gender);
                mData.put("weight", weight);
                mData.put("birthDate", birthDate);
                mData.put("adoptionDate", adoptionDate);
                mData.put("userId", userId);
                mData.put("animalId", animalId);

                mFireStore.collection("animals").document(animalId)
                        .set(mData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(addPetPage.this, "Evcil hayvan başarıyla eklendi.", Toast.LENGTH_SHORT).show();


                            // Evcil hayvan ismini önceki aktiviteye geri döndür
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("petName", name); // Evcil hayvanın ismini döndür
                            setResult(RESULT_OK, resultIntent);
                            finish(); // Aktiviteyi kapat
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(addPetPage.this, "Evcil hayvan eklenemedi.", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }