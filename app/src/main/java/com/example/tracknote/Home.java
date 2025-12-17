package com.example.tracknote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.tracknote.Dao.NotesDao;
import com.example.tracknote.Entity.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class Home extends BaseActivity implements NotesAdapter.OnNoteClickListener {

    private NotesAdapter adapter;
    private NotesDao noteDao;
    private int userId;
    private RecyclerView rvSmartFolders;
    private SessionManager session;
    private SmartFolderAdapter folderAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        session = new SessionManager(this);
        userId = session.getUserIdOrRedirect(this);
        noteDao = AppDatabase.getINSTANCE(this).notesDao();

        TextView greet = findViewById(R.id.greet);
        greet.setText("Hello, " + session.getUsername());

        RecyclerView rv = findViewById(R.id.rView);
        FloatingActionButton add = findViewById(R.id.btnAdd);
        ImageButton acc = findViewById(R.id.imgAcc);
        SearchView search = findViewById(R.id.searchBar);
        AutoCompleteTextView filter = findViewById(R.id.etFilterCategory);
        rvSmartFolders = findViewById(R.id.rvSmartFolders);

        List<SmartFolderAdapter.SmartFolderType> folders = Arrays.asList(
                SmartFolderAdapter.SmartFolderType.ALL,
                SmartFolderAdapter.SmartFolderType.PINNED,
                SmartFolderAdapter.SmartFolderType.RECENT,
                SmartFolderAdapter.SmartFolderType.TODAY
        );

        folderAdapter = new SmartFolderAdapter(folders, this::loadSmartFolder);
        rvSmartFolders.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvSmartFolders.setAdapter(folderAdapter);

        // Updated adapter with both click and share
        adapter = new NotesAdapter(new ArrayList<>(), this);
        rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        rv.setAdapter(adapter);
        loadSmartFolder(SmartFolderAdapter.SmartFolderType.ALL);

        // Swipe to delete
        ItemTouchHelper.SimpleCallback swipe = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getAdapterPosition();
                Notes note = adapter.getNoteAt(pos);
                Executors.newSingleThreadExecutor().execute(() -> {
                    noteDao.softDelete(note.getLocal_note_id());
                    runOnUiThread(() -> Toast.makeText(Home.this, "Note Deleted!", Toast.LENGTH_SHORT).show());
                });
            }
        };
        new ItemTouchHelper(swipe).attachToRecyclerView(rv);

  
        search.setOnClickListener(v -> {
            search.setIconified(false);

        });

        EditText searchEdit =
                search.findViewById(androidx.appcompat.R.id.search_src_text);

        searchEdit.setOnClickListener(v -> {
            search.setIconified(false);

        });
        // Search
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

        // Filter by category
        filter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String category = s.toString().trim();
                if (category.isEmpty()) loadAllNotes();
                else filterByCategory(category);
            }
        });

        // Add note
        add.setOnClickListener(v -> {
            AddNote sheet = new AddNote();
            sheet.show(getSupportFragmentManager(), "AddNote");
        });

        // Account
        acc.setOnClickListener(v -> startActivity(new Intent(Home.this, Account.class)));
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile icon
        loadProfileIcon();
    }

    private void loadProfileIcon() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String uri = prefs.getString("user_icon_uri", null);
        ImageButton acc = findViewById(R.id.imgAcc);

        if (uri != null) {
            Glide.with(this)
                    .load(Uri.parse(uri))
                    .placeholder(R.drawable.profile1)
                    .error(R.drawable.profile1)
                    .circleCrop()
                    .into(acc);
        } else {
            acc.setImageResource(R.drawable.profile1);
        }
    }
    @Override
    public void onNoteClick(Notes note) {
        AddNote sheet = new AddNote();
        Bundle b = new Bundle();
        b.putInt("noteId", note.getLocal_note_id());
        sheet.setArguments(b);
        sheet.show(getSupportFragmentManager(), "EditNote");
    }

    @Override
    public void onShare(Notes note) {
        String shareBody = note.getTitle() + "\n\n" + Html.fromHtml(note.getDescNote()).toString();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, note.getTitle());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share Note via"));
    }

    private void filterByCategory(String category) {
        noteDao.getNotesByCategory(userId, category)
                .observe(this, notes -> adapter.setNotesList(notes));
    }

    private void loadAllNotes() {
        noteDao.getNotesForUser(userId)
                .observe(this, notes -> adapter.setNotesList(notes));
    }

    private void loadSmartFolder(SmartFolderAdapter.SmartFolderType type) {
        if (noteDao == null) return;
        switch (type) {
            case PINNED:
                noteDao.getPinnedNotesLive(userId).observe(this, adapter::setNotesList);
                break;
            case RECENT:
                noteDao.getRecentNotesLive(userId).observe(this, adapter::setNotesList);
                break;
            case TODAY:
                long start = DateUtils.getStartOfToday();
                long end = DateUtils.getEndOfToday();
                noteDao.getTodayNotesLive(userId, start, end).observe(this, adapter::setNotesList);
                break;
            default:
                noteDao.getNotesForUser(userId).observe(this, adapter::setNotesList);
        }
    }
}
