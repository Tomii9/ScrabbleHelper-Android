package tomii.scrabblehelper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class LoginActivity extends AppCompatActivity {

    private RequestController requestController;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final ScrabbleHelperApp app = (ScrabbleHelperApp) getApplicationContext();
        super.onCreate(savedInstanceState);
        token = ((ScrabbleHelperApp) LoginActivity.this.getApplication()).getToken();
        setContentView(R.layout.activity_login);
        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText serverField = (EditText) findViewById(R.id.server);
                EditText userNameField = (EditText)findViewById(R.id.username);
                EditText passWordField = (EditText)findViewById(R.id.password);

                if (serverField.getText().length() == 0) {
                    showErrorDialog("Server is empty!");
                } else if (userNameField.getText().length() == 0) {
                    showErrorDialog("UserName is empty!");
                } else if (passWordField.getText().length() == 0) {
                    showErrorDialog("Password is empty!");
                } else {
                    String server = serverField.getText().toString();
                    app.setServer(server);
                    requestController = new RequestController("dummy", server);
                    SessionDTO sessionDTO = requestController.login(userNameField.getText().toString(), passWordField.getText().toString());
                    if (sessionDTO == null) {
                        showErrorDialog("Server could not be reached!");
                    } else if (sessionDTO.getErrorMessage().equals("")) {
                        token = sessionDTO.getToken();
                        app.setToken(token);
                        app.setUserName(userNameField.getText().toString());
                        if (sessionDTO.getType().equals("admin")) {
                            app.setAdmin(true);
                        } else {
                            app.setAdmin(false);
                        }
                        requestController = new RequestController(token, server);
                        requestController.refreshCache();
                        requestController.endGame();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        showErrorDialog(sessionDTO.getErrorMessage());
                    }

                }
            }
        });
        Button registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });
    }

    private void showErrorDialog(String errorMessage) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle(errorMessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void showRegisterDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        Context context = LoginActivity.this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText userNameField = new EditText(context);
        userNameField.setHint("username");
        layout.addView(userNameField);

        final EditText passWordField = new EditText(context);
        passWordField.setHint("password");
        layout.addView(passWordField);

        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String username = userNameField.getText().toString();
                String password = passWordField.getText().toString();

                if (username.length() == 0) {
                    showErrorDialog("UserName is empty!");
                } else if (password.length() == 0) {
                    showErrorDialog("Password is empty!");
                } else {
                    if (requestController.register(username, password)) {
                        showErrorDialog("Success! Now you can log in");
                    } else {
                        showErrorDialog("Error! User already exists!");
                    }

                }

            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.setView(layout);
        builder.show();
    }
}
