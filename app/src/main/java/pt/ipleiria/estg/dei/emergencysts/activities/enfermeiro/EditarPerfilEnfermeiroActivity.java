package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.modelo.Enfermeiro;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class EditarPerfilEnfermeiroActivity extends AppCompatActivity {

    private EditText etNome, etEmail, etTelefone, etMorada, etNif, etSns;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_enfermeiro);

        // Inicializar Views
        ImageView btnCancel = findViewById(R.id.btnCancel);
        Button btnSaveBottom = findViewById(R.id.btnSaveBottom);

        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etTelefone = findViewById(R.id.etTelefone);
        etMorada = findViewById(R.id.etMorada);
        etNif = findViewById(R.id.etNif);
        etSns = findViewById(R.id.etSns);
        progressBar = findViewById(R.id.progressBar);

        // Preencher campos com dados atuais
        carregarDadosAtuais();

        // Ações dos botões
        btnCancel.setOnClickListener(v -> finish());

        // Apenas o botão de baixo guarda as alterações
        btnSaveBottom.setOnClickListener(v -> guardarAlteracoes());
    }

    private void carregarDadosAtuais() {
        Enfermeiro e = SharedPrefManager.getInstance(this).getEnfermeiro();

        etNome.setText(e.getNome());
        etEmail.setText(e.getEmail());
        etTelefone.setText(e.getTelefone());
        etMorada.setText(e.getMorada());
        etNif.setText(e.getNif());
        etSns.setText(e.getSns());
    }

    private void guardarAlteracoes() {
        String nome = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nome e Email são obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        int userId = SharedPrefManager.getInstance(this).getEnfermeiro().getId();
        String token = SharedPrefManager.getInstance(this).getKeyAccessToken();

        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_ENFERMEIRO + "/" + userId);

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_LONG).show();
                    atualizarSharedPrefsLocalmente();
                    finish();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    // O erro 405 desaparece agora. Se der outro erro (ex: 400), é problema nos parâmetros.
                    String erro = "Erro ao atualizar: " + error.getMessage();
                    if (error.networkResponse != null) {
                        erro += " (Cód: " + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(this, erro, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // Tenta enviar os dois formatos para garantir compatibilidade com Yii2
                params.put("nome", etNome.getText().toString());
                params.put("email", etEmail.getText().toString());
                params.put("telefone", etTelefone.getText().toString());
                params.put("morada", etMorada.getText().toString());
                params.put("nif", etNif.getText().toString());
                params.put("sns", etSns.getText().toString());

                // Formato Model[campo]
                params.put("Enfermeiro[nome]", etNome.getText().toString());
                params.put("Enfermeiro[email]", etEmail.getText().toString());
                params.put("Enfermeiro[telefone]", etTelefone.getText().toString());
                params.put("Enfermeiro[morada]", etMorada.getText().toString());
                params.put("Enfermeiro[nif]", etNif.getText().toString());
                params.put("Enfermeiro[sns]", etSns.getText().toString());

                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void atualizarSharedPrefsLocalmente() {
        Enfermeiro atual = SharedPrefManager.getInstance(this).getEnfermeiro();

        atual.setNome(etNome.getText().toString());
        atual.setEmail(etEmail.getText().toString());
        atual.setTelefone(etTelefone.getText().toString());
        atual.setMorada(etMorada.getText().toString());
        atual.setNif(etNif.getText().toString());
        atual.setSns(etSns.getText().toString());

        SharedPrefManager.getInstance(this).saveEnfermeiro(atual);
    }
}