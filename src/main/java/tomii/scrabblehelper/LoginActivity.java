package tomii.scrabblehelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private RequestController requestController = new RequestController("dummy");
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final ScrabbleHelperApp app = (ScrabbleHelperApp) getApplicationContext();
        super.onCreate(savedInstanceState);
        token = ((ScrabbleHelperApp) LoginActivity.this.getApplication()).getToken();
        setContentView(R.layout.activity_login);
        Button button = (Button) findViewById(R.id.loginButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    EditText userNameField = (EditText)findViewById(R.id.username);
                    EditText passWordField = (EditText)findViewById(R.id.password);

                    if (userNameField.getText().length() == 0) {
                        showErrorDialog("UserName is empty!");
                    } else if (passWordField.getText().length() == 0) {
                        showErrorDialog("Password is empty!");
                    } else {
                        SessionDTO sessionDTO = requestController.login(userNameField.getText().toString(), passWordField.getText().toString());
                        if (sessionDTO.getErrorMessage().equals("")) {
                            token = sessionDTO.getToken();
                            app.setToken(token);
                            requestController = new RequestController(token);
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
}
