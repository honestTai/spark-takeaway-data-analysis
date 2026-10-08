package com.takeaway.analysis.service;

import com.takeaway.analysis.entity.ImportLog;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface DataImportService {
    ImportLog importMerchants(MultipartFile file);
    ImportLog importDishes(MultipartFile file);
    ImportLog importOrders(MultipartFile file);
    List<ImportLog> getAllImportLogs();
    ImportLog getImportLogById(Long logId);
}

