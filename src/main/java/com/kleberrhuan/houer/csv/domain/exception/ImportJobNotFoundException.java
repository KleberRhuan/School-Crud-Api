/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.exception;

import com.kleberrhuan.houer.common.domain.exception.BusinessException;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import java.util.UUID;
import org.springframework.http.HttpStatus;

/** Exceção lançada quando um job de importação CSV não é encontrado. */
public class ImportJobNotFoundException extends BusinessException {

  public ImportJobNotFoundException(UUID jobId) {
    super(
      HttpStatus.NOT_FOUND,
      ApiErrorType.RESOURCE_NOT_FOUND,
      MessageKey.of("error.csv.import.job.not.found"),
      jobId
    );
  }
}
