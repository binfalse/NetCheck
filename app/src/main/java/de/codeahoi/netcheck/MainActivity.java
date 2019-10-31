package de.codeahoi.netcheck;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    public static final String PREF_URL = "NetCheck:URL";
    public static final String PREF_EXPECT = "NetCheck:CONTENT";

    public static final String DEFAULT_URL = "https://codeahoi.de/netcheck";
    public static final String DEFAULT_EXPECT = "success";

    private EditText url;
    private EditText expect;
    private TextView net_state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.actionlogo);

        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        if (net_state == null)
            net_state = findViewById(R.id.txt_net_state);
        if (url == null)
            url = findViewById(R.id.edit_url);
        if (expect == null)
            expect = findViewById(R.id.edit_expect);

        if (url.getText().length() == 0)
            url.setText(preferences.getString(PREF_URL, DEFAULT_URL));

        if (expect.getText().length() == 0)
            expect.setText(preferences.getString(PREF_EXPECT, DEFAULT_EXPECT));


        final Button save = findViewById(R.id.btn_save);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREF_URL, getUrl());
                editor.putString(PREF_EXPECT, getExpect());
                editor.apply();
                checkNet();
            }
        });

        final Button reset = findViewById(R.id.btn_resest);
        reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                url.setText(DEFAULT_URL);
                expect.setText(DEFAULT_EXPECT);
                save.callOnClick();
            }
        });

        expect.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    save.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Get URL from input field `R.id.`edit_url
     * @return the trimmed input
     */
    private String getUrl() {
        return url.getText().toString().trim();
    }

    /**
     * Get expected string from input field `R.id.edit_expect`
     * @return the trimmed input
     */
    private String getExpect() {
        return expect.getText().toString();
    }

    /**
     * Test the network.
     *
     * Will try to access the web resource provided in `R.id.edit_url` and set the new state and log contents.
     */
    private void checkNet() {

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.getCache().clear();
        StringRequest request = new StringRequest(Request.Method.GET, getUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ((TextView) findViewById(R.id.txt_log_content)).setText(response.trim());
                        if (response.contains(getExpect()))
                            net_state.setText(getString(R.string.ui_net_yes));
                        else
                            net_state.setText(getString(R.string.ui_net_yes_unexpected));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                StringBuilder supp = new StringBuilder("\n\n").append(error.getMessage());
                if (error.networkResponse != null) {

                    supp.append ("\n\nHTTP Status: ").append(error.networkResponse.statusCode).append("\n");
                    for (Header h : error.networkResponse.allHeaders) {
                        supp.append (h.getName()).append(": ").append(h.getValue()).append("\n");
                    }
                }

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    String pre = "";
                    if (error.getMessage().contains("UnknownHostException"))
                        pre = "\n" + getString(R.string.ui_log_no_network_unknownhost);
                    ((TextView) findViewById(R.id.txt_log_content)).setText(String.format(getString(R.string.ui_log_no_network), getString(R.string.ui_log_no_network_timeout), pre, supp.toString()));
                    net_state.setText(getString(R.string.ui_net_not));
                } else if (error instanceof AuthFailureError) {
                    ((TextView) findViewById(R.id.txt_log_content)).setText(String.format(getString(R.string.ui_log_no_network), getString(R.string.ui_log_no_network_autherror), "", supp.toString()));
                    net_state.setText(getString(R.string.ui_net_yes));
                } else if (error instanceof ServerError) {
                    ((TextView) findViewById(R.id.txt_log_content)).setText(String.format(getString(R.string.ui_log_no_network), getString(R.string.ui_log_no_network_servererror), "", supp.toString()));
                    net_state.setText(getString(R.string.ui_net_yes));
                } else if (error instanceof NetworkError) {
                    ((TextView) findViewById(R.id.txt_log_content)).setText(String.format(getString(R.string.ui_log_no_network), getString(R.string.ui_log_no_network_networkerror), "", supp.toString()));
                    net_state.setText(getString(R.string.ui_net_not));
                } else if (error instanceof ParseError) {
                    ((TextView) findViewById(R.id.txt_log_content)).setText(String.format(getString(R.string.ui_log_no_network), getString(R.string.ui_log_no_network_parseerror), "", supp.toString()));
                    net_state.setText(getString(R.string.ui_net_yes));
                }
            }
        });
        request.setShouldCache(false);
        queue.add(request);
    }

}
