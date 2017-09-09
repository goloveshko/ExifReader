package io.github.goloveshko.exifreader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ExifReaderActivity extends AppCompatActivity {
    public static final String EXIF_TAG_NAMES_ENTRIES = "exif_tag_names_entries";
    private static final int FILE_SELECT_CODE = 42;
    private static final String TAG = "ExifReaderActivity";
    private static final String ADD_EXIF_TAG_NAMES = "addExifTagNames";
    private TextView EXIFTextView;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exif_reader);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.button_open);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        EXIFTextView = (TextView)findViewById(R.id.EXIFTextView);
        EXIFTextView.setKeyListener(null);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_exif_reader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_open) {
            showFileChooser();
            return true;
        }
        else if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, FILE_SELECT_CODE);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                new LoadExifTask().execute(uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private String getExif(Uri uri) throws ImageProcessingException, IOException {
        InputStream stream = getContentResolver().openInputStream(uri);
        Metadata metadata = ImageMetadataReader.readMetadata(stream);
        Set<Integer> selectedField = getSelectedField();
        String result = new String();
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                if(tag.getTagType() != ExifSubIFDDirectory.TAG_STRIP_BYTE_COUNTS) {//selectedField.contains(tag.getTagType())){ //
                    result += tag;
                    result += System.getProperty("line.separator");
                    Log.i(TAG, "" + tag);
                }
            }
        }

        return result;
        //Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
    }

    private class LoadExifTask extends AsyncTask<Uri, Void, String> {
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }
        protected String doInBackground(Uri... urls) {
            int count = urls.length;
            String result = new String();
            for (int i = 0; i < count; i++) {
                try {
                    result = getExif(urls[i]);
                } catch (ImageProcessingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isCancelled())
                    break;
            }
            return result;
        }

        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            EXIFTextView.setText(result);
            getSelectedField();
        }
    }

    public void menuShowSettingsClicked(MenuItem item) {
        Intent settingsActivity = new Intent(ExifReaderActivity.this, SettingsActivity.class);

        HashMap<Integer, String> result = null;
        try {
            result = reflectionSample("addExifTagNames");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        settingsActivity.putExtra(EXIF_TAG_NAMES_ENTRIES, result);
        startActivity(settingsActivity);
    }

    @Nullable
    public Set<String> getDefaultTags(String functionName) {
        HashMap<Integer, String> allMap = null;

        try {
            allMap = reflectionSample(ADD_EXIF_TAG_NAMES);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if(allMap != null) {
            Set<Integer> keySet = allMap.keySet();
            Set<String> result = new HashSet<>(keySet.size());
            keySet.forEach(i -> result.add(i.toString()));
            return result;
        }

        return null;
    }

    public HashMap<Integer, String> reflectionSample(String functionName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class c = ExifDirectoryBase.class;
        Class paraTypes[] = new Class[1];
        paraTypes[0] = HashMap.class;
        Method method = null;

        method = c.getDeclaredMethod(functionName, paraTypes);
        method.setAccessible(true);

        HashMap<Integer, String> argument = new HashMap<>();

        Object arg[] = new Object[1];
        arg[0] = new HashMap(argument);

        method.invoke(this, arg);

        argument = (HashMap<Integer, String>)arg[0];
        return argument;
    }

    @Nullable
    private Set<Integer> getSelectedField() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> selections = sharedPrefs.getStringSet(SettingsActivity.PREFERENCE_SHOW_EXIF_COLUMNS, getDefaultTags(ADD_EXIF_TAG_NAMES));

        if(selections == null) {
            return null;
        }

        //Toast.makeText(this, selections.toString(), Toast.LENGTH_LONG).show();

        HashSet<Integer> result = new HashSet<>(selections.size());
        selections.forEach(i -> result.add(Integer.valueOf(i)));
        return result;
    }
}
