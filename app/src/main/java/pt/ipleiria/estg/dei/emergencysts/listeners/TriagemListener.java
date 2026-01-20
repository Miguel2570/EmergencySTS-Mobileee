package pt.ipleiria.estg.dei.emergencysts.listeners;

import java.util.ArrayList;
import pt.ipleiria.estg.dei.emergencysts.modelo.Triagem;

public interface TriagemListener {
    // Métodos para interagir com a lista (Clicks nos botões)
    void onTriagemClick(int id);
    void onEliminarClick(int id);

    // NOVOS MÉTODOS: Para receber os dados da API/BD
    void onTriagensLoaded(ArrayList<Triagem> triagens);
    void onTriagemError(String error);
}