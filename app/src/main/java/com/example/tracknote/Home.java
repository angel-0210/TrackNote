package com.example.tracknote;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.tracknote.Dao.NotesDao;
import com.example.tracknote.Entity.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class Home extends BaseActivity implements NotesAdapter.OnNoteClickListener {

    private NotesAdapter adapter;
    private NotesDao noteDao;
    private int userId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        SessionManager session = new SessionManager(this);
        userId = session.getUserIdOrRedirect(this);

        TextView greet = findViewById(R.id.greet);
        greet.setText("Hello, " + session.getUsername());

        RecyclerView rv = findViewById(R.id.rView);
        FloatingActionButton add = findViewById(R.id.btnAdd);
        ImageButton acc = findViewById(R.id.imgAcc);
        SearchView search = findViewById(R.id.searchBar);
        AutoCompleteTextView filter=findViewById(R.id.etFilterCategory);

        adapter = new NotesAdapter(new ArrayList<>(), this);
        rv.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
        rv.setAdapter(adapter);

        noteDao = AppDatabase.getINSTANCE(this).notesDao();

        // Observe LiveData
        noteDao.getNotesForUser(userId)
                .observe(this, notes -> adapter.setNotesList(notes));

        // Swipe to delete
        ItemTouchHelper.SimpleCallback swipe =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(
                            RecyclerView rv,
                            RecyclerView.ViewHolder vh,
                            RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(
                            RecyclerView.ViewHolder vh, int direction) {

                        int pos = vh.getAdapterPosition();
                        Notes note = adapter.getNoteAt(pos);

                        Executors.newSingleThreadExecutor().execute(() -> {
                            noteDao.softDelete(note.getLocal_note_id());

                            runOnUiThread(() ->
                                    Toast.makeText(
                                            Home.this,
                                            "Note Deleted!",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                        });
                    }
                };

        new ItemTouchHelper(swipe).attachToRecyclerView(rv);

        //  Search
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                adapter.setSearchText(q);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String q) {
                adapter.setSearchText(q);
                return true;
            }
        });
        // Filter
        // Filter by custom category (AutoCompleteTextView)
        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String category = s.toString().trim();

                if (category.isEmpty()) {
                    loadAllNotes();          // clear filter
                } else {
                    filterByCategory(category); // apply filter
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        // Add note
        add.setOnClickListener(v -> {
            AddNote sheet = new AddNote();
            sheet.show(getSupportFragmentManager(), "AddNote");
        });

        // Account
        acc.setOnClickListener(v ->
                startActivity(new Intent(Home.this, Account.class))
        );
    }

    @Override
    public void onNoteClick(Notes note) {
        AddNote sheet = new AddNote();
        Bundle b = new Bundle();
        b.putInt("noteId", note.getLocal_note_id());
        sheet.setArguments(b);
        sheet.show(getSupportFragmentManager(), "EditNote");
    }
    private void filterByCategory(String category) {
        noteDao.getNotesByCategory(userId, category)
                .observe(this, notes -> adapter.setNotesList(notes));
    }
    private void loadAllNotes() {
        noteDao.getNotesForUser(userId)
                .observe(this, notes -> adapter.setNotesList(notes));
    }

}
