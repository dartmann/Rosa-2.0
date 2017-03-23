package de.davidartmann.android.rosa2.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.davidartmann.android.rosa2.database.PersonDao;
import de.davidartmann.android.rosa2.database.model.Person;

/**
 * Helper class for retrieving the path of selected images.
 * Created by david on 21.10.16.
 */
public class FileHelper {

    private static final String TAG = FileHelper.class.getSimpleName();

    public boolean isExtStorageDoc(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadDoc(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDoc(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public void writeJsonFile(Context context) {
        PersonDao personDao = new PersonDao(context);
        personDao.openReadable();
        List<Person> allPers = personDao.findAll();
        personDao.close();
        ObjectMapper mapper = new ObjectMapper();
        boolean mkdirs;
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/rosa");
        if (!file.exists()) {
            //tvErrorlog.append("Verzeichnis für Export noch nicht vorhanden\n");
            Log.d(TAG, "public external storage dir does not exist");
            mkdirs = file.mkdirs();
        } else {
            //tvErrorlog.append("Verzeichnis für Export bereits vorhanden\n");
            Log.d(TAG, "public external storage dir does exist");
            mkdirs = true;
        }
        if (mkdirs) {
            //tvErrorlog.append("Erstellen des Export Ordners erfolgreich\n");
            Log.d(TAG, "make dirs success");
            String fileName = "rosa.json";
            File jsonFile = new File(file, fileName);
            boolean createFile = false;
            if (!jsonFile.exists()) {
                //tvErrorlog.append("JSON Datei existiert nicht - versuche neue Datei zu erstellen\n");
                Log.d(TAG, "json file does not exist");
                try {
                    createFile = jsonFile.createNewFile();
                } catch (IOException e) {
                    //tvErrorlog.append("Erstellen der neuen JSON Datei fehlgeschlagen:\n" + e);
                    Log.d(TAG, "File creation failed", e);
                }
            } else {
                createFile = true;
                //tvErrorlog.append("JSON Datei existiert bereits\n");
                Log.d(TAG, "json file already exists");
            }
            if (createFile) {
                //tvErrorlog.append("Erstellen der JSON Datei erfolgreich");
                Log.d(TAG, "create new file sucess");
                Log.d(TAG, "json file exists: " + jsonFile.exists());
                Log.d(TAG, "trying to serialize all persons...");
                try {
                    mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, allPers);
                    Toast.makeText(context, "Exportdatei (rosa.json) ist erstellt worden", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    //tvErrorlog.append("Serialisieren der Personen fehlgeschlagen:\n" + e);
                    Log.d(TAG, "Error while writing JSON file", e);
                }
            } else {
                //tvErrorlog.append("Erstellen der neuen Datei fehlgeschlagen\n");
                Log.d(TAG, "create new file failed");
                Toast.makeText(context, "Konnte Exportdatei nicht erstellen", Toast.LENGTH_LONG).show();
            }
        } else {
            //tvErrorlog.append("Erstellen des Export Ordners fehlgeschlagen\n");
            Log.d(TAG, "make dirs failed");
            Toast.makeText(context, "Konnte Ordner '/rosa' nicht anlegen", Toast.LENGTH_LONG).show();
        }
    }
}
