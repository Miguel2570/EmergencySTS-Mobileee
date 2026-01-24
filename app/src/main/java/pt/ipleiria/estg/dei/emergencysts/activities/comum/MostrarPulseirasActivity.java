package pt.ipleiria.estg.dei.emergencysts.activities.comum;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro.AtribuirPulseiraActivity;
import pt.ipleiria.estg.dei.emergencysts.adapters.PulseiraAdapter;
import pt.ipleiria.estg.dei.emergencysts.listeners.PulseiraListener;
import pt.ipleiria.estg.dei.emergencysts.modelo.Pulseira;
import pt.ipleiria.estg.dei.emergencysts.mqtt.MqttClientManager;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;

public class MostrarPulseirasActivity extends AppCompatActivity implements PulseiraListener {

    private ProgressBar progressBar;
    private LinearLayout layoutSemPulseira;
    private boolean isPaciente;
    private TextView tvTitulo, tvSubtitulo;
    private TextView tvTotalPulseiras;

    private ListView listViewPulseiras;
    private PulseiraAdapter adapter;
    private final ArrayList<Pulseira> listaPulseiras = new ArrayList<>();

    private CardView cardPulseira;
    private TextView tvEstadoBadge, tvCodigoPulseira, tvDescricao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_pulseiras);

        isPaciente = getIntent().getBooleanExtra("IS_PACIENTE", false);

        progressBar = findViewById(R.id.progressBar);
        layoutSemPulseira = findViewById(R.id.layoutSemPulseira);
        tvTitulo = findViewById(R.id.tvTitulo);
        tvSubtitulo = findViewById(R.id.tvSubtitulo);
        tvTotalPulseiras = findViewById(R.id.tvTotalPulseiras);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        if (isPaciente) {
            cardPulseira = findViewById(R.id.cardPulseira);
            tvEstadoBadge = findViewById(R.id.tvEstadoBadge);
            tvCodigoPulseira = findViewById(R.id.tvCodigoPulseira);
            tvDescricao = findViewById(R.id.tvDescricao);

            tvTitulo.setText("A minha Pulseira");
            tvSubtitulo.setText("Acompanhe o seu estado");

            if (tvTotalPulseiras != null) {
                tvTotalPulseiras.setVisibility(View.GONE);
            }

            ListView lv = findViewById(R.id.listViewPulseiras);
            if (lv != null) lv.setVisibility(View.GONE);

        } else {
            listViewPulseiras = findViewById(R.id.listViewPulseiras);
            adapter = new PulseiraAdapter(this, listaPulseiras, this);
            listViewPulseiras.setAdapter(adapter);

            tvTitulo.setText("Pulseiras");
            tvSubtitulo.setText("Pulseiras em espera");

            if (tvTotalPulseiras != null) {
                tvTotalPulseiras.setVisibility(View.VISIBLE);
            }

            CardView cv = findViewById(R.id.cardPulseira);
            if (cv != null) cv.setVisibility(View.GONE);
        }

        MqttClientManager.getInstance(this);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();

        VolleySingleton.getInstance(this).setPulseiraListener(this);
        getPulseirasAPI();

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
        try {
            unregisterReceiver(mqttReceiver);
        } catch (Exception ignored) {}
    }

    private final BroadcastReceiver mqttReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            String topic = intent.getStringExtra("topic");
            if (topic == null) return;

            if (topic.startsWith("pulseira/")) {
                getPulseirasAPI();
            }

            if (isPaciente && topic.startsWith("pulseira/atualizada/")) {
                Toast.makeText(ctx, "O estado da sua pulseira foi atualizado!", Toast.LENGTH_LONG).show();
            } else if (!isPaciente && topic.startsWith("pulseira/criada/")) {
                Toast.makeText(ctx, "Nova pulseira recebida!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void getPulseirasAPI() {
        progressBar.setVisibility(View.VISIBLE);
        layoutSemPulseira.setVisibility(View.GONE);
        VolleySingleton.getInstance(this).getPulseirasAtivasAPI(isPaciente);
    }

    @Override
    public void onPulseirasLoaded(ArrayList<Pulseira> pulseiras) {
        progressBar.setVisibility(View.GONE);
        atualizarInterface(pulseiras);
    }

    @Override
    public void onPulseiraClick(Pulseira pulseira) {
        if (!isPaciente) {
            Intent intent = new Intent(this, AtribuirPulseiraActivity.class);
            intent.putExtra("pulseira_id", pulseira.getId());
            startActivity(intent);
        }
    }

    private void atualizarInterface(ArrayList<Pulseira> pulseiras) {
        if (tvTotalPulseiras != null && !isPaciente) {
            int count = (pulseiras != null) ? pulseiras.size() : 0;
            tvTotalPulseiras.setText("Total de Pulseiras: " + count);
        }

        if (pulseiras == null || pulseiras.isEmpty()) {
            layoutSemPulseira.setVisibility(View.VISIBLE);
            if (isPaciente && cardPulseira != null) cardPulseira.setVisibility(View.GONE);
            if (!isPaciente && listViewPulseiras != null) listViewPulseiras.setVisibility(View.GONE);
            return;
        }

        if (isPaciente) {
            Pulseira p = pulseiras.get(0);
            String status = p.getStatus();

            boolean finalizada = status == null ||
                    status.trim().isEmpty() ||
                    status.equalsIgnoreCase("null") ||
                    status.equalsIgnoreCase("Finalizado") ||
                    status.equalsIgnoreCase("Concluída") ||
                    status.equalsIgnoreCase("Atendido");

            if (finalizada) {
                layoutSemPulseira.setVisibility(View.VISIBLE);
                cardPulseira.setVisibility(View.GONE);
                return;
            }

            cardPulseira.setVisibility(View.VISIBLE);
            layoutSemPulseira.setVisibility(View.GONE);

            tvTitulo.setText(
                    p.getNomePaciente() != null && !p.getNomePaciente().isEmpty()
                            ? "Olá, " + p.getNomePaciente()
                            : "A minha Pulseira"
            );

            tvCodigoPulseira.setText("#" + p.getCodigo());

            configurarBadgePrioridade(p.getPrioridade());

        } else {
            listViewPulseiras.setVisibility(View.VISIBLE);
            listaPulseiras.clear();
            listaPulseiras.addAll(pulseiras);
            adapter.notifyDataSetChanged();
        }
    }

    private void configurarBadgePrioridade(String prioridade) {
        if (prioridade != null && !prioridade.equalsIgnoreCase("Pendente")) {
            tvEstadoBadge.setText(prioridade);
            tvDescricao.setText("Triagem concluída. Aguarde chamada.");

            switch (prioridade.toLowerCase()) {
                case "vermelho":
                    tvEstadoBadge.setTextColor(Color.WHITE);
                    tvEstadoBadge.setBackgroundResource(R.drawable.circle_red);
                    break;
                case "laranja":
                    tvEstadoBadge.setTextColor(Color.WHITE);
                    tvEstadoBadge.setBackgroundResource(R.drawable.circle_orange);
                    break;
                case "amarelo":
                    tvEstadoBadge.setTextColor(Color.BLACK);
                    tvEstadoBadge.setBackgroundResource(R.drawable.circle_yellow);
                    break;
                case "verde":
                    tvEstadoBadge.setTextColor(Color.WHITE);
                    tvEstadoBadge.setBackgroundResource(R.drawable.circle_green);
                    break;
                case "azul":
                    tvEstadoBadge.setTextColor(Color.WHITE);
                    tvEstadoBadge.setBackgroundResource(R.drawable.circle_blue);
                    break;
                default:
                    tvEstadoBadge.setBackgroundResource(R.drawable.bg_chip_pendente);
            }
        } else {
            tvEstadoBadge.setText("Pendente");
            tvEstadoBadge.setTextColor(Color.parseColor("#D84315"));
            tvEstadoBadge.setBackgroundResource(R.drawable.bg_chip_pendente);
            tvDescricao.setText("Aguarde na sala de espera pela triagem.");
        }
    }
}