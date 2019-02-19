package edu.uw.cldaws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class WordCountLambda {
    public String myHandler(Object myCount, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + myCount.getClass());
        logger.log("received : " + myCount.toString());
        return myCount.toString();
    }

}
