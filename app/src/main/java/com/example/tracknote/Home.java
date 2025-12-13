package com.example.tracknote;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.tracknote.Dao.NotesDao;
import com.example.tracknote.Entity.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class Home extends BaseActivity implements NotesAdapter.OnNoteClickListener {
    private TextView name;
    private SearchView search;
    private ImageButton account;
    private RecyclerView notesView;
    private NotesAdapter adapter;
    private FloatingActionButton add;
    private NotesDao noteDao;
    int userId;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ICON = "user_icon_uri";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        account = findViewById(R.id.imgAcc);
        name = findViewById(R.id.greet);

        search = findViewById(R.id.searchBar);

        // **<<--- SearchView Cleanup (Best Practice) --->>**
        search.setIconifiedByDefault(true);
        search.setFocusable(false);
        search.clearFocus();

        search.setOnClickListener(v -> {
            search.setIconified(false);
            search.setFocusable(true);
            search.setFocusableInTouchMode(true);
            search.requestFocus();
        });
        // **<<--- END SearchView Cleanup --->>**


        notesView = findViewById(R.id.rView);
        notesView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new NotesAdapter(new ArrayList<>(), this::onNoteClick);
        notesView.setAdapter(adapter);

        add = findViewById(R.id.btnAdd);
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);



        String uri = prefs.getString(KEY_USER_ICON, null);
        if (uri != null) {
            Glide.with(this)
                    .load(Uri.parse(uri))
                    .placeholder(R.drawable.profile1) // fallback image
                    .error(R.drawable.profile1)
                    .circleCrop()
                    .into(account);
        } else {
            account.setImageResource(R.drawable.profile1);
        }


        account.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Account.class);
            startActivity(intent);
        });



        SessionManager session = new SessionManager(Home.this);
        noteDao = AppDatabase.getINSTANCE(getApplicationContext()).notesDao();
        userId = session.getUserId();



        add.setOnClickListener(v -> {
            AddNote bottomSheet = new AddNote();
            bottomSheet.show(getSupportFragmentManager(), "AddNoteBottomSheet");
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchNotes(newText);
                return true;
            }
        });



        name.setText("Hello , " + session.getUsername());

        loadNotes(userId);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Notes noteToDelete = adapter.getNoteAt(position);

                // Show confirmation dialog
                new AlertDialog.Builder(Home.this)
                        .setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            new Thread(() -> {
                                noteDao.deleteNote(noteToDelete);
                                runOnUiThread(() -> {
                                    loadNotes(userId);
                                    Toast.makeText(Home.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                                });
                            }).start();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            adapter.notifyItemChanged(position); // Restore the swiped item
                            dialog.dismiss();
                        })
                        .setCancelable(false)
                        .show();
            }
        }).attachToRecyclerView(notesView);

        loadNotes(userId); // Initial load
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes(userId);

        // 3. ADDED: Reload the image when returning to Home, in case it was updated in Account
        String uri = prefs.getString(KEY_USER_ICON, null);
        if (uri != null) {
            Glide.with(this)
                    .load(Uri.parse(uri))
                    .placeholder(R.drawable.profile1)
                    .error(R.drawable.profile1)
                    .circleCrop()
                    .into(account);
        } else {
            account.setImageResource(R.drawable.profile1);
        }
    }
    public void reloadNotes() {
        SessionManager session = new SessionManager(this);
        loadNotes(session.getUserId()); // Your existing method that loads notes
    }



    private void loadNotes(int userId) {
        new Thread(() -> {
            List<Notes> notes = noteDao.getNotesForUser(userId);
            runOnUiThread(() -> adapter.setNotesList(notes));
        }).start();
    }

    private void searchNotes(String query) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getINSTANCE(getApplicationContext());
            NotesDao noteDao = db.notesDao();
            List<Notes> results = noteDao.searchNotes(query, userId);

            runOnUiThread(() -> {
                adapter.setNotesList(results);
            });
        }).start();
    }


    @Override
    public void onNoteClick(Notes note) {
        AddNote bottomSheet = new AddNote();
        Bundle args = new Bundle();
        args.putInt("noteId", note.local_note_id);
        bottomSheet.setArguments(args);
        bottomSheet.show(getSupportFragmentManager(), "EditNoteBottomSheet");
    }


}