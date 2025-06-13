/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence.parser;

import com.kleberrhuan.houer.common.interfaces.dto.error.ViolationInfo;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record Handler(Pattern pattern, Function<Matcher, ViolationInfo> builder) {
  Optional<ViolationInfo> tryParse(String message) {
    Matcher m = pattern.matcher(message);
    return m.find() ? Optional.of(builder.apply(m)) : Optional.empty();
  }
}
