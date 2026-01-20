package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pt.ipleiria.estg.dei.emergencysts.R;

public class DetalhesPacienteActivity extends AppCompatActivity {

    private TextView tvNome, tvNif, tvSns, tvTelefone, tvGenero, tvMorada, tvDataNasc;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_paciente);

        btnBack    = findViewById(R.id.btnBack);
        tvNome     = findViewById(R.id.tvNome);
        tvNif      = findViewById(R.id.tvNif);
        tvSns      = findViewById(R.id.tvSns);
        tvTelefone = findViewById(R.id.tvTelefone);
        tvGenero   = findViewById(R.id.tvGenero);
        tvMorada   = findViewById(R.id.tvMorada);
        tvDataNasc = findViewById(R.id.tvDataNasc);

        //Botão voltar
        btnBack.setOnClickListener(v -> finish());

        //RECEBER DADOS ENVIADOS
        String nome     = getIntent().getStringExtra("nome");
        String nif      = getIntent().getStringExtra("nif");
        String sns      = getIntent().getStringExtra("sns");
        String telefone = getIntent().getStringExtra("telefone");
        String genero   = getIntent().getStringExtra("genero");
        String morada   = getIntent().getStringExtra("morada");
        String dataNasc = getIntent().getStringExtra("datanascimento");

        //MOSTRAR NO ECRÃ
        tvNome.setText(nome);
        tvNif.setText(nif);
        tvSns.setText(sns);
        tvTelefone.setText(telefone);
        tvGenero.setText(genero);
        tvMorada.setText(morada);
        tvDataNasc.setText(dataNasc);
    }
}
