package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebImgUrls = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int celebInt;
    ImageView imageView;
    int correctInt = 0;
    String[] answers = new String[4];
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    public void personChosen(View view) {

        if(view.getTag().toString().equals(Integer.toString(correctInt))) {
            Toast.makeText(getApplicationContext(), "Correct", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong, it was: " + celebNames.get(celebInt), Toast.LENGTH_LONG).show();
        }
        createQuestion();
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {


            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    total.append(line).append("\n");
                }


                return total.toString();

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void createQuestion() {
        Random random = new Random();
        celebInt = random.nextInt(celebNames.size());

        ImageDownloader imageTask = new ImageDownloader();
        Bitmap celebImage;
        try {
            celebImage = imageTask.execute(celebImgUrls.get(celebInt)).get();
            imageView.setImageBitmap(celebImage);

            correctInt = random.nextInt(4);
            int incorrect;
            for(int i=0; i<4; i++) {

                if (i==correctInt) {
                    answers[i] = celebNames.get(celebInt);
                }
                else {
                    incorrect = random.nextInt(celebNames.size());
                    while (incorrect == correctInt) {
                        incorrect = random.nextInt(celebNames.size());
                    }
                    answers[i] = celebNames.get(incorrect);
                }
            }
            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        button0 = findViewById(R.id.button1);
        button1 = findViewById(R.id.button2);
        button2 = findViewById(R.id.button3);
        button3 = findViewById(R.id.button4);
        DownloadTask task = new DownloadTask();
        String result = null;

        try {
            result = task.execute("https://www.imdb.com/list/ls052283250/").get();

            String splitResult = result.split("<div class=\"desc lister-total-num-results\">")[1];
            String finalResult = splitResult.split("<div class=\"row text-center lister-working hidden\">")[0];

            Pattern p = Pattern.compile("src=\"(.*?)\"");
            Matcher m = p.matcher(finalResult);

            while (m.find()) {
                celebImgUrls.add(m.group(1));
            }

            p = Pattern.compile("img alt=\"(.*?)\"");
            m = p.matcher(finalResult);

            while (m.find()) {
                celebNames.add(m.group(1));
            }


//            Log.i("Web Info Receieved", finalResult);

        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        createQuestion();

    }
}