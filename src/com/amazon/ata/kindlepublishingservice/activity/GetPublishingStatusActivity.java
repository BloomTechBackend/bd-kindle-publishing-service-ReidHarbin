package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;

public class GetPublishingStatusActivity {

    private PublishingStatusDao publishingStatusDao;
    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao publishingStatusDao) {
        this.publishingStatusDao = publishingStatusDao;
    }

    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) {
        String publishingRecordId = publishingStatusRequest.getPublishingRecordId();

        if (publishingRecordId == null || publishingRecordId.isBlank()) {
            throw new ValidationException();
        }

        List<PublishingStatusItem> publishingStatusItems =
                publishingStatusDao.getPublishingStatuses(publishingRecordId);

        List<PublishingStatusRecord> publishingStatusRecords = new ArrayList<>();


        for (PublishingStatusItem item : publishingStatusItems) {
            publishingStatusRecords.add(
                 PublishingStatusRecord.builder()
                    .withStatusMessage(item.getStatusMessage())
                    .withStatus(item.getStatus().toString())
                    .withBookId(item.getBookId()).build());
        }

        return new GetPublishingStatusResponse(publishingStatusRecords);
    }
}
