package com.example.tracknote;

import android.content.Context;
import android.util.Log;

import com.example.tracknote.Dao.NotesDao;
import com.example.tracknote.Entity.Notes;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.UUID;

public class SyncManager {

    private final NotesDao notesDao;
    private final FirebaseFirestore firestore;
    private final String firebaseUid;

    public SyncManager(Context context, String firebaseUid) {
        this.notesDao = AppDatabase.getInstance(context).notesDao();
        this.firestore = FirebaseFirestore.getInstance();
        this.firebaseUid = firebaseUid;
    }

    // Ensure cloudNoteId exists for a note
    private String ensureCloudId(Notes note) {
        if (note.getCloudNoteId() == null || note.getCloudNoteId().isEmpty()) {
            String id = UUID.randomUUID().toString();
            note.setCloudNoteId(id);

            // Update local DB on a separate thread
            new Thread(() -> notesDao.updateNote(note)).start();

            return id;
        }
        return note.getCloudNoteId();
    }

    // Push a note to Firestore
    public void pushNoteToCloud(Notes note) {
        String cloudId = ensureCloudId(note);
        note.setLastModified(System.currentTimeMillis());

        firestore.collection("users")
                .document(firebaseUid)
                .collection("notes")
                .document(cloudId)
                .set(note)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Note uploaded"))
                .addOnFailureListener(e -> Log.e("Firestore", "Upload failed", e));

    }

    // Pull all notes from Firestore to local DB
    public void pullNotesFromCloud() {
        firestore.collection("users")
                .document(firebaseUid)
                .collection("notes")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Notes> cloudNotes = snapshots.toObjects(Notes.class);

                    new Thread(() -> {
                        for (Notes cloudNote : cloudNotes) {

                            // Delete locally if removed in cloud
                            if (cloudNote.getIsDeleted() == 1) {
                                if (cloudNote.getCloudNoteId() != null) {
                                    notesDao.deleteByCloudId(cloudNote.getCloudNoteId());
                                }
                                continue;
                            }

                            Notes local = notesDao.getByCloudId(cloudNote.getCloudNoteId());

                            if (local == null) {
                                // New cloud note → insert locally
                                notesDao.insert(cloudNote);

                            } else if (cloudNote.getLastModified() > local.getLastModified()) {
                                // Cloud version newer → update local
                                cloudNote.setLocal_note_id(local.getLocal_note_id());
                                notesDao.updateNote(cloudNote);
                            }
                        }
                    }).start();
                });
    }

    // Delete a note from Firestore
    public void deleteNoteFromCloud(String cloudNoteId) {
        if (cloudNoteId == null || cloudNoteId.isEmpty()) return;

        firestore.collection("users")
                .document(firebaseUid)
                .collection("notes")
                .document(cloudNoteId)
                .delete();
    }

    // Listen for real-time Firestore changes
    public void listenForCloudChanges() {
        firestore.collection("users")
                .document(firebaseUid)
                .collection("notes")
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots == null) return;

                    new Thread(() -> {
                        snapshots.getDocumentChanges().forEach(change -> {
                            Notes note = change.getDocument().toObject(Notes.class);
                            Notes local = notesDao.getByCloudId(note.getCloudNoteId());

                            switch (change.getType()) {
                                case ADDED:
                                case MODIFIED:
                                    if (local == null) {
                                        notesDao.insert(note);
                                    } else if (note.getLastModified() > local.getLastModified()) {
                                        note.setLocal_note_id(local.getLocal_note_id());
                                        notesDao.updateNote(note);
                                    }
                                    break;
                                case REMOVED:
                                    notesDao.deleteByCloudId(note.getCloudNoteId());
                                    break;
                            }
                        });
                    }).start();
                });
    }
}
