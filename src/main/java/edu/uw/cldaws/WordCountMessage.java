package edu.uw.cldaws;

import software.amazon.awssdk.services.sqs.model.Message;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

public class WordCountMessage {
    private String url;
    private String receipt;
    public WordCountMessage(Message m) {
        putUrl(m.body());
        putReceipt(m.receiptHandle());
    }
    public WordCountMessage(SQSMessage m) {
        putUrl(m.getBody());
        putReceipt(m.getReceiptHandle());
    }
    public String getUrl() {
        return url;
    }
    public void putUrl(String url) {
        this.url = url;
    }
    public String getReceipt() {
        return receipt;
    }
    public void putReceipt(String id) {
        receipt = id;
    }
    public static String buildSqsMessageBody(String url) {
        return url;
    }
}
