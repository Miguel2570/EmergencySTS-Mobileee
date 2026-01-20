package pt.ipleiria.estg.dei.emergencysts.listeners;

import java.util.ArrayList;
import pt.ipleiria.estg.dei.emergencysts.modelo.Triagem;

public interface TriagemListener {
    void onTriagemClick(int id);
    void onEliminarClick(int id);

    void onTriagensLoaded(ArrayList<Triagem> triagens);
    void onTriagemError(String error);
}