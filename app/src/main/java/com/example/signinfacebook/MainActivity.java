package com.example.signinfacebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private CallbackManager mcallbackManager;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private TextView textViewUser;
    private ImageView mlogo;
    private LoginButton loginButton;
    private AccessTokenTracker accessTokenTracker;
    private static final String TAG = "Facebook Authentication";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = mFirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        textViewUser = findViewById(R.id.text_user);
        mlogo = findViewById(R.id.logo);
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("Email","Public_Profile");
        mcallbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(mcallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "On Success" + loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "On Cancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "On Error" + error);

            }
        });

         authStateListener = new FirebaseAuth.AuthStateListener() {
             @Override
             public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                 FirebaseUser user = firebaseAuth.getCurrentUser();
                 if(user!=null){
                     updateUI(user);
                 }
                 else {
                     updateUI(null);
                 }
             }
         };
         accessTokenTracker = new AccessTokenTracker() {
             @Override
             protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                 if(currentAccessToken==null){
                     mFirebaseAuth.signOut();
                 }
             }
         };
    }

    private void handleFacebookToken(AccessToken token){
        Log.d(TAG,"handleFacebookToken"+ token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "sign in with credentail: successful");
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    Log.d(TAG, "sign in with credentail: failure", task.getException());
                    Toast.makeText(MainActivity.this,"Authentication Failed",Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mcallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            textViewUser .setText(user.getDisplayName());
            if(user.getPhotoUrl() != null){
                String photoUrl = user.getPhotoUrl().toString();
                photoUrl = photoUrl + "?type+large";
                Picasso.get().load(photoUrl).into(mlogo);

            }
        }

        else {
            textViewUser.setText("");
            mlogo.setImageResource(R.drawable.logo);
        }
        }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener!=null){
            mFirebaseAuth.removeAuthStateListener(authStateListener);

        }
    }
}
