package pt.ipleiria.estg.dei.emergencysts.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class ConfigActivity extends AppCompatActivity {

    private EditText editServerIp, editApiPath;
    private Button btnSave;
    private SwitchCompat switchDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Carregar tema e preferências
        SharedPrefManager pref = SharedPrefManager.getInstance(this);
        int savedTheme = pref.getTheme();
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        // Inicializar Views
        editServerIp = findViewById(R.id.editServerIp);
        editApiPath  = findViewById(R.id.editApiPath);
        btnSave      = findViewById(R.id.btnSave);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Configurar estado do Switch
        switchDarkMode.setChecked(savedTheme == AppCompatDelegate.MODE_NIGHT_YES);

        // Preencher campos com valores atuais (ou default)
        editServerIp.setText(pref.getServerBase());
        editApiPath.setText(pref.getApiPath());

        btnSave.setOnClickListener(v -> {
            String base = editServerIp.getText().toString().trim();
            String path = editApiPath.getText().toString().trim();

            if (base.isEmpty()) {
                editServerIp.setError("Por favor, escreva o IP (ex: 10.0.2.2 ou o ip do servidor)");
                return;
            }

            if (!base.startsWith("http://") && !base.startsWith("https://")) {
                base = "http://" + base;
            }

            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }

            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (!path.endsWith("/")) {
                path = path + "/";
            }

            // 5. Gravar
            pref.setServerBase(base);
            pref.setApiPath(path);

            Toast.makeText(this, "Configuração Atualizada!", Toast.LENGTH_SHORT).show();

            // 6. Voltar ao Login (Limpando a stack para forçar recarregamento)
            Intent i = new Intent(ConfigActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        // Lógica do Tema Escuro
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                pref.saveTheme(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                pref.saveTheme(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }
}