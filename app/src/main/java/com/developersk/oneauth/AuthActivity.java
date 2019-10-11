package com.developersk.oneauth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Pattern;

public class AuthActivity extends BaseActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "AuthActivity";
    private static final int RC_SIGN_IN = 9001;
    private Button tabLogin, tabReg;
    private LinearLayout llLogin, llReg;
    private ImageView logo;
    private EditText loginEmail, loginPassword, regFName, regLName, regEmail, regPassword, regPassword2;
    private Button btLogin, btReg;
    private AppCompatImageView googleAuth;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        logo = findViewById(R.id.iv_logo);
        tabLogin = findViewById(R.id.tab_login);
        tabReg = findViewById(R.id.tab_register);
        llLogin = findViewById(R.id.ll_login);
        llReg = findViewById(R.id.ll_reg);

        loginEmail = findViewById(R.id.loginemail);
        loginPassword = findViewById(R.id.loginpassword);
        regEmail = findViewById(R.id.reg_email);
        regFName = findViewById(R.id.first_name);
        regLName = findViewById(R.id.last_name);
        regPassword = findViewById(R.id.reg_pass);
        regPassword2 = findViewById(R.id.reg_pass_retype);
        btLogin = findViewById(R.id.bt_login);
        btReg = findViewById(R.id.bt_register);
        googleAuth = findViewById(R.id.google_auth);

        btLogin.setOnClickListener(this);
        btReg.setOnClickListener(this);
        googleAuth.setOnClickListener(this);

        Animation fadeIn = AnimationUtils.loadAnimation(AuthActivity.this, R.anim.fade_in);
        logo.startAnimation(fadeIn);

        llLogin.setVisibility(View.VISIBLE);
        llReg.setVisibility(View.GONE);
        tabLogin.setBackground(getDrawable(R.drawable.ic_rectangle));
        tabReg.setBackgroundColor(getColor(R.color.dark_back));

        tabReg.setOnClickListener(v -> tapToBounce
                ("reg"));
        tabLogin.setOnClickListener(v -> tapToBounce("login"));

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
            }
        };
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(this, "SignIn Failed...", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideProgressDialog();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    private void tapToBounce(String inp) {
        final Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 15);
        bounce.setInterpolator(interpolator);

        final Animation fade_slide = AnimationUtils.loadAnimation(this, R.anim.slide_fade_down);
        if (inp.equals("reg")) {
            tabReg.setBackground(getDrawable(R.drawable.ic_rectangle));
            tabReg.setTextColor(Color.WHITE);
            tabReg.startAnimation(bounce);
            tabLogin.setBackgroundColor(getColor(R.color.default_back));
            tabLogin.setTextColor(getColor(R.color.green));
            tabReg.setBackground(getDrawable(R.drawable.ic_rectangle));
            tabLogin.setBackgroundColor(getColor(R.color.dark_back));

            llReg.setVisibility(View.VISIBLE);
            llLogin.setAlpha(0.0f);
            llLogin.animate()
                    .translationY(llLogin.getHeight())
                    .alpha(1.0f)
                    .setListener(null);
            llLogin.setVisibility(View.GONE);

            llReg.animate()
                    .translationY(0)
                    .alpha(1.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            llReg.setVisibility(View.VISIBLE);
                        }
                    });

        } else if (inp.equals("login")) {
            tabLogin.setBackground(getDrawable(R.drawable.ic_rectangle));
            tabLogin.setTextColor(Color.WHITE);
            tabLogin.startAnimation(bounce);
            tabReg.setBackgroundColor(getColor(R.color.default_back));
            tabReg.setTextColor(getColor(R.color.green));
            tabLogin.setBackground(getDrawable(R.drawable.ic_rectangle));
            tabReg.setBackgroundColor(getColor(R.color.dark_back));

            llLogin.setVisibility(View.VISIBLE);
            llReg.setAlpha(0.0f);
            llReg.animate()
                    .translationY(llLogin.getHeight())
                    .alpha(1.0f)
                    .setListener(null);
            llReg.setVisibility(View.GONE);

            llLogin.animate()
                    .translationY(0)
                    .alpha(1.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            llLogin.setVisibility(View.VISIBLE);
                        }
                    });

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_register:
                createAccount(regEmail.getText().toString(), regPassword.getText().toString());
                break;
            case R.id.bt_login:
                signIn(loginEmail.getText().toString(), loginPassword.getText().toString());
                break;
            case R.id.google_auth:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void createAccount(String email, String password) {
        if (!validateForm(0)) {
            return;
        }
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideProgressDialog();
            }
        });
    }

    private void signIn(String email, String password) {
        if (!validateForm(1)) {
            return;
        }
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
            hideProgressDialog();
        });
    }


    private boolean validateForm(int isLogin) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        if (isLogin == 1) {
            if (TextUtils.isEmpty(loginEmail.getText().toString().trim())) {
                loginEmail.setError("Required.");
                return true;
            } else if (TextUtils.isEmpty(loginPassword.getText().toString().trim())) {
                loginPassword.setError("Required.");
                return true;
            } else if (!pat.matcher(loginEmail.getText().toString().trim()).matches()) {
                return true;
            } else {
                loginEmail.setError(null);
                loginPassword.setError(null);
                return false;
            }
        } else {
            if (TextUtils.isEmpty(regEmail.getText().toString().trim())) {
                regEmail.setError("Required.");
                return true;
            } else if (TextUtils.isEmpty(regPassword.getText().toString().trim())) {
                regPassword.setError("Required.");
                return true;
            } else if (!pat.matcher(regEmail.getText().toString().trim()).matches()) {
                return true;
            } else {
                regEmail.setError(null);
                regPassword.setError(null);
                return false;
            }
        }
    }
}
