package pt.ipleiria.estg.dei.emergencysts.listeners;

public interface LoginListener {
    void onValidateLogin(String token, String username, String role);

    void onLoginError(String error);
}