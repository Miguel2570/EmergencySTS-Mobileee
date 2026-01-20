package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro.EstatisticasActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.comum.HistoricoActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.comum.MostrarPulseirasActivity;
import pt.ipleiria.estg.dei.emergencysts.mqtt.MqttClientManager;

public class EnfermeiroActivity extends AppCompatActivity {

    private CardView cardMostrarPulseira;
    private CardView cardConsultarPaciente;
    private CardView cardHistoricoTriagem;
    private CardView cardPerfil;
    private CardView cardEstatisticas;
    private CardView cardTotalUtilizadores;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enfermeiro);

        cardMostrarPulseira = findViewById(R.id.cardMostrarPulseira);
        cardConsultarPaciente = findViewById(R.id.cardConsultarPaciente);
        cardHistoricoTriagem = findViewById(R.id.cardHistoricoTriagem);
        cardPerfil = findViewById(R.id.cardPerfil);
        cardEstatisticas = findViewById(R.id.cardEstatisticas);
        cardTotalUtilizadores = findViewById(R.id.cardTotalUtilizadores);

        cardMostrarPulseira.setOnClickListener(v ->
                startActivity(new Intent(this, MostrarPulseirasActivity.class))
        );

        cardConsultarPaciente.setOnClickListener(v ->
                startActivity(new Intent(this, ConsultarPacienteActivity.class))
        );

        cardHistoricoTriagem.setOnClickListener(v ->
                startActivity(new Intent(this, HistoricoActivity.class))
        );

        cardPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilEnfermeiroActivity.class))
        );
        cardEstatisticas.setOnClickListener(v ->
                startActivity(new Intent(this, EstatisticasActivity.class))
        );
        cardTotalUtilizadores.setOnClickListener(v ->
                startActivity(new Intent(this, TotalUtilizadoresActivity.class))
        );

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // GARANTIR MQTT ATIVO
    @Override
    protected void onResume() {
        super.onResume();
        MqttClientManager.getInstance(this).connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // NÃO chamamos mqtt.disconnect() aqui
        // Mantemos MQTT ativo para receber notificações
    }
}
