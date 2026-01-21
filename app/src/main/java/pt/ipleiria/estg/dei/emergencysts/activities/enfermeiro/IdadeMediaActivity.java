package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;


public class IdadeMediaActivity extends AppCompatActivity {
    private ImageView btnVoltar;
    private Button btnCalcularMedia;
    private TextView tvResultadoMedia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idade_media);

        btnVoltar = findViewById(R.id.btnVoltar);
        btnCalcularMedia = findViewById(R.id.btnCalcularMedia);
        tvResultadoMedia = findViewById(R.id.tvResultadoMedia);

        btnVoltar.setOnClickListener(v -> finish());
        btnCalcularMedia.setOnClickListener(v -> calcularMedia());
        tvResultadoMedia.setText("--");

        btnCalcularMedia.setOnClickListener(v -> calcularMedia());
    }

    private void calcularMedia() {
        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_PACIENTE);

        StringRequest req = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONArray data = new JSONObject(response).getJSONArray("data");

                double soma = 0;
                int count = 0;
                int anoAtual = Calendar.getInstance().get(Calendar.YEAR);

                for (int i = 0; i < data.length(); i++) {
                    String dataNasc = data.getJSONObject(i).optString("datanascimento");

                    if (dataNasc.length() >= 4) {
                        int ano = Integer.parseInt(dataNasc.substring(0, 4));
                        soma += (anoAtual - ano);
                        count++;
                    }
                }

                int media = 0;
                if (count > 0) {
                    media = (int) (soma / count);
                }
                tvResultadoMedia.setText(String.format("%d Anos", media));

            } catch (Exception e) {
                tvResultadoMedia.setText("Erro");
            }
        }, error -> Toast.makeText(this, "Erro Rede", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
}