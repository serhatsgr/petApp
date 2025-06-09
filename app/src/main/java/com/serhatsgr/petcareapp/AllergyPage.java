package com.serhatsgr.petcareapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.serhatsgr.petcareapp.R;

public class AllergyPage extends AppCompatActivity {

    private EditText edtAllergy;
    private Button btnAddAllergy;
    private LinearLayout allergyListLayout;
    private FirebaseFirestore db;
    private String animalId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allergy_page);

        // XML bileşenlerini bağla
        edtAllergy = findViewById(R.id.edtAllergy);
        btnAddAllergy = findViewById(R.id.btnAddAllergy);
        allergyListLayout = findViewById(R.id.allergyListLayout);

        // Firestore başlat
        db = FirebaseFirestore.getInstance();

        // SharedPreferences ile animalId alma
        SharedPreferences sharedPreferences = getSharedPreferences("PetAppPrefs", Context.MODE_PRIVATE);
        animalId = sharedPreferences.getString("animalId", null);

        if (animalId == null) {
            Toast.makeText(this, "Hayvan ID bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firestore'dan alerjileri yükle
        loadAllergies();

        // Alerji ekleme butonu
        btnAddAllergy.setOnClickListener(v -> addAllergy());
    }

    private void loadAllergies() {
        CollectionReference allergyRef = db.collection("Pets").document(animalId).collection("Allergies");

        allergyRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allergyListLayout.removeAllViews(); // Önce eski listeyi temizle
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String allergyName = document.getString("name");
                    addAllergyView(allergyName, document.getId());
                }
            } else {
                Toast.makeText(this, "Alerjiler yüklenemedi!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAllergy() {
        String allergyName = edtAllergy.getText().toString().trim();

        if (TextUtils.isEmpty(allergyName)) {
            Toast.makeText(this, "Lütfen bir alerji adı girin!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Pets").document(animalId).collection("Allergies")
                .add(new AllergyModel(allergyName))
                .addOnSuccessListener(documentReference -> {
                    addAllergyView(allergyName, documentReference.getId());
                    edtAllergy.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Alerji eklenemedi!", Toast.LENGTH_SHORT).show());
    }

    private void addAllergyView(String allergyName, String docId) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(16, 16, 16, 16);

        TextView txtAllergy = new TextView(this);
        txtAllergy.setText(allergyName);
        txtAllergy.setTextSize(18);
        txtAllergy.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button btnDelete = new Button(this);
        btnDelete.setText("Sil");
        btnDelete.setOnClickListener(v -> deleteAllergy(itemLayout, docId));

        itemLayout.addView(txtAllergy);
        itemLayout.addView(btnDelete);
        allergyListLayout.addView(itemLayout);
    }

    private void deleteAllergy(View itemLayout, String docId) {
        db.collection("Pets").document(animalId).collection("Allergies").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    allergyListLayout.removeView(itemLayout);
                    Toast.makeText(this, "Alerji silindi!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Silme başarısız!", Toast.LENGTH_SHORT).show());
    }

    public static class AllergyModel {
        private String name;

        public AllergyModel() {}

        public AllergyModel(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
