package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;

public class CalcularMediaSemBotaoActivity extends AppCompatActivity {
    private TextView tvResultadoMedia;
    private ImageView btnVoltar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calcular_media_sem_botao);

        tvResultadoMedia = findViewById(R.id.tvResultadoMedia);
        btnVoltar = findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v -> finish());

        calcularMedia();

    }

    private void calcularMedia() {
        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_PACIENTE);

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONArray data = new JSONObject(response).getJSONArray("data");

                double soma = 0;
                int count = 0;
                int anoAtual = Calendar.getInstance().get(Calendar.YEAR);

                for (int i = 0; i < data.length(); i++){
                    String dataNascimento = data.getJSONObject(i).getString("datanascimento");

                    if (dataNascimento.length()>= 4) {
                        int ano = Integer.parseInt(dataNascimento.substring(0,4));
                        soma = soma + (anoAtual - ano);
                        count++;
                    }
                }
                if (count > 0) {
                    int media = (int) (soma / count);
                    tvResultadoMedia.setText("Idade Média dos Pacientes: " + media + " anos");
                } else {
                    tvResultadoMedia.setText("0 anos");
                }

            } catch (Exception e) {
                tvResultadoMedia.setText("Erro ao calcular a media");
            }
        }, error -> {
            tvResultadoMedia.setText("Erro de rede");
        });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}