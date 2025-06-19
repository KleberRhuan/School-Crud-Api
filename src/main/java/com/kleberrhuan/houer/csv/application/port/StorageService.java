/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.port;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
  URI store(MultipartFile file, UUID jobId);

  void delete(URI uri);

  boolean exists(URI uri);

  InputStream openInputStream(URI uri);
}
