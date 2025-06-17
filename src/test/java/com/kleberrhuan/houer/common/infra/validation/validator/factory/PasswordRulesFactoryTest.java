/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.validation.validator.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.kleberrhuan.houer.common.infra.properties.PasswordValidationProperties;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.Rule;

@DisplayName("PasswordRulesFactory")
class PasswordRulesFactoryTest {

  @Test
  @DisplayName("build deve respeitar propriedades e incluir regras corretas")
  void shouldBuildRulesAccordingToProps() {
    PasswordValidationProperties props = new PasswordValidationProperties();
    props.setMinLength(10);
    props.setMaxLength(20);
    props.setRequireUppercase(true);
    props.setRequireLowercase(false); // desliga lowercase
    props.setRequireNumbers(true);
    props.setRequireSpecialChars(false);
    props.setUppercaseCount(2);
    props.setNumbersCount(2);

    List<Rule> rules = PasswordRulesFactory.build(props);

    // Deve conter LengthRule com min/max ajustados
    assertThat(rules)
      .anySatisfy(r -> {
        assertThat(r).isInstanceOf(LengthRule.class);
        LengthRule lr = (LengthRule) r;
        assertThat(lr.getMinimumLength()).isEqualTo(10);
        assertThat(lr.getMaximumLength()).isEqualTo(20);
      });

    // Deve conter CharacterRule para Uppercase e Numbers, mas não Lowercase ou
    // Special
    List<CharacterRule> charRules = rules
      .stream()
      .filter(r -> r instanceof CharacterRule)
      .map(r -> (CharacterRule) r)
      .toList();

    assertThat(charRules).hasSize(2);
    assertThat(
      charRules
        .stream()
        .anyMatch(cr ->
          cr
            .getValidCharacters()
            .equals(EnglishCharacterData.UpperCase.getCharacters())
        )
    )
      .isTrue();
    assertThat(
      charRules
        .stream()
        .anyMatch(cr ->
          cr
            .getValidCharacters()
            .equals(EnglishCharacterData.Digit.getCharacters())
        )
    )
      .isTrue();
  }
}
