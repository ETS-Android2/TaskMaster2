package com.android.taskmaster2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.amplifyframework.core.Amplify;

import java.io.File;
import java.util.Date;

public class TaskDetailPage extends AppCompatActivity {

    String titleName;
    private ImageView taskImage ;
    private File downloadedImage ;
    private Handler handleImageView ;
    private static final String TAG = "TaskDetailsActivity";
    String exctension;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail_page);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        exctension = sharedPreferences.getString("fileType",null);


        taskImage = findViewById(R.id.downloadFile);


        Log.i(TAG, "onCreate:  DIRCTORY -->   " +getApplicationContext().getFilesDir());
        getFileFromApi();


        Intent comingIntent = getIntent();
        TextView title = findViewById(R.id.textView7);
        TextView body = findViewById(R.id.lorem_ipsum);
        TextView state = findViewById(R.id.state);
        TextView team = findViewById(R.id.team);
        titleName = comingIntent.getExtras().getString(MainActivity.TITLE);
        String bodytask = comingIntent.getExtras().getString(MainActivity.BODY);
        String statetask = comingIntent.getExtras().getString(MainActivity.STATE);
        String teamtask = comingIntent.getExtras().getString(MainActivity.TEAM);

        title.setText(titleName);
        body.setText(bodytask);
        state.setText(statetask);
        team.setText(teamtask);
    }

    private void setTaskImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8; // down sizing image as it throws OutOfMemory  Exception for larger images
        Bitmap bitmap = BitmapFactory.decodeFile(downloadedImage.getAbsolutePath() , options);
        taskImage.setImageBitmap(bitmap);
    }



    private void getFileFromApi(){

        if(exctension == "Image"){
            String fileFey =titleName+".png";
            Amplify.Storage.downloadFile(
                    fileFey,
                    new File(getApplicationContext().getFilesDir() + titleName+".png") ,
                    success -> {
                        Log.i(TAG, "getFileFromApi: successfully   ----> " + success.toString());
                        downloadedImage = success.getFile();
                        handleImageView.sendEmptyMessage(1);
                    },
                    failure -> Log.i(TAG, "getFileFromApi:  failed  ---> " + failure.toString())
            ) ;
        }
        else {

            Amplify.Storage.getUrl(
                    titleName,
                    result -> Log.i("MyAmplifyApp", "Successfully generated: " + result.getUrl()),
                    error -> Log.e("MyAmplifyApp", "URL generation failure", error)
            );
        }

    }

}