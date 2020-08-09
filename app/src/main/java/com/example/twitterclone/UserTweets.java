package com.example.twitterclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserTweets extends AppCompatActivity {
    Intent prevIntent;
    ListView tweetlistview;
    List<String> following;
    SimpleAdapter tweetadapter;
    List<Map<String,String>> tweetlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tweets);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.onlogin_menu);
        toolbar.getMenu().findItem(R.id.tweetpage).setTitle("View Following");
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.logout:
                        ParseUser.logOut();
                        Toast.makeText(UserTweets.this, "Logged Out", Toast.LENGTH_SHORT).show();
                        Intent logout1=new Intent(getApplicationContext(),MainActivity.class);
                        logout1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(logout1);
                        finish();
                        return true;
                    case R.id.tweet:
                        AlertDialog.Builder builder=new AlertDialog.Builder(UserTweets.this);
                        builder.setTitle("Post a Tweet");
                        final EditText tweettext=new EditText(UserTweets.this);
                        builder.setView(tweettext);
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(UserTweets.this, "Tweet Cancelled", Toast.LENGTH_SHORT).show();
                                dialogInterface.cancel();
                            }
                        });
                        builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {
                                if(tweettext.getText().toString().equals("") || tweettext==null){
                                    Toast.makeText(UserTweets.this, "Tweet cannot be empty", Toast.LENGTH_SHORT).show();
                                    dialogInterface.cancel();
                                }else{
                                    ParseObject tweetobject=new ParseObject("Tweets");
                                    tweetobject.put("username",ParseUser.getCurrentUser().getUsername());
                                    tweetobject.put("tweet",tweettext.getText().toString());
                                    tweetobject.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(e==null){
                                                Toast.makeText(UserTweets.this, "Tweet Successfully Posted", Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(UserTweets.this, "Unable to Post the tweet", Toast.LENGTH_SHORT).show();
                                                Log.i("Tweet post error",e+"");
                                            }
                                            dialogInterface.cancel();
                                        }
                                    });
                                }
                            }
                        });
                        builder.show();
                        return true;
                    case R.id.tweetpage:
                        Intent intent=new Intent(getApplicationContext(),Onlogin.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("actionled","login");
                        startActivity(intent);
                        return true;
                    default:return false;
                }
            }
        });
        if(ParseUser.getCurrentUser()==null){
            Toast.makeText(this, "Login to Proceed Further", Toast.LENGTH_SHORT).show();
            Intent logout1=new Intent(getApplicationContext(),MainActivity.class);
            logout1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(logout1);
            finish();
        }
        prevIntent=getIntent();
        following=prevIntent.getStringArrayListExtra("following");
        tweetlistview=(ListView)findViewById(R.id.tweetlistview);
        tweetlist=new ArrayList<Map<String,String>>();
        ParseQuery<ParseObject> query=ParseQuery.getQuery("Tweets");
        query.whereContainedIn("username",following);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    for(ParseObject obj:objects){
                        Map<String,String> tweet=new HashMap<String, String>();
                        tweet.put("content",obj.getString("tweet"));
                        tweet.put("username",obj.getString("username"));
                        tweetlist.add(tweet);
                        //Log.i("check",tweet.toString());
                    }
                    //Log.i("check",tweetlist.toString());
                    tweetadapter=new SimpleAdapter(UserTweets.this,tweetlist,android.R.layout.simple_list_item_2,new String[]{"content","username"},new int[]{android.R.id.text1,android.R.id.text2});
                    //Log.i("check","tweetadapter "+tweetadapter);
                    tweetlistview.setAdapter(tweetadapter);
                }else{
                    Toast.makeText(UserTweets.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    Log.i("Tweet fetching error",e+"");
                }
            }
        });
    }
}