package com.example.app_fall;


import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;


import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ContactActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public Adapter mAdapter;
    public static final  int CONTACTLOADER = 0;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_contact);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {


                case R.id.home:
                    Intent intent2 = new Intent(ContactActivity.this,MainActivity.class);
                    startActivity(intent2);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.about:
                    Intent intent3 = new Intent(ContactActivity.this,AboutActivity.class);
                    startActivity(intent3);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.setting1:
                    Intent intent4 = new Intent(ContactActivity.this,SettingsActivity.class);

                    startActivity(intent4);

                    overridePendingTransition(0,0);
                    return true;
            }

            return super.onOptionsItemSelected(item);

        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        ListView listView = findViewById(R.id.list);
        mAdapter = new Adapter(this, null);
        listView.setAdapter(mAdapter);

        // whenever we press a listview for updating
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ContactActivity.this, EditorActivity.class);
                Uri newUri = ContentUris.withAppendedId(Contract.ContactEntry.CONTENT_URI, id);
                intent.setData(newUri);
                startActivity(intent);

            }
        });

        // get the loader running
        getLoaderManager().initLoader(CONTACTLOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {Contract.ContactEntry._ID,
                Contract.ContactEntry.COLUMN_NAME,
                Contract.ContactEntry.COLUMN_EMAIL,
                Contract.ContactEntry.COLUMN_PICTURE,
                Contract.ContactEntry.COLUMN_PHONENUMBER,
                Contract.ContactEntry.COLUMN_TYPEOFCONTACT
        };

        return new CursorLoader(this, Contract.ContactEntry.CONTENT_URI,
                projection, null,
                null,
                null);
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mAdapter.swapCursor(null);

    }
}

