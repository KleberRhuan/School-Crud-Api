/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.infra.specification;

import com.kleberrhuan.houer.common.infra.persistence.specification.AbstractSpecificationFactory;
import com.kleberrhuan.houer.common.infra.persistence.specification.CommonSpecs;
import com.kleberrhuan.houer.school.domain.model.School;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolFilterSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SchoolSpecificationFactory
  extends AbstractSpecificationFactory<School, SchoolFilterSpec> {

  public SchoolSpecificationFactory(ConversionService conversionService) {
    super(School.class, conversionService);
  }

  @Override
  protected Specification<School> extraSpecs(
    SchoolFilterSpec filter,
    Long currentUserId
  ) {
    log.debug(
      "Aplicando filtros extras para School. UserId: {}",
      currentUserId
    );

    Specification<School> spec = null;
    spec = addSpec(spec, CommonSpecs.onlyActive(filter.onlyActive()));
    spec = addSpec(spec, byNameContaining(filter.name()));
    spec =
      addSpec(spec, byMunicipalityNameContaining(filter.municipalityName()));
    spec = addSpec(spec, byStateAbbreviation(filter.stateAbbreviation()));
    spec = addSpec(spec, byOperationalStatus(filter.operationalStatus()));
    spec = addSpec(spec, bySchoolType(filter.schoolType()));
    spec = addSpec(spec, byAdministrativeRegion(filter.administrativeRegion()));

    if (filter.administrativeDependence() != null) {
      spec =
        addSpec(
          spec,
          byAdministrativeDependenceContaining(
            filter.administrativeDependence()
          )
        );
    }
    if (filter.location() != null) {
      spec = addSpec(spec, byLocationContaining(filter.location()));
    }
    if (filter.situation() != null) {
      spec = addSpec(spec, bySituationContaining(filter.situation()));
    }

    return spec;
  }

  private Specification<School> addSpec(
    Specification<School> existing,
    Specification<School> toAdd
  ) {
    if (toAdd == null) {
      return existing;
    }

    return existing == null ? toAdd : existing.and(toAdd);
  }

  private Specification<School> byNameContaining(String name) {
    return name == null || name.trim().isEmpty()
      ? null
      : CommonSpecs.likeUnaccent(r -> r.get("nomeEsc"), name.trim());
  }

  private Specification<School> byMunicipalityNameContaining(
    String municipality
  ) {
    return municipality == null || municipality.trim().isEmpty()
      ? null
      : CommonSpecs.likeUnaccent(r -> r.get("mun"), municipality.trim());
  }

  private Specification<School> byStateAbbreviation(String stateAbbreviation) {
    return stateAbbreviation == null || stateAbbreviation.trim().isEmpty()
      ? null
      : (r, q, cb) ->
        cb.equal(cb.upper(r.get("de")), stateAbbreviation.trim().toUpperCase());
  }

  private Specification<School> byOperationalStatus(Integer operationalStatus) {
    return operationalStatus == null
      ? null
      : (r, q, cb) -> cb.equal(r.get("codsit"), operationalStatus.shortValue());
  }

  private Specification<School> bySchoolType(Short schoolType) {
    return schoolType == null
      ? null
      : (r, q, cb) -> cb.equal(r.get("tipoEsc"), schoolType);
  }

  private Specification<School> byAdministrativeRegion(
    String administrativeRegion
  ) {
    return administrativeRegion == null || administrativeRegion.trim().isEmpty()
      ? null
      : CommonSpecs.likeUnaccent(
        r -> r.get("nomeDep"),
        administrativeRegion.trim()
      );
  }

  private Specification<School> byAdministrativeDependenceContaining(
    String dependence
  ) {
    return (root, query, cb) ->
      cb.like(
        cb.lower(root.get("nomeDep")),
        "%" + dependence.toLowerCase() + "%"
      );
  }

  private Specification<School> byLocationContaining(String location) {
    return (root, query, cb) ->
      cb.like(cb.lower(root.get("mun")), "%" + location.toLowerCase() + "%");
  }

  private Specification<School> bySituationContaining(String situation) {
    return (root, query, cb) -> {
      try {
        Short situationCode = Short.parseShort(situation);
        return cb.equal(root.get("codsit"), situationCode);
      } catch (NumberFormatException e) {
        // Se não for numérico, tenta busca textual
        return cb.like(
          cb.function("CAST", String.class, root.get("codsit")),
          "%" + situation + "%"
        );
      }
    };
  }

  public Specification<School> byFilter(SchoolFilterSpec filter) {
    return extraSpecs(filter, null);
  }
}
