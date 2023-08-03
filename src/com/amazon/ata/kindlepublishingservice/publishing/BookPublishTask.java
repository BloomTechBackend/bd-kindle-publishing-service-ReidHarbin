package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;

import javax.inject.Inject;

public class BookPublishTask implements Runnable {

    private BookPublishRequestManager bookPublishRequestManager;
    private PublishingStatusDao publishingStatusDao;
    private CatalogDao catalogDao;

    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager,
                           PublishingStatusDao publishingStatusDao,
                           CatalogDao catalogDao) {
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
    }

    public void publishBookRequestToCatalog() {
        BookPublishRequest bookPublishRequest = bookPublishRequestManager.getBookPublishRequestToProcess();
        if (bookPublishRequest == null) {
            return;
        }

        String publishingRecordId = bookPublishRequest.getPublishingRecordId();
        String bookId = bookPublishRequest.getBookId();

        publishingStatusDao.setPublishingStatus(publishingRecordId, PublishingRecordStatus.IN_PROGRESS, bookId);

        try {
            CatalogItemVersion catalogItemVersion = catalogDao.createOrUpdateBook(KindleFormatConverter.format(bookPublishRequest));
            publishingStatusDao.setPublishingStatus(publishingRecordId,
                    PublishingRecordStatus.SUCCESSFUL,
                    catalogItemVersion.getBookId());
        } catch (Exception e) {
            publishingStatusDao.setPublishingStatus(publishingRecordId,
                    PublishingRecordStatus.FAILED,
                    bookId,
                    e.getMessage());
        }
    }

    @Override
    public void run() {
        publishBookRequestToCatalog();
    }
}
