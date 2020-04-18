package exceptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RequestBodyFieldsException extends Exception {

    private final String MISSING_FIELDS_MESSAGE = "Request's json in body missing the following fields: ";
    private String message;

    public RequestBodyFieldsException(String message) {
        this.message = message;
    }

    public RequestBodyFieldsException(String... missingFields) {
        this.message = createMissingFieldsMessage(missingFields);
    }

    public RequestBodyFieldsException(Object requestModel) {
        Method[] methods = requestModel.getClass().getMethods();
        List<String> missingFields = new ArrayList<>();

        for(Method method: methods) {
            if(method.getName().startsWith("get")) {
                try {
                    if(method.invoke(requestModel) == null) {
                       missingFields.add(method.getName().substring(3).toLowerCase());
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        this.message = createMissingFieldsMessage(missingFields.toArray(new String[0]));
    }

    @Override
    public String getMessage() {
        return message;
    }

    private String createMissingFieldsMessage(String... missingFields) {
        StringBuilder sb = new StringBuilder(MISSING_FIELDS_MESSAGE);
        int length = missingFields.length;

        for(int i = 0; i < length; i++) {
            sb.append(missingFields[i]);

            if(i < length - 1) {
                sb.append(", ");
            } else {
                sb.append(".");
            }
        }

        return sb.toString();
    }
}
