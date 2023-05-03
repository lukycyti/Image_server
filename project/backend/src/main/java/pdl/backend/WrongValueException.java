package pdl.backend;

public class WrongValueException extends Exception{

    public WrongValueException(String errorMessage){
        super(errorMessage);
    }
}
