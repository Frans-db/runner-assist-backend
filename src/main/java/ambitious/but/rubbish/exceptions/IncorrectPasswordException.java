package ambitious.but.rubbish.exceptions;

public class IncorrectPasswordException extends Exception {
    String error;

    public IncorrectPasswordException(String error){
        this.error = error;
    }

    public String toString(){
        return this.error;
    }
}
