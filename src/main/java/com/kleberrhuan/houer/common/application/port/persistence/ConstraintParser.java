/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.port.persistence;

import com.kleberrhuan.houer.common.interfaces.dto.error.ViolationInfo;
import java.sql.SQLException;
import java.util.Optional;

@FunctionalInterface
public interface ConstraintParser {
  Optional<ViolationInfo> parse(SQLException ex);
}
