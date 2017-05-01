package tomii.scrabblehelper;

import android.content.pm.PackageInstaller;

/**
 * Created by Tomii on 2017.04.27..
 */

public class SessionDTO {
    String token;
    String type;
    String errorMessage;

    public SessionDTO() {

    }

    public SessionDTO(String token, String type, String errorMessage) {
        super();
        this.token = token;
        this.type = type;
        this.errorMessage = errorMessage;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


}
