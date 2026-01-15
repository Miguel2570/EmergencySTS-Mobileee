package pt.ipleiria.estg.dei.emergencysts.activities.paciente;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.fragments.PerfilFragment;

public class PerfilPacienteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_paciente);

        // Se for a primeira vez que a atividade abre, insere o Fragmento
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PerfilFragment())
                    .commit();
        }
    }
}