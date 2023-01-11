package fr.doko.walkinggame.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import fr.doko.walkinggame.R;
import fr.doko.walkinggame.databinding.ActivityAuthorityBinding;

public class AuthorityActivity extends AppCompatActivity {
    ActivityAuthorityBinding binding;
    private long backKeyPressedTime = 0;
    protected static final String SHARED_PREFS_NAME = "fr.doko.walking_game.DataStorage";
    protected static final String PSEUDO_SUPER = "PSEUDO_SUPER";
    private String pseudo = "";
    EditText username_TV;
    Button buttonPromptUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authority);

        username_TV = (EditText) findViewById(R.id.username);
        buttonPromptUser = findViewById(R.id.buttonPromptUser);

        binding.buttonRecognition.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(AuthorityActivity.this,
                    Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
                }
            }
        });

        buttonPromptUser = findViewById(R.id.buttonPromptUser);
        buttonPromptUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pseudo = username_TV.getText().toString();
                addPseudoToSharedPreferences(AuthorityActivity.this, pseudo);
                checkPermission(pseudo);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    protected void checkPermission(String pseudo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] PERMISSIONS = new String[]{Manifest.permission.ACTIVITY_RECOGNITION};
            boolean ACTIVITY_RECOGNITION = false;
            for (String PERMISSION : PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                    if (PERMISSION.equals(Manifest.permission.ACTIVITY_RECOGNITION)) {
                        ACTIVITY_RECOGNITION = true;
                    }
                }
            }
            if (!ACTIVITY_RECOGNITION && !pseudo.isEmpty()) {
                Intent intent = new Intent(this, PedometerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(AuthorityActivity.this, "Cliquez une fois de plus sur le bouton 'Retour' pour quitter.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
        }
    }

    protected static void addPseudoToSharedPreferences(Context context, String name) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
        SharedPreferences.Editor myEditor = mySharedPreferences.edit();
        myEditor.putString(PSEUDO_SUPER, name);
        myEditor.commit();

    }

    public static String getPseudoFromSharedPreferences(Context context) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
        return mySharedPreferences.getString(PSEUDO_SUPER, "");
    }


}
