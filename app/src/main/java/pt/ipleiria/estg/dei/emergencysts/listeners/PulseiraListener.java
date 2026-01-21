package pt.ipleiria.estg.dei.emergencysts.listeners;

import java.util.ArrayList;

import pt.ipleiria.estg.dei.emergencysts.modelo.Pulseira;

public interface PulseiraListener {
    void onPulseiraClick(Pulseira pulseira);
    void onPulseirasLoaded(ArrayList<Pulseira> pulseiras);
}
