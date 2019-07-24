package ambitious.but.rubbish.exceptions;

public class UserAlreadyExistsException extends Exception {
    String error;

    public UserAlreadyExistsException(String error){
        this.error = error;
    }

    public String toString(){
        return this.error;
    }
}
