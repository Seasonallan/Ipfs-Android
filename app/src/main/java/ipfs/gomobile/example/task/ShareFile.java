package ipfs.gomobile.example.task;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.season.myapplication.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import ipfs.gomobile.android.IPFS;
import ipfs.gomobile.example.MainActivity;

public final class ShareFile extends AsyncTask<Void, Void, String> {
    private static final String TAG = "ShareFile";

    private final WeakReference<MainActivity> activityRef;
    private final Uri fileUri;
    private ByteArrayOutputStream buffer;
    private boolean backgroundError;


    public ShareFile(MainActivity activity, Uri file) {
        activityRef = new WeakReference<>(activity);
        this.fileUri = file;
    }

    @Override
    protected void onPreExecute() {
        MainActivity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;

        activity.displayStatusProgress("Sharing file on IPFS");
    }

    private void readFile(Uri file) throws Exception {
        MainActivity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;

        try {
            InputStream is = activity.getContentResolver().openInputStream(file);
            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[4096];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

//            Log.d(TAG, "shared file data=" + MainActivity.bytesToHex(buffer.toByteArray()));
        } catch (FileNotFoundException e) {
            throw new Exception("File not found", e);
        } catch (IOException e) {
            throw new Exception("Failed to read file", e);
        }
    }

    @Override
    protected String doInBackground(Void... v) {
        MainActivity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) {
            cancel(true);
            return null;
        }

        IPFS ipfs = activity.getIpfs();

        try {
            readFile(fileUri);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write("--------------------------5f505897199c8c52\r\n".getBytes());
            outputStream.write("Content-Disposition: form-data; name=\"file\"\r\n".getBytes());
            outputStream.write("Content-Type: application/octet-stream\r\n\r\n".getBytes());
            byte[] bytes = buffer.toByteArray();
            outputStream.write(bytes);
            outputStream.write("\r\n--------------------------5f505897199c8c52--".getBytes());

            byte body[] = outputStream.toByteArray();

            ArrayList<JSONObject> jsonList = ipfs.newRequest("add")
                    .withHeader("Content-Type", "multipart/form-data; boundary=------------------------5f505897199c8c52")
                    .withBody(body)
                    .sendToJSONList();

            String cid = jsonList.get(0).getString("Hash");
            Log.d(TAG, "cid is " + cid);
            return cid;
        } catch (Exception err) {
            backgroundError = true;
            return MainActivity.exceptionToString(err);
        }
    }

    protected void onPostExecute(String result) {
        MainActivity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;

        if (backgroundError) {
            activity.displayStatusError("fail", result);
            Log.e(TAG, "IPFS add error: " + result);
        } else {
            activity.displayStatusSuccess(result);
        }
    }
}
