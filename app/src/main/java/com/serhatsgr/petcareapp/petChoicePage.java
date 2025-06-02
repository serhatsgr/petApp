package com.serhatsgr.petcareapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class petChoicePage extends AppCompatActivity {

    private TextView txtKullaniciAdi;
    private Button btnPet1, btnPet2, btnPet3, btnPet4;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_choice_page);

        txtKullaniciAdi = findViewById(R.id.kullaniciAdi);
        btnPet1 = findViewById(R.id.btnPet1);
        btnPet2 = findViewById(R.id.btnPet2);
        btnPet3 = findViewById(R.id.btnPet3);
        btnPet4 = findViewById(R.id.btnPet4);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        kullaniciAdiniYukle();
        evcilHayvanlariYukle();

        btnPet1.setOnClickListener(v -> handleButtonClick(btnPet1));
        btnPet2.setOnClickListener(v -> handleButtonClick(btnPet2));
        btnPet3.setOnClickListener(v -> handleButtonClick(btnPet3));
        btnPet4.setOnClickListener(v -> handleButtonClick(btnPet4));

        btnPet1.setOnLongClickListener(v -> {
            handlePetLongPress(btnPet1);
            return true;
        });
        btnPet2.setOnLongClickListener(v -> {
            handlePetLongPress(btnPet2);
            return true;
        });
        btnPet3.setOnLongClickListener(v -> {
            handlePetLongPress(btnPet3);
            return true;
        });
        btnPet4.setOnLongClickListener(v -> {
            handlePetLongPress(btnPet4);
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        evcilHayvanlariYukle();
    }

    private void kullaniciAdiniYukle() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mFirestore.collection("Users").document(currentUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String userName = document.getString("UserName");
                                txtKullaniciAdi.setText(userName);
                            } else {
                                Toast.makeText(this, "Kullanıcı adı bulunamadı.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Hata: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void evcilHayvanlariYukle() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mFirestore.collection("animals").whereEqualTo("userId", currentUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            int buttonIndex = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                String petName = document.getString("name");
                                String petId = document.getString("animalId");
                                updateButtonLabel(buttonIndex, petName, petId);
                                buttonIndex++;
                            }
                        } else {
                            Toast.makeText(this, "Evcil hayvanlar yüklenemedi.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void handleButtonClick(Button button) {
        String buttonText = button.getText().toString();
        if (buttonText.equals("Ekle")) {
            Intent intent = new Intent(petChoicePage.this, addPetPage.class);
            startActivityForResult(intent, 1);
        } else {
            String petId = (String) button.getTag();
            Intent intent1 = new Intent(petChoicePage.this, PetDetailActivity.class);
            intent1.putExtra("animalId", petId);
            startActivity(intent1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String petName = data.getStringExtra("petName");
            String petId = data.getStringExtra("petId");

            if (petName != null && petId != null) {
                updateButtonLabelForNewPet(petName, petId);
            }

            evcilHayvanlariYukle();
        }
    }

    private void updateButtonLabelForNewPet(String petName, String petId) {
        if (btnPet1.getText().toString().equals("Ekle")) {
            btnPet1.setText(petName);
            btnPet1.setTag(petId);
        } else if (btnPet2.getText().toString().equals("Ekle")) {
            btnPet2.setText(petName);
            btnPet2.setTag(petId);
        } else if (btnPet3.getText().toString().equals("Ekle")) {
            btnPet3.setText(petName);
            btnPet3.setTag(petId);
        } else if (btnPet4.getText().toString().equals("Ekle")) {
            btnPet4.setText(petName);
            btnPet4.setTag(petId);
        } else {
            Toast.makeText(this, "Tüm butonlar dolu. Yeni bir hayvan eklemek için mevcut bir hayvanı silin.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateButtonLabel(int index, String petName, String petId) {
        switch (index) {
            case 0:
                btnPet1.setText(petName);
                btnPet1.setTag(petId);
                break;
            case 1:
                btnPet2.setText(petName);
                btnPet2.setTag(petId);
                break;
            case 2:
                btnPet3.setText(petName);
                btnPet3.setTag(petId);
                break;
            case 3:
                btnPet4.setText(petName);
                btnPet4.setTag(petId);
                break;
        }
    }

    private void handlePetLongPress(Button button) {
        String petId = (String) button.getTag(); // Silmek için petId'yi al
        String petName = button.getText().toString(); // Pet adını al

        // Kullanıcıdan onay iste
        new AlertDialog.Builder(this)
                .setTitle("Evcil Hayvan Sil")
                .setMessage(petName + " adlı evcil hayvanı silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    silEvcilHayvan(petId, button);
                })
                .setNegativeButton("Hayır", null)
                .show();
    }

    private void silEvcilHayvan(String petId, Button button) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mFirestore.collection("animals").document(petId).delete()
                    .addOnSuccessListener(aVoid -> {
                        button.setText("Ekle"); // Butonu "Ekle" olarak güncelle
                        button.setTag(null); // Tag'i sıfırla
                        Toast.makeText(this, "Evcil hayvan silindi.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Silme işlemi başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}