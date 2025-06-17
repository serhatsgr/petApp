package com.serhatsgr.petcareapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesActivity extends AppCompatActivity {
    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private List<Note> noteList = new ArrayList<>();
    private String animalId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // animalId'yi al
        animalId = getIntent().getStringExtra("animalId");

        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesAdapter = new NotesAdapter(noteList);
        notesRecyclerView.setAdapter(notesAdapter);

        Button addNoteButton = findViewById(R.id.add_note_button);
        addNoteButton.setOnClickListener(v -> showAddNoteDialog());

        loadNotes(); // Notları yükle
    }

    private void loadNotes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notes")
                .whereEqualTo("animalId", animalId) // animalId'ye göre filtrele
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        noteList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Note note = document.toObject(Note.class);
                            note.setId(document.getId()); // Firestore ID'sini ayarla
                            noteList.add(note);
                        }
                        notesAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Notlar yüklenemedi.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddNoteDialog() {
        final EditText noteInput = new EditText(this);
        noteInput.setHint("Notunuzu buraya yazın");

        new AlertDialog.Builder(this)
                .setTitle("Yeni Not Ekle")
                .setView(noteInput)
                .setPositiveButton("Ekle", (dialog, which) -> {
                    String newNoteText = noteInput.getText().toString();
                    if (!newNoteText.isEmpty()) {
                        addNote(newNoteText);
                    } else {
                        Toast.makeText(this, "Not boş olamaz.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void addNote(String newNoteText) {
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        Note newNote = new Note("userIdValue", animalId, newNoteText, timestamp);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notes").add(newNote)
                .addOnSuccessListener(documentReference -> {
                    newNote.setId(documentReference.getId()); // Firestore ID'sini ayarla
                    noteList.add(newNote);
                    notesAdapter.notifyItemInserted(noteList.size() - 1);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Not eklenemedi.", Toast.LENGTH_SHORT).show());
    }

    private void showEditNoteDialog(Note note) {
        final EditText noteInput = new EditText(this);
        noteInput.setText(note.getNote());
        noteInput.setHint("Notunuzu buraya yazın");

        new AlertDialog.Builder(this)
                .setTitle("Notu Düzenle")
                .setView(noteInput)
                .setPositiveButton("Kaydet", (dialog, which) -> {
                    String updatedNoteText = noteInput.getText().toString();
                    if (!updatedNoteText.isEmpty()) {
                        updateNote(note, updatedNoteText);
                    } else {
                        Toast.makeText(this, "Not boş olamaz.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void updateNote(Note note, String updatedText) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notes").document(note.getId()) // ID'yi kullan
                .update("note", updatedText)
                .addOnSuccessListener(aVoid -> {
                    note.setNote(updatedText);
                    notesAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Not güncellenemedi.", Toast.LENGTH_SHORT).show());
    }

    private void deleteNote(Note note) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notes").document(note.getId()) // ID'yi kullan
                .delete()
                .addOnSuccessListener(aVoid -> {
                    noteList.remove(note);
                    notesAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Not silindi.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Not silinemedi.", Toast.LENGTH_SHORT).show());
    }

    // Note Model
    public static class Note {
        private String userId;
        private String animalId;
        private String note;
        private String timestamp;
        private String id; // Firestore ID'si

        public Note() {}

        public Note(String userId, String animalId, String note, String timestamp) {
            this.userId = userId;
            this.animalId = animalId;
            this.note = note;
            this.timestamp = timestamp;
        }

        public String getUserId() { return userId; }
        public String getAnimalId() { return animalId; }
        public String getNote() { return note; }
        public String getTimestamp() { return timestamp; }
        public String getId() { return id; } // Getter
        public void setId(String id) { this.id = id; } // Setter
        public void setNote(String note) { this.note = note; } // Setter
    }

    // NotesAdapter
    public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
        private List<Note> notes;

        public NotesAdapter(List<Note> notes) {
            this.notes = notes;
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
            return new NoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            Note note = notes.get(position);
            holder.noteTextView.setText(note.getNote());
            holder.timestampTextView.setText(note.getTimestamp());

            holder.itemView.setOnLongClickListener(v -> {
                showNoteOptions(note);
                return true; // Uzun basma olayını işledi
            });
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        class NoteViewHolder extends RecyclerView.ViewHolder {
            private TextView noteTextView;
            private TextView timestampTextView;

            public NoteViewHolder(View itemView) {
                super(itemView);
                noteTextView = itemView.findViewById(R.id.note_text);
                timestampTextView = itemView.findViewById(R.id.note_timestamp);
            }
        }
    }

    private void showNoteOptions(Note note) {
        String[] options = {"Düzenle", "Sil"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seçenekler")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditNoteDialog(note);
                    } else if (which == 1) {
                        deleteNote(note);
                    }
                })
                .show();
    }
}