package com.serhatsgr.petcareapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class UpdatePetPage extends AppCompatActivity {

    private TextInputEditText textName, textType, textRace, textGender, textWeight, textBirthDate, textAdoptionDate;
    private Button btnUpdate;
    private String animalId;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFireStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pet_page);

        textName = findViewById(R.id.name);
        textType = findViewById(R.id.type);
        textRace = findViewById(R.id.race);
        textGender = findViewById(R.id.gender);
        textWeight = findViewById(R.id.weight);
        textBirthDate = findViewById(R.id.birthDate);
        textAdoptionDate = findViewById(R.id.adoptionDate);
        btnUpdate = findViewById(R.id.buttonUpdatePet);

        mAuth = FirebaseAuth.getInstance();
        mFireStore = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("PetAppPrefs", MODE_PRIVATE);
        animalId = sharedPreferences.getString("animalId", null);

        // Hayvan bilgilerini yükle
        loadAnimalData(animalId);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAnimal();
            }
        });
    }

    private void loadAnimalData(String animalId) {
        mFireStore.collection("animals").document(animalId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        textName.setText(document.getString("name"));
                        textType.setText(document.getString("type"));
                        textRace.setText(document.getString("race"));
                        textGender.setText(document.getString("gender"));
                        textWeight.setText(document.getString("weight"));
                        textBirthDate.setText(document.getString("birthDate"));
                        textAdoptionDate.setText(document.getString("adoptionDate"));
                    } else {
                        Toast.makeText(UpdatePetPage.this, "Hayvan bulunamadı.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UpdatePetPage.this, "Veri yüklenemedi.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateAnimal() {
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
            Toast.makeText(UpdatePetPage.this, "Tüm alanları doldurmalısınız.", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> mData = new HashMap<>();
        mData.put("name", name);
        mData.put("type", type);
        mData.put("race", race);
        mData.put("gender", gender);
        mData.put("weight", weight);
        mData.put("birthDate", birthDate);
        mData.put("adoptionDate", adoptionDate);

        mFireStore.collection("animals").document(animalId)
                .update(mData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UpdatePetPage.this, "Hayvan bilgileri başarıyla güncellendi.", Toast.LENGTH_SHORT).show();
                    finish(); // Aktiviteyi kapat
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UpdatePetPage.this, "Hayvan bilgileri güncellenemedi.", Toast.LENGTH_SHORT).show();
                });
    }
}