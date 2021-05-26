package com.example.creerode.foodorder;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class OrderHistory  extends AppCompatActivity{

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    Context context=this;
    String response;
    String email;
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderhistory);

        email=getDefaults("userid",this);


        recyclerView = (RecyclerView) findViewById(R.id.orderhistory_recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        FloatingActionButton o_fab=(FloatingActionButton)findViewById(R.id.o_fab);
        o_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(OrderHistory.this,MainActivity.class);
                startActivity(i);
            }


        });


        OrderHistory.AsyncGet a= new OrderHistory.AsyncGet();
        a.execute();

    }
    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }
    public class AsyncGet extends AsyncTask<String, String, String>
    {

        ProgressDialog pdLoading = new ProgressDialog(OrderHistory.this);
        HttpURLConnection conn;
        URL url = null;
        String results="";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }
        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                //url = new URL("https://inamtginamtg.000webhostapp.com/displaycurrentorders.php?email="+email);
                url = new URL(LoginActivity.weburl+"orderhistory.php?email="+email);
                Log.i("url1",url.toString());

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                //Uri.Builder builder = new Uri.Builder();

                //String query = builder.build().getEncodedQuery();
                Log.i("url2",url.toString());

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                //               writer.write(query);
                writer.write(url.toString());

                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                //if (response_code == HttpURLConnection.HTTP_OK) {

                // Read data sent from server
                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                results=result.toString();
                response=results;



                Log.i("result",results);
                // Pass data to onPostExecute method
                return(result.toString());

//            }else{
//
//                return("unsuccessful");
//            }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArrayList<OrderHistoryDataModel> itemslist = OrderHistoryDataModel.fromJson(jsonArray);


            adapter=new OrderHistoryRecyclerAdapter(context,itemslist);
            recyclerView.setAdapter(adapter);


            if(result!=null)
            {

                Toast.makeText(OrderHistory.this, "Items Fetched successfully", Toast.LENGTH_LONG).show();
            }

            else {
                Toast.makeText(OrderHistory.this, "Fetch Failed.", Toast.LENGTH_LONG).show();
            }


        }
        String getjsonString()
        {
            return results;
        }


    }

}

