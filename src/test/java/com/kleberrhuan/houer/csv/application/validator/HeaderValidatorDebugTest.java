/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator;

import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class HeaderValidatorDebugTest {

  @Test
  public void debugHeaderValidation() {
    HeaderValidator validator = new HeaderValidator();

    Set<String> expectedColumns = Arrays
      .stream(CsvSchoolColumn.values())
      .map(Enum::name)
      .collect(Collectors.toUnmodifiableSet());

    System.out.println("Expected columns count: " + expectedColumns.size());
    System.out.println(
      "First 10 columns: " +
      expectedColumns.stream().limit(10).collect(Collectors.toList())
    );

    // Teste com um header pequeno que sabemos que está no enum
    String[] smallHeader = { "NOMEDEP", "DE", "MUN" };

    try {
      String[] result = validator.validate(smallHeader, "test.csv");
      System.out.println("Small header validation succeeded");
    } catch (Exception e) {
      System.out.println("Small header validation failed: " + e.getMessage());
    }

    // Teste com todas as colunas do enum
    String[] allColumnsHeader = expectedColumns.toArray(String[]::new);

    try {
      String[] result = validator.validate(allColumnsHeader, "test.csv");
      System.out.println("All columns validation succeeded");
    } catch (Exception e) {
      System.out.println("All columns validation failed: " + e.getMessage());
    }
  }
}
