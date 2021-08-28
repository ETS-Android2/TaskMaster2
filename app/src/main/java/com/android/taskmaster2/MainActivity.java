package com.android.taskmaster2;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.analytics.AnalyticsEvent;
import com.amplifyframework.analytics.pinpoint.AWSPinpointAnalyticsPlugin;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.Team;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ViewAdapter viewAdapter;
    private List<TaskItem> taskList;
    private List<com.amplifyframework.datastore.generated.model.TaskItem> commingList= TaskManager.getInstance().getData();

    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String STATE = "state";
    public static final String TEAM = "team";

    private static final String TAG = "MainActivity";
    private AppBarConfiguration appBarConfiguration;

    private static PinpointManager pinpointManager;


    int count=1;
    String teamId;
    String team;
    Handler handler;
    RecyclerView taskRecycleView;


    public static PinpointManager getPinpointManager(final Context applicationContext) {
        if (pinpointManager == null) {
            final AWSConfiguration awsConfig = new AWSConfiguration(applicationContext);
            AWSMobileClient.getInstance().initialize(applicationContext, awsConfig, new Callback<UserStateDetails>() {

                @Override
                public void onResult(UserStateDetails userStateDetails) {
                    Log.i("INIT", userStateDetails.getUserState().toString());
                }

                @Override
                public void onError(Exception e) {
                    Log.e("INIT", "Initialization error.", e);
                }
            });

            PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                    applicationContext,
                    AWSMobileClient.getInstance(),
                    awsConfig);

            pinpointManager = new PinpointManager(pinpointConfig);

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            }
                            final String token = task.getResult();
                            Log.d(TAG, "Registering push notifications token: " + token);
                            pinpointManager.getNotificationClient().registerDeviceToken(token);
                        }
                    });
        }
        return pinpointManager;
    }


    @Override
    protected void onResume (){
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sharedPreferences.getString("Username","go set your info in setting !!");
        TextView usernameView = findViewById(R.id.Username_main);
        team=sharedPreferences.getString("team","Team A");
        usernameView.setText(username);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize PinpointManager
        getPinpointManager(getApplicationContext());


//        try {
//            Amplify.addPlugin(new AWSApiPlugin());
//            Amplify.addPlugin(new AWSDataStorePlugin());
//            Amplify.addPlugin(new AWSPinpointAnalyticsPlugin(this));
//
//            Amplify.addPlugin(new AWSCognitoAuthPlugin());
//            Amplify.configure(getApplicationContext());
//            Log.i("Tutorial", "Initialized Amplify");
//        } catch (AmplifyException failure) {
//            Log.e("Tutorial", "Could not initialize Amplify", failure);
//        }

        handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Objects.requireNonNull(taskRecycleView.getAdapter()).notifyDataSetChanged();
                return false;
            }
        });
//commingList=new ArrayList<>();

        getTeamFromAPI();

//        AppDB db = Room.databaseBuilder(getApplicationContext(),
//                AppDB.class, AddTask.TASK).allowMainThreadQueries().build();

//        TaskDao taskDao = db.taskDao();
//        taskList = taskDao.findAll();
//        commingList.add(new com.amplifyframework.datastore.generated.model.TaskItem("12","title A","body A","new",new Team("1","team A")));
//        commingList.add(new com.amplifyframework.datastore.generated.model.TaskItem("12","title A","body A","new",new Team("1","team A")));
//        commingList.add(new com.amplifyframework.datastore.generated.model.TaskItem("12","title A","body A","new",new Team("1","team A")));


        ImageButton menuBtn = findViewById(R.id.imageButton);
        menuBtn.setOnClickListener(v -> {
            Intent menuIntent = new Intent(MainActivity.this, SettingPage.class);
            doanalitics();
            startActivity(menuIntent);
        });
        Button allTaskBtn = MainActivity.this.findViewById(R.id.allTaskBtn);
//        recordAnEvent("NavigateToAddTasksActivity");
        allTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AllTask.class);
                doanalitics();
                MainActivity.this.startActivity(intent);
            }
        });
        Button addTaskBtn = MainActivity.this.findViewById(R.id.addBtn);
        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTask.class);
                doanalitics();
                MainActivity.this.startActivity(intent);
            }
        });



    }



    private void dataSetChanged(){viewAdapter.notifyDataSetChanged();}

    private void getTeamFromAPI(){
        Amplify.API.query(ModelQuery.list(Team.class,Team.NAME.eq(team)),
                response ->{

                    for(Team item : response.getData()){

                        commingList=item.getTaskitem();
                        Log.i("coming","on create : ------------ =>"+item.getTaskitem());
                    }
                        taskRecycleView = findViewById(R.id.list);
                        viewAdapter = new ViewAdapter(commingList, new ViewAdapter.OnTaskItemClickListener() {
                            @Override
                            public void onTaskClicked(int position) {
                                Intent detailsPage = new Intent(getApplicationContext(), TaskDetailPage.class);
                                detailsPage.putExtra(TITLE,commingList.get(position).getTitle());
                                detailsPage.putExtra(BODY,commingList.get(position).getBody());
                                detailsPage.putExtra(STATE,commingList.get(position).getState());
                                detailsPage.putExtra(TEAM,commingList.get(position).getTeam().getName());
//                team=commingList.get(position).getTeam().getId();
                                startActivity(detailsPage);

                            }
                        });
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                            this,
                            LinearLayoutManager.VERTICAL,
                            false);

                    Log.i("viewAdapterCHange","on create : the item is =>"+commingList);

                    runOnUiThread(() ->{
                        taskRecycleView.setLayoutManager(linearLayoutManager);
                        taskRecycleView.setAdapter(viewAdapter);
                        taskRecycleView.getAdapter().notifyDataSetChanged();
                    });


                },
                error -> Log.e("error","onCreate faild"+error.toString())
        );
    }

public void doanalitics() {
    AnalyticsEvent event = AnalyticsEvent.builder()
            .name("PasswordReset")
            .addProperty("Channel", "SMS")
            .addProperty("Successful", true)
            .addProperty("ProcessDuration", 792)
            .addProperty("UserAge", 120.3)
            .build();
    Amplify.Analytics.recordEvent(event);
}
//    private void getTasksFromAPI(){
//        Amplify.API.query(ModelQuery.list(com.amplifyframework.datastore.generated.model.TaskItem.class, com.amplifyframework.datastore.generated.model.TaskItem.TEAM.eq(teamId)),
//                response ->{
//
//                    for(com.amplifyframework.datastore.generated.model.TaskItem item : response.getData()){
//                        commingList.add(item);
//                        Log.i("coming","on create : ------------ =>"+item);
//                    }
//                    handler.sendEmptyMessage(1);
//                },
//                error -> Log.e("error","onCreate faild"+error.toString())
//        );
//    }




}