package es.atareao.instashare;

/**
 * Created by lorenzo on 12/06/17.
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;


public class JsonReader {
    private static final String TAG = "ClipboardManager";

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static String readJsonFromUrl(String url) throws IOException, JSONException {
        String ans = "";
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            ans = json.getString("thumbnail_url");
            String[] parts = ans.split("/");
            for(String part:parts){
                Log.i(TAG, part);
            }
            ans = "https://" + parts[2] + "/" + parts[5] + "/" + parts[7];
            Log.i(TAG, "ans= " + ans);
        } catch (Exception e){
            Log.i(TAG, "Error: " + e.toString());

        }
        finally {
            is.close();
        }
        return ans;
    }

    public static void main(String[] args) throws IOException, JSONException {
        /*JSONObject json = readJsonFromUrl("https://graph.facebook.com/19292868552");
        System.out.println(json.toString());
        System.out.println(json.get("id"));*/
    }
}