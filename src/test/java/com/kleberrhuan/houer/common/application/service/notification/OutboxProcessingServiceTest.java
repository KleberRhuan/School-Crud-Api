/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import com.kleberrhuan.houer.common.application.port.notification.OutboxProcessor;
import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OutboxProcessingServiceTest {

  @Mock
  private OutboxStore store;

  @Mock
  private OutboxProcessor<OutboxMessage> processor;

  @InjectMocks
  private OutboxProcessingService service;

  @Test
  void test() {
    // Test logic here
  }
}
