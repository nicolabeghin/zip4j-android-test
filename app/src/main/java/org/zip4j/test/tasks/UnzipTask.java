package org.zip4j.test.tasks;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;

import org.zip4j.test.MainActivity;
import org.zip4j.test.utils.ProgressDialogFragment;

import java.io.File;
import java.lang.ref.WeakReference;


public class UnzipTask extends AsyncTask<Void, Integer, Boolean> {
    protected final WeakReference<AppCompatActivity> mainActivityWeakRef;
    private final File file;

    public UnzipTask(AppCompatActivity mainActivity, File file) {
        super();
        this.mainActivityWeakRef = new WeakReference<>(mainActivity);
        this.file = file;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ProgressDialogFragment.showProgressDialog(mainActivityWeakRef.get().getSupportFragmentManager(), "Extracting " + file.getName() + "...", "Extracting");
    }

    @Override
    protected Boolean doInBackground(Void... files) {
        return decompress();
    }

    private boolean decompress() {
        final File targetFolder = file.getParentFile();
        final File existingMediaFolder = new File(targetFolder, "Media");
        cleanupExistingMediaFolder(existingMediaFolder);
        targetFolder.mkdirs();
        try {
            ZipFile zipFile = new ZipFile(file);
            zipFile.setRunInThread(true);
            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
            zipFile.extractAll(targetFolder.getAbsolutePath());
            while (progressMonitor.getState() == ProgressMonitor.State.BUSY) {
                publishProgress(progressMonitor.getPercentDone());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(MainActivity.APP_NAME, ex.getMessage());
            return false;
        }
        return true;
    }
    
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        ProgressDialogFragment.showProgressDialog(mainActivityWeakRef.get().getSupportFragmentManager(), String.format("Extracting... (%d%%)", values[0]), null);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        ProgressDialogFragment.dismissProgressDialog(mainActivityWeakRef.get().getSupportFragmentManager());
        if (result) {
            Log.i(MainActivity.APP_NAME, "Extracted " + file.getAbsolutePath());
            Toast.makeText(mainActivityWeakRef.get(), file.getName() + " extracted to " + file.getParentFile().getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } else {
            Log.e(MainActivity.APP_NAME, "Error extracting " + file.getAbsolutePath());
            Toast.makeText(mainActivityWeakRef.get(), "Unable to extract " + file.getName(), Toast.LENGTH_SHORT).show();
        }
    }


    private static void deleteRecursive(File fileOrDirectory) {
        try {
            if (fileOrDirectory.isDirectory())
                for (File child : fileOrDirectory.listFiles()) {
                    deleteRecursive(child);
                }
            fileOrDirectory.delete();
        } catch (Exception ex) {
            Log.e(MainActivity.APP_NAME, "Unable to delete " + ex.getMessage());
        }
    }

    private static void cleanupExistingMediaFolder(File existingMediaFolder) {
        if (existingMediaFolder != null && existingMediaFolder.exists()) {
            Log.w(MainActivity.APP_NAME, "Deleting pre-existing Media folder at " + existingMediaFolder.getAbsolutePath());
            deleteRecursive(existingMediaFolder);
        }
    }


}
