package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.modelo.Pulseira;
import pt.ipleiria.estg.dei.emergencysts.mqtt.MqttClientManager;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.PulseiraJsonParser;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class AtribuirPulseiraActivity extends AppCompatActivity {

    private static final String TAG = "AtribuirPulseira";
    private EditText etNome, etDataNasc, etSNS, etTelefone;
    private EditText etMotivo, etQueixa, etDescricao, etInicio, etDor, etAlergias, etMedicacao;
    private Spinner spinnerPrioridade;
    private Button btnAtribuir;

    private String pulseiraId;
    private MqttClientManager mqtt; // Adicionado para gerir subscrições

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atribuir_pulseira);

        if (VolleySingleton.getInstance(this).isInternetConnection()) {
            carregarDadosTriagem();
        } else {
            Toast.makeText(this, "Sem internet: Não é possível carregar os dados.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        inicializarCampos();

        String[] cores = {"Selecione a Prioridade...", "Vermelho", "Laranja", "Amarelo", "Verde", "Azul"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cores);
        spinnerPrioridade.setAdapter(adapter);

        // Obtenção do ID da pulseira
        int idRecebido = getIntent().getIntExtra("pulseira_id", -1);
        if (idRecebido != -1) {
            pulseiraId = String.valueOf(idRecebido);
        } else {
            pulseiraId = getIntent().getStringExtra("pulseira_id");
        }

        if (pulseiraId == null) {
            Toast.makeText(this, "Erro: ID em falta", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- INICIALIZAÇÃO MQTT ---
        // Agora o MqttClientManager utiliza internamente o user 'emergencysts' e a password definida
        mqtt = MqttClientManager.getInstance(this);
        // Subscreve ao tópico específico desta pulseira para ouvir atualizações externas
        mqtt.subscribe("pulseira/atualizada/" + pulseiraId);

        carregarDadosTriagem();

        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        if (btnVoltar != null) btnVoltar.setOnClickListener(v -> finish());

        btnAtribuir.setOnClickListener(v -> {
            if (!VolleySingleton.getInstance(this).isInternetConnection()) {
                Toast.makeText(this, "Sem internet. Não é possível atribuir a pulseira.", Toast.LENGTH_SHORT).show();
                return;
            }

            String prioridade = spinnerPrioridade.getSelectedItem().toString();
            if (prioridade.equals("Selecione a Prioridade...")) {
                Toast.makeText(this, "Selecione uma cor.", Toast.LENGTH_SHORT).show();
            } else {
                guardarAtribuicao(prioridade);
            }
        });
    }

    private void inicializarCampos() {
        etNome = findViewById(R.id.etNome);
        etDataNasc = findViewById(R.id.etDataNasc);
        etSNS = findViewById(R.id.etSNS);
        etTelefone = findViewById(R.id.etTelefone);
        etMotivo = findViewById(R.id.etMotivo);
        etQueixa = findViewById(R.id.etQueixa);
        etDescricao = findViewById(R.id.etDescricao);
        etInicio = findViewById(R.id.etInicio);
        etDor = findViewById(R.id.etDor);
        etAlergias = findViewById(R.id.etAlergias);
        etMedicacao = findViewById(R.id.etMedicacao);
        spinnerPrioridade = findViewById(R.id.spinnerPrioridade);
        btnAtribuir = findViewById(R.id.btnAtribuir);
    }

    private void carregarDadosTriagem() {
        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_PULSEIRA + "/" + pulseiraId);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Pulseira p = PulseiraJsonParser.parserJsonPulseira(response);
                    if (p != null) {
                        etNome.setText(p.getNomePaciente());
                        etDataNasc.setText(p.getDataNascimento());
                        etSNS.setText(p.getSns());
                        etTelefone.setText(p.getTelefone());
                        etMotivo.setText(p.getMotivo());
                        etQueixa.setText(p.getQueixa());
                        etDescricao.setText(p.getDescricao());
                        etInicio.setText(p.getInicioSintomas());
                        etDor.setText(String.valueOf(p.getDor()));
                        etAlergias.setText(p.getAlergias());
                        etMedicacao.setText(p.getMedicacao());
                    }
                },
                error -> Log.e(TAG, "Erro ao carregar triagem: " + error.getMessage())
        );
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void guardarAtribuicao(String cor) {
        String baseUrl = SharedPrefManager.getInstance(this).getServerUrl();
        String authKey = SharedPrefManager.getInstance(this).getKeyAccessToken();
        String url = baseUrl + "api/pulseira/" + pulseiraId + "?auth_key=" + authKey;

        // Nota: O PHP deve estar configurado para disparar o MqttService::publish() após este PUT
        StringRequest request = new StringRequest(Request.Method.POST, url, // Método POST com _method=PUT para Volley/Yii2
                response -> {
                    Toast.makeText(this, "Prioridade atribuída! O paciente será notificado.", Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> Toast.makeText(this, "Erro ao guardar atribuição.", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("_method", "PUT"); // Emulação de PUT para compatibilidade com a API
                params.put("prioridade", cor);
                params.put("status", "Em espera");

                // Envio dos dados de triagem atualizados para o PHP
                params.put("motivoconsulta", etMotivo.getText().toString());
                params.put("queixaprincipal", etQueixa.getText().toString());
                params.put("descricaosintomas", etDescricao.getText().toString());
                params.put("iniciosintomas", etInicio.getText().toString());
                params.put("intensidadedor", etDor.getText().toString());
                params.put("alergias", etAlergias.getText().toString());
                params.put("medicacao", etMedicacao.getText().toString());
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}