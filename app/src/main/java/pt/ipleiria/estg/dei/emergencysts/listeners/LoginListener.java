package pt.ipleiria.estg.dei.emergencysts.listeners;

public interface LoginListener {
    // Adicionámos o parâmetro 'String role' aqui:
    void onValidateLogin(String token, String username, String role);

    void onLoginError(String error);
}