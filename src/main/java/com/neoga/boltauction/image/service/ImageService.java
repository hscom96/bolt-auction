package com.neoga.boltauction.image.service;

import com.neoga.boltauction.item.domain.Item;
import com.neoga.boltauction.memberstore.store.domain.Store;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    void saveItemImages(Item itemId, MultipartFile... images) throws IOException;
    void updateItemImages(Item item, MultipartFile... images) throws IOException;
    void saveStoreImage(Store store, MultipartFile image) throws IOException;
}
