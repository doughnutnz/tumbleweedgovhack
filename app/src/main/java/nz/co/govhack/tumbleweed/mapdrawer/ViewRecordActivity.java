package nz.co.govhack.tumbleweed.mapdrawer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Callback;
import okhttp3.Response;

public class ViewRecordActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener {

    private JSONArray parksJson;
    private JSONObject mRecord;

    private RatingBar getRatingBar;
    private RatingBar setRatingBar;
    private TextView countText;
    private int count;
    private float curRate;
    private float globalRate;

    String installationId = "";
    String recordId = "";
    String playgroundName = "";
    String lat = "";
    String lon = "";
    String mark = "";

    boolean initialisation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_record);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        String json = Utils.loadJSONFromAsset(getAssets());

        try {
            parksJson = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Bundle b = getIntent().getExtras();
        if (b != null) {
            recordId = b.getString("record_id");
            mRecord = findRecordById(recordId);

            try {
                toolbar.setTitle(mRecord.getString("name"));
                setSupportActionBar(toolbar);

                String details = "<h2>Address</h2>" +
                        "<p>" + mRecord.getString("address") + "</p>" +
                        "<h2>Equipement</h2>" +
                        "<p>" + mRecord.getString("equipment") + "</p>";

                String facilities = mRecord.getString("facilities");
                String about = mRecord.getString("about");

                if(facilities.length()>0) details += "<h2>Facilities</h2>" +
                                                    "<p>" + facilities + "</p>";
                if(about.length()>0) details += "<h2>About</h2>" +
                                                "<p>" + about + "</p>";

                ((TextView) findViewById(R.id.record_details)).setText(Html.fromHtml(details));

                installationId = Installation.id(getApplicationContext());
                playgroundName = mRecord.getString("name");
                lat = mRecord.getString("lat");
                lon = mRecord.getString("long");


            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("****", "Json error here", e);
            }

        }

        findViewsById();

        getRatingBar.setOnRatingBarChangeListener(this);

        /*
        RecordClick record = new RecordClick();
        record.execute();

        UpdateRating update = new UpdateRating();
        update.execute();*/

        recordClick();
        updateGlobalRating();
        updateInstallationRating();

    }

    private void findViewsById() {
        getRatingBar = (RatingBar) findViewById(R.id.getRating);
        setRatingBar = (RatingBar) findViewById(R.id.setRating);
        countText = (TextView) findViewById(R.id.countText);
    }

    private JSONObject findRecordById(String recordId) {
        try {
            for(int i = 0; i < parksJson.length(); i++) {
                    JSONObject record = (JSONObject) parksJson.get(i);
                    String id = "" + record.getInt("id");

                    if (id.equals(recordId)) {
                        return record;
                    }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateGlobalRating() {
        OkHttpClient client = new OkHttpClient();
        String url = getResources().getString(R.string.rating_url);
        Request request = new Request.Builder().url(url + "?playground_name=" + playgroundName).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("****", "Failed to update rating", e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    JSONObject Jobject = new JSONObject(jsonData);
                    count = (int) Jobject.getDouble("count");
                    if(count>0) {
                        globalRate = (float) Jobject.getDouble("rating");
                    }
                } catch (JSONException e) {
                    Log.i("****", "Rating update has failed", e);
                } catch (IOException e) {
                    Log.i("****", "Rating update has failed", e);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setRatingBar.setRating((int) globalRate);
                        countText.setText(count + " user ratings");
                    }
                });
                Log.i("****", "Rating has been updated");
                Log.i("****", "The Http response is: " + response.toString());
            }
        });
    }

    private void updateInstallationRating() {
        OkHttpClient client = new OkHttpClient();
        String url = getResources().getString(R.string.rating_url);
        Request request = new Request.Builder().url(url + "?playground_name=" + playgroundName + "&installation_id=" + installationId).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("****", "Failed to update rating", e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    JSONObject Jobject = new JSONObject(jsonData);
                    count = (int) Jobject.getDouble("count");
                    if(count>0) {
                        curRate = (float) Jobject.getDouble("rating");
                    }
                } catch (JSONException e) {
                    Log.i("****", "Rating installation update has failed", e);
                } catch (IOException e) {
                    Log.i("****", "Rating installation update has failed", e);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getRatingBar.setRating((int) curRate);
                    }
                });
                Log.i("****", "The Http response is: " + response.toString());
            }
        });
        initialisation = false;
    }

    private void postRating() {
        OkHttpClient client = new OkHttpClient();
        String url = getResources().getString(R.string.rating_url);
        FormBody formBody = new FormBody.Builder()
                .add("installation_id", installationId)
                .add("record_id", recordId)
                .add("playground_name", playgroundName)
                .add("mark", mark)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("****", "Failed to record rating", e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                updateGlobalRating();
                Log.i("****", "Rating has been recorded");
                Log.i("****", "The Http response is: " + response.toString());
            }
        });
    }

    private void recordClick() {
        OkHttpClient client = new OkHttpClient();
        String url = getResources().getString(R.string.store_click_url);
        FormBody formBody = new FormBody.Builder()
                .add("installation_id", installationId)
                .add("record_id", recordId)
                .add("playground_name", playgroundName)
                .add("latitude", lat)
                .add("longitude", lon)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("****", "Failed to record click", e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("****", "Click has been recorded");
                Log.i("****", "The Http response is: " + response.toString());
            }
        });
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        mark = String.valueOf(Math.round(rating));
        if(!initialisation) {
            postRating();
        }
    }

    private class RecordClick extends AsyncTask<Void, Integer, Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            recordClick();
            return null;
        }

    }

    private class UpdateRating extends AsyncTask<Void, Integer, Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            updateGlobalRating();
            return null;
        }

    }

    private class PostRating extends AsyncTask<Void, Integer, Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            postRating();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), "Thanks !", Toast.LENGTH_LONG).show();
        }
    }

}
