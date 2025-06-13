package com.kleberrhuan.houer.common.infra.persistence.parser;

import com.kleberrhuan.houer.common.application.port.persistence.ConstraintParser;
import com.kleberrhuan.houer.common.interfaces.dto.error.ViolationInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
@Qualifier("postgres")
public class PostgresConstraintParser implements ConstraintParser {

    /** Dados de cada tipo de violação */
    private record Handler(Pattern pattern,
                           Function<Matcher, ViolationInfo> builder) {

        Optional<ViolationInfo> tryParse(String message) {
            Matcher m = pattern.matcher(message);
            return m.find() ? Optional.of(builder.apply(m)) : Optional.empty();
        }
    }

    // —————————————  Regex + builders —————————————
    private static final Map<String, Handler> DISPATCH = Map.of(
            "23505", new Handler( // UNIQUE
                    Pattern.compile(
                            "Key \"?\\((?<col>[^)]+)\\)\"?=\\((?<val>[^)]+)\\) already exists"),
                    m -> ViolationInfo.unique(m.group("col"), m.group("val"))
            ),
            "23502", new Handler( // NOT NULL
                    Pattern.compile(
                            "null value in column \"(?<col>[^\"]+)\" violates not-null constraint"),
                    m -> ViolationInfo.notNull(m.group("col"))
            ),
            "23503", new Handler( // FK
                    Pattern.compile(
                            "violates foreign key constraint \"(?<cons>[^\"]+)\""),
                    m -> ViolationInfo.foreignKey(m.group("cons"))
            ),
            "23514", new Handler( // CHECK
                    Pattern.compile(
                            "violates check constraint \"(?<cons>[^\"]+)\""),
                    m -> ViolationInfo.check(m.group("cons"))
            ),
            "23P01", new Handler( // EXCLUSION
                    Pattern.compile(
                            "violates exclusion constraint \"(?<cons>[^\"]+)\""),
                    m -> ViolationInfo.exclusion(m.group("cons"))
            )
    );

    // —————————————  API pública —————————————
    @Override
    public Optional<ViolationInfo> parse(SQLException ex) {
        return Optional.ofNullable(DISPATCH.get(ex.getSQLState()))
                .flatMap(h -> h.tryParse(ex.getMessage()));
    }
}