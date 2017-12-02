package com.astralbody888.alexanderconner.guesswhoapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

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

    ArrayList<String> personURLs = new ArrayList<String>();
    ArrayList<String> personNames = new ArrayList<String>();
    int chosenPerson = 0;
    int locationCorrectAnswer = 0;
    int numButtons;
    String[] answers = new String[4];


    ImageView personImageView;

    GridLayout gridLayout;
    Button button1, button2, button3, button4;


    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

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

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data !=-1) {
                    char current = (char) data;

                    result += current;

                    data = reader.read();
                }
                return result;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        personImageView = (ImageView) findViewById(R.id.mainImageView);

        gridLayout = (GridLayout) findViewById(R.id.gridLayout);

        DownloadTask task = new DownloadTask();
        String result = null;
        String url = "http://www.posh24.se/kandisar";
        try {
            result = task.execute(url).get();
            Log.i("URL Contents", result);
            //Split the page HTML in half to help us narrow down content
            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            //Find patterns for persons image source + their names in alt tag
            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while (m.find()) {
                System.out.println(m.group(1));
                personURLs.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()) {
                System.out.println(m.group(1));
                personNames.add(m.group(1));
            }

            createNewQuestion();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void guessName(View view) {
        if (view.getTag().toString().equals(Integer.toString(locationCorrectAnswer))) {
            Toast.makeText(getApplicationContext(), "Correct", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong! Answer was " + answers[locationCorrectAnswer], Toast.LENGTH_LONG).show();
        }
        try {
            createNewQuestion();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void createNewQuestion() throws ExecutionException, InterruptedException {
        Random random = new Random();
        chosenPerson = random.nextInt(personURLs.size());

        //Now download image.
        ImageDownloader imageTask = new ImageDownloader();

        Bitmap personImage;

        personImage = imageTask.execute(personURLs.get(chosenPerson)).get();

        personImageView.setImageBitmap(personImage);

        locationCorrectAnswer = random.nextInt(4);

        int incorrectAnswerLocation;

        numButtons = gridLayout.getChildCount();
        Log.i("NumBottons", " " + numButtons);

        //For each button, fill with celeb name. if i is the designated location,
        //set answer to that location. else get random name, and make sure its not the same
        for (int i = 0; i < numButtons; i++){
            if (i == locationCorrectAnswer) {
                answers[i] = personNames.get(chosenPerson);
            } else {
                incorrectAnswerLocation = random.nextInt(personURLs.size());
                while (incorrectAnswerLocation == chosenPerson)
                {
                    incorrectAnswerLocation = random.nextInt(personURLs.size());
                }
                answers[i] = personNames.get(incorrectAnswerLocation);
            }
        }
        //set buttons in gridLayout to answers
        for (int i = 0; i < numButtons; i++)
        {
            Button btn = (Button) gridLayout.getChildAt(i);
            btn.setText(answers[i]);
        }
    }
}
