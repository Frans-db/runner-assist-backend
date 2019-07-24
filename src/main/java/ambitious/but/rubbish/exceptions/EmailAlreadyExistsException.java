package ambitious.but.rubbish.exceptions;

public class EmailAlreadyExistsException extends Exception {
    String error;

    public EmailAlreadyExistsException(String error){
        this.error = error;
    }

    public String toString(){
        return this.error;
    }
}
