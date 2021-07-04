 package com.example.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

 public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener
 , PublisherKit.PublisherListener {

    private static String API_KEY = "46878964";
    private static String SESSION_ID = "1_MX40Njg3ODk2NH5-MTU5NzAzNTA0Njk0N35KNUdFanhXSVJCbkxBaVVLSVN0MkNINWZ-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00Njg3ODk2NCZzaWc9MzJiNGVlYTgxZDc0YmJjZTgzMmI2YjBhYWY2MTc5NDc0Y2JmNjQwZjpzZXNzaW9uX2lkPTFfTVg0ME5qZzNPRGsyTkg1LU1UVTVOekF6TlRBME5qazBOMzVLTlVkRmFuaFhTVkpDYmt4QmFWVkxTVk4wTWtOSU5XWi1mZyZjcmVhdGVfdGltZT0xNTk3MDM1MTQ4Jm5vbmNlPTAuODc4Mjc2ODYyNDg0MDA1MiZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTk5NjI3MTQ2JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";

    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();

    private static final int RC_VIDEO_APP_PERM = 124;



    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
     private ImageView closeVideoChatBtn;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;




    private DatabaseReference usersRef;
    private String userID="";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);


        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);

        closeVideoChatBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.child(userID).hasChild("Ringing"))
                        {

                            //current user is the receiver of the call
                            //now, we have to delete that node

                            usersRef.child(userID).child("Ringing").removeValue();

                        if(mPublisher !=null)
                        {
                            mPublisher.destroy();
                        }
                        if(mSubscriber !=null)
                        {
                            mSubscriber.destroy();
                        }





                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();


                        }

                        if(dataSnapshot.child(userID).hasChild("Calling"))
                        {
                            //current user is the caller of the call
                            //now, we have to delete that node

                            usersRef.child(userID).child("Calling").removeValue();

                            if(mPublisher !=null)
                            {
                                mPublisher.destroy();
                            }
                            if(mSubscriber !=null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();


                        }
                        else
                        {
                            if(mPublisher !=null)
                            {
                                mPublisher.destroy();
                            }
                            if(mSubscriber !=null)
                            {
                                mSubscriber.destroy();
                            }

                            //if the child is already deleted

                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });



        requestPermisions();

    }


      //this method basically check the request of permisson
     //requestPermission()
     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
     {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);

         EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);

     }

     //now lets request for permissons (video and audio)

     @AfterPermissionGranted(RC_VIDEO_APP_PERM)
     private void requestPermisions()
     {
         String [] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

         if(EasyPermissions.hasPermissions(this,perms))  //if we get permissions
         {
             mPublisherViewController = findViewById(R.id.publisher_container);
             mSubscriberViewController = findViewById(R.id.subscriber_container);


             //1. initialize and connect to te Session

             //Session.Builder returns a new session instance
             mSession  = new Session.Builder(this, API_KEY,SESSION_ID).build();

             //this method sets the object that will implement the session listener interface
             //this interface includes the callback methods
             //these callback methods are called in response to session  related events
             mSession.setSessionListener(VideoChatActivity.this);


             //this method connect the client's application to the opentalk session
             mSession.connect(TOKEN);

         }
         else
         {
             //if user do not give permissions

             EasyPermissions.requestPermissions(this,"Hey! thia app needs camera and audio permissions! Please Allow!!",RC_VIDEO_APP_PERM,perms);



         }

     }

     @Override
     public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

     }

     @Override
     public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

     }

     @Override
     public void onError(PublisherKit publisherKit, OpentokError opentokError) {

     }



     //2. Publishing a stream to the session

     @Override
     public void onConnected(Session session)
     {
         Log.d(LOG_TAG,"Session Conncected!");

         mPublisher = new Publisher.Builder(this).build();

         //this method will implement the publisher listener interface
         //this interface includes callback methods
         //these callback methods are called in response to publisher related events
         mPublisher.setPublisherListener(VideoChatActivity.this);

         //now get the view

         mPublisherViewController.addView(mPublisher.getView());

         if(mPublisher.getView() instanceof GLSurfaceView)
         {
             ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
         }

         mSession.publish(mPublisher);




     }

     @Override
     public void onDisconnected(Session session)
     {
         Log.i(LOG_TAG,"Stream Disconnected");

         if(mSubscriber !=null)
         {
             mSubscriber = null;

             mSubscriberViewController.removeAllViews();
         }


     }


     //3. Subscribing to the stream
     @Override
     public void onStreamReceived(Session session, Stream stream)
     {
         Log.d(LOG_TAG, "Stream Received");

         //if subscriber is not talking to someone else
         if(mSubscriber==null)
         {
             //we're going to receive the stream

             mSubscriber = new Subscriber.Builder(this,stream).build();
             mSession.subscribe(mSubscriber);
             mSubscriberViewController.addView(mSubscriber.getView());


         }


     }

     @Override
     public void onStreamDropped(Session session, Stream stream)
     {
         Log.i(LOG_TAG,"Stream Dropped");


     }

     @Override
     public void onError(Session session, OpentokError opentokError)
     {
         Log.i(LOG_TAG,"Stream Error");


     }
 }
