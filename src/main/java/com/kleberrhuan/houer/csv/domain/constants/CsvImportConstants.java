/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CsvImportConstants {

  public static final class Queues {

    public static final String CSV_IMPORT_QUEUE = "csv.import.queue";
    public static final String CSV_NOTIFICATION_QUEUE =
      "csv.notification.queue";

    private Queues() {}
  }

  public static final class Jobs {

    public static final String ID_PARAMETER_NAME = "jobId";
    public static final String FILENAME_PARAMETER_NAME = "filename";
    public static final String FILE_URI_PARAMETER_NAME = "fileUri";
    public static final String DESCRIPTION_PARAMETER_NAME = "description";
    public static final String USER_ID_PARAMETER_NAME = "userId";
    public static final String TIMESTAMP_PARAMETER_NAME = "timestamp";
  }

  public static final class Exchanges {

    public static final String CSV_IMPORT_EXCHANGE = "csv.import.exchange";
    public static final String CSV_NOTIFICATION_EXCHANGE =
      "csv.notification.exchange";

    private Exchanges() {}
  }
}
