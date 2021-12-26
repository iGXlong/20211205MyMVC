package mvc;

import javafx.application.Application;
import javafx.stage.Stage;

import java.lang.reflect.Parameter;

public class ParameterTypeException extends RuntimeException{

    public ParameterTypeException(){}
    public ParameterTypeException(String message){
        super(message);
    }


}
