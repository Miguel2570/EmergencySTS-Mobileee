package pt.ipleiria.estg.dei.emergencysts.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class ConfigActivity extends AppCompatActivity {

    private EditText editServerIp, editApiPath;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        int savedTheme = SharedPrefManager.getInstance(this).getTheme();
        AppCompatDelegate.setDefaultNightMode(savedTheme);
        SharedPrefManager pref = SharedPrefManager.getInstance(this);

        // Ligação aos campos
        editServerIp = findViewById(R.id.editServerIp);
        editApiPath  = findViewById(R.id.editApiPath);
        btnSave      = findViewById(R.id.btnSave);
        SwitchCompat switchDarkMode = findViewById(R.id.switchDarkMode);

        if (savedTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            switchDarkMode.setChecked(true);
        } else {
            switchDarkMode.setChecked(false);
        }

        // Preencher valores guardados
        editServerIp.setText(pref.getServerBase());
        editApiPath.setText(pref.getApiPath()); // já tem valor por defeito


        // Guardar dados
        btnSave.setOnClickListener(v -> {

            String base = editServerIp.getText().toString().trim();
            String path = editApiPath.getText().toString().trim();

            if (!base.startsWith("http")) {
                editServerIp.setError("O URL deve começar por http:// ou https://");
                return;
            }

            // Guarda valores
            pref.setServerBase(base);
            pref.setApiPath(path);

            Toast.makeText(this, "Configuração guardada!", Toast.LENGTH_SHORT).show();

            //Inicia login
            Intent i = new Intent(ConfigActivity.this, LoginActivity.class);
            startActivity(i);
            finish(); // Fecha a ConfigActivity
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPrefManager prefManager = SharedPrefManager.getInstance(ConfigActivity.this);

            if (isChecked) {
                // Ativar Escuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                prefManager.saveTheme(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                // Ativar Claro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                prefManager.saveTheme(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }
}
