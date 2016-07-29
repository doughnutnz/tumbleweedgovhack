package com.example.stefan.mapdrawer;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class ViewRecordActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener {

    private JSONArray parksJson;
    private JSONObject mRecord;

    private RatingBar getRatingBar;
    private RatingBar setRatingBar;
    private TextView countText;
    private int count;
    private float curRate;

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
            String recordId = b.getString("record_id");
            mRecord = findRecordById(recordId);

            try {
                toolbar.setTitle(mRecord.getString("name"));
                setSupportActionBar(toolbar);

                String details = "<h2>Address</h2>" +
                        "<p>" + mRecord.getString("address") + "</p>" +
                        "<p>" + mRecord.getString("geocode_address") + "</p>" +
                        "<h2>Facilities</h2>" +
                        "<p>" + mRecord.getString("facilities") + "</p>" +
                        "<h2>About</h2>" +
                        "<p>" + mRecord.getString("about") + "</p>";
                ((TextView) findViewById(R.id.record_details)).setText(Html.fromHtml(details));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        findViewsById();

        getRatingBar.setOnRatingBarChangeListener(this);
        setRatingBar.setRating(curRate);
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

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        rating = (float) Math.round(rating);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        curRate = Float.valueOf(decimalFormat.format((curRate * count + rating)
                / ++count));
        Toast.makeText(ViewRecordActivity.this,
                "New Rating: " + curRate, Toast.LENGTH_SHORT).show();
        setRatingBar.setRating(curRate);
        countText.setText(count + " Ratings");
    }
}
