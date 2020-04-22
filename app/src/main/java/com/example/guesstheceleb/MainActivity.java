package com.example.guesstheceleb;

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

public class MainActivity extends AppCompatActivity
{
    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int choosencelebrity =0;
    ImageView imageView;
    String[] answers = new String[4];
    int locationofcorrectanswers=0;
    Button button0, button1, button2, button3;

    public void celebchosen(View view)
    {
        if(view.getTag().toString().equals(Integer.toString(locationofcorrectanswers)))
        {
            Toast.makeText(getApplicationContext(),"Correct",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Wrong! it was "+ celebNames.get(choosencelebrity),Toast.LENGTH_SHORT).show();
        }
        newquestion();
    }

    public void newquestion()
    {
        Random rand = new Random();
        choosencelebrity = rand.nextInt(celebURLs.size());

        imagedownloader imagetask = new imagedownloader();
        Bitmap celebimage;

        try
        {
            celebimage = imagetask.execute(celebURLs.get(choosencelebrity)).get();
            imageView.setImageBitmap(celebimage);
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        locationofcorrectanswers=rand.nextInt(4);
        int incorrectanswerlocation;
        for(int i=0; i<4;i++)
        {
            if (i == locationofcorrectanswers)
            {
                answers[i] = celebNames.get(choosencelebrity);
            }
            else
            {
                incorrectanswerlocation = rand.nextInt(celebURLs.size());

                while (incorrectanswerlocation == choosencelebrity)
                {
                    incorrectanswerlocation = rand.nextInt(celebURLs.size());
                }
                answers[i] = celebNames.get(incorrectanswerlocation);
            }
        }
        button0.setText(answers[0]);
        button1.setText(answers[1]);
        button2.setText(answers[2]);
        button3.setText(answers[3]);
    }

    public class imagedownloader extends AsyncTask<String, Void, Bitmap>
    {

        @Override
        protected Bitmap doInBackground(String... urls)
        {
            try
            {
                URL url=new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap mybitmap = BitmapFactory.decodeStream(in);
                return mybitmap;
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class downloadtask extends AsyncTask<String,Void, String>
    {
        String result;
        URL url;
        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... urls)
        {
            try
            {
                url=new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader= new InputStreamReader(in);
                int data = reader.read();

                while(data != -1)
                {
                    char current= (char) data;
                    result+=current;
                    data= reader.read();
                }

            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
                return null;
            } catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
            return result;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        button0= findViewById(R.id.button0);
        button1=findViewById(R.id.button1);
        button2=findViewById(R.id.button2);
        button3=findViewById(R.id.button3);

        downloadtask task = new downloadtask();
        try
        {
            String result=task.execute("http://www.posh24.se/kandisar").get();
            String[] splitresult = result.split("<div class=\"listedArticles\">");
            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m= p.matcher(splitresult[0]);

            while(m.find())
            {
               celebURLs.add(m.group(1));
            }

             p = Pattern.compile("alt=\"(.*?)\"");
             m= p.matcher(splitresult[0]);
            while(m.find())
            {
                celebNames.add(m.group(1));
            }
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

       newquestion();
    }
}
