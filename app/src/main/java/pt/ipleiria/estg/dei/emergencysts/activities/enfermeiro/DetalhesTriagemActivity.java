package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.mqtt.MqttClientManager;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class DetalhesTriagemActivity extends AppCompatActivity {

    private static final String TAG = "DetalhesTriagem";
    private TextView tvNomeValor, tvDataNascimento, tvSNS, tvTelefoneValor, tvDataTriagem;
    private TextView tvMotivo, tvQueixa, tvDescricao, tvInicio, tvDor, tvAlergias, tvMedicacao;
    private TextView tvPrioridade;
    private View dotPrioridade;

    private LinearLayout layoutBotoes;
    private Button btnEliminar;

    private int triagemId;
    private MqttClientManager mqtt;
    private int pulseiraId = -1;

    // --- RECEIVER DO MQTT ---
    private final BroadcastReceiver mqttReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            String topic = intent.getStringExtra("topic");
            Log.d(TAG, "Mensagem MQTT recebida no tópico: " + topic);

            // Verifica se a atualização é sobre esta triagem ou a pulseira associada
            if (topic != null) {
                if (topic.contains("triagem/atualizada/" + triagemId) ||
                        (pulseiraId != -1 && topic.contains("pulseira/atualizada/" + pulseiraId))) {

                    Log.d(TAG, "Atualizando dados da Triagem via MQTT...");
                    getTriagem(); // Faz novo GET para atualizar a UI
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_triagem);

        triagemId = getIntent().getIntExtra("ID_TRIAGEM", -1);
        if (triagemId == -1) {
            Toast.makeText(this, "Erro: ID inválido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        getTriagem();

        // Inicializa o MQTT e subscreve os tópicos globais de atualização
        mqtt = MqttClientManager.getInstance(this);
        // Usamos o wildcard # para ouvir todas as atualizações e filtrar no onReceive
        mqtt.subscribe("triagem/atualizada/#");
        mqtt.subscribe("pulseira/atualizada/#");
    }

    private void initViews() {
        tvNomeValor = findViewById(R.id.tvNomeValor);
        tvDataNascimento = findViewById(R.id.tvDataNascimento);
        tvSNS = findViewById(R.id.tvSNS);
        tvTelefoneValor = findViewById(R.id.tvTelefoneValor);
        tvDataTriagem = findViewById(R.id.tvDataTriagem);
        tvMotivo = findViewById(R.id.tvMotivo);
        tvQueixa = findViewById(R.id.tvQueixa);
        tvDescricao = findViewById(R.id.tvDescricao);
        tvInicio = findViewById(R.id.tvInicio);
        tvDor = findViewById(R.id.tvDor);
        tvAlergias = findViewById(R.id.tvAlergias);
        tvMedicacao = findViewById(R.id.tvMedicacao);
        tvPrioridade = findViewById(R.id.tvPrioridade);
        dotPrioridade = findViewById(R.id.dotPrioridade);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        layoutBotoes = findViewById(R.id.layoutBotoesAcao);
        btnEliminar = findViewById(R.id.btnEliminar);

        String role = SharedPrefManager.getInstance(this).getKeyRole();
        if (role != null && (role.equalsIgnoreCase("paciente") || role.equalsIgnoreCase("utente"))) {
            if (layoutBotoes != null) layoutBotoes.setVisibility(View.GONE);
        } else {
            if (layoutBotoes != null) layoutBotoes.setVisibility(View.VISIBLE);
            if (btnEliminar != null) btnEliminar.setOnClickListener(v -> confirmarEliminar());
        }
    }

    private void getTriagem() {
        String url = VolleySingleton.getInstance(this)
                .getAPIUrl(VolleySingleton.ENDPOINT_TRIAGEM + "/" + triagemId + "?expand=userprofile,pulseira");

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                this::bindData,
                error -> Log.e(TAG, "Erro ao carregar triagem: " + error.getMessage())
        );
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void bindData(JSONObject json) {
        try {
            JSONObject up = json.optJSONObject("userprofile");
            if (up != null) {
                if(tvNomeValor!=null) tvNomeValor.setText(up.optString("nome", "-"));
                if(tvSNS!=null) tvSNS.setText(up.optString("sns", "-"));
                if(tvDataNascimento!=null) tvDataNascimento.setText(formatData(up.optString("datanascimento", "-")));
                if(tvTelefoneValor!=null) tvTelefoneValor.setText(up.optString("telefone", "-"));
            }

            if(tvDataTriagem!=null) tvDataTriagem.setText(formatData(json.optString("datatriagem", "-")));
            if(tvMotivo!=null) tvMotivo.setText(json.optString("motivoconsulta", "-"));
            if(tvQueixa!=null) tvQueixa.setText(json.optString("queixaprincipal", "-"));
            if(tvDescricao!=null) tvDescricao.setText(json.optString("descricaosintomas", "-"));
            if(tvInicio!=null) tvInicio.setText(formatData(json.optString("iniciosintomas", "-")));
            if(tvDor!=null) tvDor.setText(String.valueOf(json.optInt("intensidadedor", 0)));
            if(tvAlergias!=null) tvAlergias.setText(json.optString("alergias", "-"));
            if(tvMedicacao!=null) tvMedicacao.setText(json.optString("medicacao", "-"));

            JSONObject pulseira = json.optJSONObject("pulseira");
            if (pulseira != null) {
                pulseiraId = pulseira.optInt("id", -1); // Atualiza o ID para o filtro do MQTT
                String prioridade = pulseira.optString("prioridade", "Pendente");
                if(tvPrioridade!=null) tvPrioridade.setText(prioridade);

                int res = R.drawable.circle_gray;
                switch (prioridade.toLowerCase()) {
                    case "vermelho": res = R.drawable.circle_red; break;
                    case "laranja": res = R.drawable.circle_orange; break;
                    case "amarelo": case "amarela": res = R.drawable.circle_yellow; break;
                    case "verde": res = R.drawable.circle_green; break;
                    case "azul": res = R.drawable.circle_blue; break;
                }
                if(dotPrioridade!=null) dotPrioridade.setBackgroundResource(res);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String formatData(String data) {
        if (data == null || data.equals("null") || data.isEmpty()) return "-";
        try {
            String hora = "";
            if (data.contains("T")) {
                String[] parts = data.split("T");
                data = parts[0];
                if(parts.length > 1 && parts[1].length() >= 5) hora = " " + parts[1].substring(0,5);
            }
            if (data.contains("-")) {
                String[] p = data.split("-");
                if (p.length == 3) return p[2] + "/" + p[1] + "/" + p[0] + hora;
            }
        } catch (Exception e) {}
        return data;
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("Apagar permanentemente?")
                .setPositiveButton("Apagar", (d, w) -> eliminarTriagemAPI()) // Alterado para chamar o metodo direto
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTriagemAPI() {
        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_TRIAGEM + "/" + triagemId + "?expand=userprofile,pulseira");

        Map<String, String> params = new HashMap<>();
        params.put("_method", "DELETE");

        VolleySingleton.getInstance(this).apiRequest(
                Request.Method.POST,
                VolleySingleton.ENDPOINT_TRIAGEM + "/" + triagemId + "?expand=userprofile,pulseira",
                params,
                response -> {
                    Toast.makeText(this, "Triagem eliminada!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Erro: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        // Regista o receiver para ouvir mensagens do MQTT que o MqttClientManager envia via Broadcast
        IntentFilter filter = new IntentFilter("MQTT_MESSAGE");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mqttReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mqttReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try { unregisterReceiver(mqttReceiver); } catch (Exception e) {}
    }
}