/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.kleberrhuan.houer.common.interfaces.dto.error.ViolationInfo;
import java.sql.SQLException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PostgresConstraintParser")
class PostgresConstraintParserTest {

  PostgresConstraintParser parser = new PostgresConstraintParser();

  @Nested
  class UniqueViolation {

    @Test
    @DisplayName("parse deve reconhecer violação UNIQUE")
    void shouldParseUnique() {
      SQLException ex = new SQLException(
        "ERROR: duplicate key value violates unique constraint \"uk_user_email\" Detail: Key (email)=(test@example.com) already exists",
        "23505"
      );

      Optional<ViolationInfo> info = parser.parse(ex);
      assertThat(info)
        .contains(ViolationInfo.unique("email", "test@example.com"));
    }
  }

  @Test
  @DisplayName("deve reconhecer violação NOT NULL")
  void notNull() {
    SQLException ex = new SQLException(
      "ERROR: null value in column \"name\" violates not-null constraint",
      "23502"
    );
    assertThat(parser.parse(ex)).contains(ViolationInfo.notNull("name"));
  }

  @Test
  @DisplayName("deve reconhecer violação FOREIGN KEY")
  void foreignKey() {
    SQLException ex = new SQLException(
      "ERROR: insert or update on table \"orders\" violates foreign key constraint \"fk_orders_user_id\"",
      "23503"
    );
    assertThat(parser.parse(ex))
      .contains(ViolationInfo.foreignKey("fk_orders_user_id"));
  }

  @Test
  @DisplayName("deve reconhecer violação CHECK")
  void check() {
    SQLException ex = new SQLException(
      "ERROR: new row for relation \"users\" violates check constraint \"chk_age_positive\"",
      "23514"
    );
    assertThat(parser.parse(ex))
      .contains(ViolationInfo.check("chk_age_positive"));
  }

  @Test
  @DisplayName("deve reconhecer violação EXCLUSION")
  void exclusion() {
    SQLException ex = new SQLException(
      "ERROR: conflicting key value violates exclusion constraint \"exc_booking_overlap\"",
      "23P01"
    );
    assertThat(parser.parse(ex))
      .contains(ViolationInfo.exclusion("exc_booking_overlap"));
  }

  @Test
  @DisplayName("deve retornar vazio para SQLState desconhecido")
  void unknown() {
    SQLException ex = new SQLException("Generic error", "99999");
    assertThat(parser.parse(ex)).isEmpty();
  }
}
