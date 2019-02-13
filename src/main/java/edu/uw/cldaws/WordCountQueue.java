package edu.uw.cldaws;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class WordCountQueue {
    private Region REGION = Region.US_WEST_2;
    private String QUEUE = "homework-B3";
    private SqsClient sqsClient;
    private String queueUrl;
    
    public WordCountQueue() {
        sqsClient = SqsClient.builder().region(REGION).build();
        queueUrl = getQueueUrl();
    }
    
    private String getQueueUrl() {
        GetQueueUrlResponse getQueueUrlResponse =
                sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(QUEUE).build());
        return getQueueUrlResponse.queueUrl();
    }
    
    public boolean queueRequest(String url) {
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(WordCountMessage.buildSqsMessageBody(url))
                .delaySeconds(10)
                .build());
        return true;
    }
    
    public List<WordCountMessage> getMessage() {
        List<WordCountMessage> mlist = new ArrayList<>();
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                .build();
        for(Message m: sqsClient.receiveMessage(receiveMessageRequest).messages()) {
            mlist.add(new WordCountMessage(m));
        }
        return mlist;
    }

    public void removeMessage(WordCountMessage m) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(m.getReceipt())
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);
    }

}
