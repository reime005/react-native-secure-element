package com.mariusreimer.foo;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.android.secureelement.ISecureElement;
import com.android.secureelement.KeyGenParameters;
import com.android.secureelement.SecureElement;
import com.android.secureelement.enums.BlockMode;
import com.android.secureelement.enums.EncryptionPadding;
import com.android.secureelement.enums.KeyAlgorithm;
import com.android.secureelement.enums.KeyGenPurpose;
import com.android.secureelement.enums.KeyProvider;
import com.android.secureelement.enums.Purpose;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ISecureElement secureElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KeyGenParameters params = new KeyGenParameters(
                KeyAlgorithm.RSA,
                KeyProvider.AndroidKeyStore,
                BlockMode.ECB,
                EncryptionPadding.PKCS1Padding,
                false,
                false,
                0,
                new Purpose(new KeyGenPurpose[]{KeyGenPurpose.ENCRYPT, KeyGenPurpose.DECRYPT}),
                "defaultKeyName",
                "Default title",
                "Default description"
        );

        secureElement = new SecureElement(getApplicationContext());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final String rawText = ((TextView) findViewById(R.id.text_raw)).getText().toString();

        findViewById(R.id.btn_encrypt).setOnClickListener((view) -> {
            try {
                String res = secureElement.encrypt(rawText, params);
                ((TextView) findViewById(R.id.text_encrypted)).setText(res);
            } catch (Exception e) {
                Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

        });

        findViewById(R.id.btn_decrypt).setOnClickListener((view) -> {
            try {
                String encryptedText = ((TextView) findViewById(R.id.text_encrypted)).getText().toString();
                String res = secureElement.decrypt(encryptedText, params);
                ((TextView) findViewById(R.id.text_decrypted)).setText(res);
            } catch (Exception e) {
                Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
