package com.example.twitterclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class Onlogin extends AppCompatActivity {
    ListView userlistview;
    List<String> currentuserlist;
    ArrayAdapter<String> currentuseradapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onlogin);
        Intent previntent=getIntent();
        if(previntent.getStringExtra("actionled").equals("signup")){
            ParseUser.getCurrentUser().put("isfollowing",new ArrayList<String>());
            ParseUser.getCurrentUser().saveInBackground();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.onlogin_menu);
        toolbar.getMenu().findItem(R.id.tweetpage).setTitle("View Tweets");
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.logout:
                        ParseUser.logOut();
                        Toast.makeText(Onlogin.this, "Logged Out", Toast.LENGTH_SHORT).show();
                        Intent logout1=new Intent(getApplicationContext(),MainActivity.class);
                        logout1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(logout1);
                        finish();
                        return true;
                    case R.id.tweet:
                        AlertDialog.Builder builder=new AlertDialog.Builder(Onlogin.this);
                        builder.setTitle("Post a Tweet");
                        final EditText tweettext=new EditText(Onlogin.this);
                        builder.setView(tweettext);
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(Onlogin.this, "Tweet Cancelled", Toast.LENGTH_SHORT).show();
                                dialogInterface.cancel();
                            }
                        });
                        builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {
                                if(tweettext.getText().toString().equals("") || tweettext==null){
                                    Toast.makeText(Onlogin.this, "Tweet cannot be empty", Toast.LENGTH_SHORT).show();
                                    dialogInterface.cancel();
                                }else{
                                    ParseObject tweetobject=new ParseObject("Tweets");
                                    tweetobject.put("username",ParseUser.getCurrentUser().getUsername());
                                    tweetobject.put("tweet",tweettext.getText().toString());
                                    tweetobject.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(e==null){
                                                Toast.makeText(Onlogin.this, "Tweet Successfully Posted", Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(Onlogin.this, "Unable to Post the tweet", Toast.LENGTH_SHORT).show();
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
                        Intent intent=new Intent(getApplicationContext(),UserTweets.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        List<String> temp=ParseUser.getCurrentUser().getList("isfollowing");
                        intent.putStringArrayListExtra("following", (ArrayList<String>) temp);
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
        userlistview=(ListView)findViewById(R.id.userlistview);
        currentuserlist=new ArrayList<String>();
        currentuserlist.add("Myself");
        currentuseradapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_checked,currentuserlist);
        userlistview.setAdapter(currentuseradapter);
        userlistview.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        ParseQuery<ParseUser> query=ParseUser.getQuery();
        query.addAscendingOrder("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        currentuserlist.clear();
                        for(ParseUser obj:objects){
                            if(!obj.getUsername().matches(ParseUser.getCurrentUser().getUsername())) {
                                currentuserlist.add(obj.getUsername().toString());
                                List temp=ParseUser.getCurrentUser().getList("isfollowing");
                                if(temp!=null){
                                    if(temp.contains(obj.getUsername()))
                                        userlistview.setItemChecked(currentuserlist.indexOf(obj.getUsername()),true);
                                }
                            }
                            //Log.i("Check of",obj.isAuthenticated()+" "+obj.getUsername());
                        }
                        currentuseradapter.notifyDataSetChanged();
                    }
                }
                else{
                    Toast.makeText(Onlogin.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                    Log.i("Listfind error",""+e);
                }
            }
        });
        userlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckedTextView checkedTextView=(CheckedTextView)view;
                if(checkedTextView.isChecked()){
                    ParseUser.getCurrentUser().add("isfollowing",currentuserlist.get(i));
                }else{
                    ParseUser.getCurrentUser().getList("isfollowing").remove(currentuserlist.get(i));
                    List temp=ParseUser.getCurrentUser().getList("isfollowing");
                    ParseUser.getCurrentUser().remove("isfollowing");
                    ParseUser.getCurrentUser().put("isfollowing",temp);
                }
                ParseUser.getCurrentUser().saveInBackground();
            }
        });
    }
}