package tomii.scrabblehelper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
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
    EditText serverField;
    EditText userNameField;
    EditText passWordField;

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

                serverField = (EditText) findViewById(R.id.server);
                userNameField = (EditText)findViewById(R.id.username);
                passWordField = (EditText)findViewById(R.id.password);

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

        final EditText registerServerField = new EditText(context);
        registerServerField.setHint("server");
        layout.addView(registerServerField);

        final EditText registerUserNameField = new EditText(context);
        registerUserNameField.setHint("username");
        registerUserNameField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        layout.addView(registerUserNameField);

        final EditText registerPassWordField = new EditText(context);
        registerPassWordField.setHint("password");
        registerPassWordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(registerPassWordField);

        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final ScrabbleHelperApp app = (ScrabbleHelperApp) getApplicationContext();
                String server = registerServerField.getText().toString();
                app.setServer(server);
                requestController = new RequestController("dummy", server);
                String username = registerUserNameField.getText().toString();
                String password = registerPassWordField.getText().toString();
                if (server.length() == 0) {
                    showErrorDialog("Server is empty!");
                } else if (username.length() == 0) {
                    showErrorDialog("UserName is empty!");
                } else if (password.length() == 0) {
                    showErrorDialog("Password is empty!");
                } else {
                    if (requestController.register(username, password)) {
                        serverField = (EditText) findViewById(R.id.server);
                        userNameField = (EditText)findViewById(R.id.username);
                        passWordField = (EditText)findViewById(R.id.password);
                        serverField.setText(server);
                        userNameField.setText(username);
                        passWordField.setText(password);
                        showErrorDialog("Success! Now you can log in");
                    } else {
                        showErrorDialog("Error! User already exists, or server could not be reached!");
                    }

                }

            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.setView(layout);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Quitting ScrabbleHelper")
                .setMessage("Are you sure you want to exit ScrabbleHelper?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null);
        builder.show();
    }
}
