/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator;

import com.kleberrhuan.houer.csv.application.validator.rule.RowRule;
import com.kleberrhuan.houer.csv.domain.model.RowContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompositeRowValidator {

  private final List<RowRule> rules;

  public void validate(RowContext context) {
    for (RowRule rule : rules) {
      rule.validate(context);
    }
  }
}
