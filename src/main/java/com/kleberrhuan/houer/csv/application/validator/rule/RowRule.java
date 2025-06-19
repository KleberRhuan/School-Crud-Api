/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator.rule;

import com.kleberrhuan.houer.csv.domain.model.RowContext;

public sealed interface RowRule
  permits MandatoryRule, NumericRule, SchoolCodeRule {
  void validate(RowContext context);
}
