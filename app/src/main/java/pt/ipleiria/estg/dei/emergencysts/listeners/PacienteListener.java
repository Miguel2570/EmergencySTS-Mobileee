package pt.ipleiria.estg.dei.emergencysts.listeners;
import java.util.ArrayList;
import pt.ipleiria.estg.dei.emergencysts.modelo.Paciente;

public interface PacienteListener {
    void onPacientesLoaded(ArrayList<Paciente> pacientes);
    void onPacienteError(String error);
}