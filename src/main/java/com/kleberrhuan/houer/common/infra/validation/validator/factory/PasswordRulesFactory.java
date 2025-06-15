/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.validation.validator.factory;

import com.kleberrhuan.houer.common.infra.properties.PasswordValidationProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.Rule;

@UtilityClass
public class PasswordRulesFactory {

  public List<Rule> build(PasswordValidationProperties p) {
    List<Rule> rules = new ArrayList<>();
    rules.add(new LengthRule(p.getMinLength(), p.getMaxLength()));

    Map<Predicate<PasswordValidationProperties>, CharacterRule> conditional =
      Map.of(
        PasswordValidationProperties::isRequireUppercase,
        new CharacterRule(
          EnglishCharacterData.UpperCase,
          p.getUppercaseCount()
        ),
        PasswordValidationProperties::isRequireLowercase,
        new CharacterRule(
          EnglishCharacterData.LowerCase,
          p.getLowercaseCount()
        ),
        PasswordValidationProperties::isRequireNumbers,
        new CharacterRule(EnglishCharacterData.Digit, p.getNumbersCount()),
        PasswordValidationProperties::isRequireSpecialChars,
        new CharacterRule(
          EnglishCharacterData.Special,
          p.getSpecialCharsCount()
        )
      );

    conditional.forEach((pred, rule) -> {
      if (pred.test(p)) rules.add(rule);
    });
    return rules;
  }
}
