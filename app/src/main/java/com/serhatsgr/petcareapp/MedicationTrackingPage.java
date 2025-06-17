package com.serhatsgr.petcareapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MedicationTrackingPage extends AppCompatActivity {

    private TextInputEditText medicineName, usageReason, frequency, startDate, usageduration;
    private CheckBox continuation;
    private Button addMedication;
    private ListView medicationList;
    private List<String> medicationIds;
    private ArrayAdapter<String> medicationAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private HashMap<String, Object> mData;
    private FirebaseFirestore mFirestore;

    private String animalId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_tracking_page);

        // Initialize UI elements
        medicineName = findViewById(R.id.editTextMedicationName);
        usageReason = findViewById(R.id.editTextUsageReason);
        frequency = findViewById(R.id.editTextFrequency);
        startDate = findViewById(R.id.editTextStartDate);
        usageduration = findViewById(R.id.editTextDuration);
        continuation = findViewById(R.id.checkBoxContinuation);
        addMedication = findViewById(R.id.buttonAddMedication);
        medicationList = findViewById(R.id.listViewMedications);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        medicationIds = new ArrayList<>();

        // Get the animalId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("PetAppPrefs", MODE_PRIVATE);
        animalId = sharedPreferences.getString("animalId", null);

        if (animalId != null) {
            Toast.makeText(this, "Animal ID: " + animalId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Hayvan ID alınamadı.", Toast.LENGTH_SHORT).show();
        }

        loadMedicationHistory();

        // Handle adding a new medication
        addMedication.setOnClickListener(v -> addMedication());
    }

    private void addMedication() {
        String mName = medicineName.getText().toString().trim();
        String usR = usageReason.getText().toString().trim();
        String ffrequency = frequency.getText().toString().trim();
        String sDate = startDate.getText().toString().trim();
        String duration = usageduration.getText().toString().trim();

        if (TextUtils.isEmpty(mName)) {
            showToast("Lütfen ilaç adını giriniz.");
            return;
        }
        if (TextUtils.isEmpty(usR)) {
            showToast("Lütfen kullanım nedenini giriniz.");
            return;
        }
        if (TextUtils.isEmpty(ffrequency)) {
            showToast("Lütfen günlük kullanım adedini giriniz.");
            return;
        }
        if (TextUtils.isEmpty(sDate)) {
            showToast("Lütfen kullanıma başlama tarihini giriniz.");
            return;
        }
        if (TextUtils.isEmpty(duration)) {
            showToast("Lütfen kullanım süresini giriniz.");
            return;
        }

        mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            String userId = mUser.getUid();

            mData = new HashMap<>();
            mData.put("medicineName", mName);
            mData.put("usageReason", usR);
            mData.put("frequency", ffrequency);
            mData.put("startDate", sDate);
            mData.put("usageDuration", duration);
            mData.put("userId", userId);
            mData.put("animalId", animalId);  // Include animalId when adding medication
            mData.put("continuation", continuation.isChecked() ? "Devam ediyor" : "Devam etmiyor");

            mFirestore.collection("medication")
                    .add(mData)
                    .addOnSuccessListener(documentReference -> {
                        showToast("İlaç kaydı başarıyla eklendi.");
                        medicineName.getText().clear();
                        usageReason.getText().clear();
                        frequency.getText().clear();
                        startDate.getText().clear();
                        usageduration.getText().clear();
                        continuation.setChecked(false);
                        loadMedicationHistory();
                    })
                    .addOnFailureListener(e -> showToast("İlaç kaydı eklenemedi."));
        } else {
            showToast("Kullanıcı oturumu açılmamış.");
        }
    }

    private void loadMedicationHistory() {
        if (animalId != null) {
            mFirestore.collection("medication")
                    .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                    .whereEqualTo("animalId", animalId)  // Filter by both userId and animalId
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> medicationListData = new ArrayList<>();
                        medicationIds.clear();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String medicationId = documentSnapshot.getId();
                            medicationIds.add(medicationId);

                            String medicationInfo = documentSnapshot.getString("medicineName") + " - " +
                                    documentSnapshot.getString("usageReason") + " - " +
                                    documentSnapshot.getString("frequency") + " - " +
                                    documentSnapshot.getString("startDate") + " - " +
                                    documentSnapshot.getString("usageDuration") + " - " +
                                    documentSnapshot.getString("continuation");
                            medicationListData.add(medicationInfo);
                        }
                        updateMedicationAdapter(medicationListData);
                    })
                    .addOnFailureListener(e -> showToast("İlaç geçmişi yüklenirken bir hata oluştu."));
        }
    }

    private void updateMedicationAdapter(List<String> medicationListData) {
        medicationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, medicationListData);
        medicationList.setAdapter(medicationAdapter);

        if (medicationListData.isEmpty()) {
            showEmptyListView();
        } else {
            medicationList.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyListView() {
        TextView textViewEmpty = new TextView(this);
        textViewEmpty.setText("İLAÇ GEÇMİŞİ YOK");
        textViewEmpty.setGravity(Gravity.CENTER);
        ((ViewGroup) medicationList.getParent()).addView(textViewEmpty);
        medicationList.setEmptyView(textViewEmpty);
        medicationList.setVisibility(View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
