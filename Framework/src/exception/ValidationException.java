package exception;

public class ValidationException extends Exception {

    Object errorToPass;

    public Object getErrorToPass() {
        return errorToPass;
    }

    public void setErrorToPass(Object errorToPass) {
        this.errorToPass = errorToPass;
    }

    public ValidationException(Object object) {
        this.setErrorToPass(object);
    }

}
