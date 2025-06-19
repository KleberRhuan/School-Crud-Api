/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.domain.model;

import com.kleberrhuan.houer.common.infra.persistence.Auditable;
import jakarta.persistence.*;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.proxy.HibernateProxy;

/** Entidade que representa uma escola com seus dados básicos. */
@Entity
@Table(name = "school", schema = "school")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@NamedEntityGraph(
  name = "School.withMetrics",
  attributeNodes = @NamedAttributeNode("schoolMetrics")
)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class School extends Auditable<Long> {

  @Id
  @Column(name = "code")
  private Long code;

  @Column(name = "nome_esc")
  private String nomeEsc;

  @Column(name = "nome_dep")
  private String nomeDep;

  @Column(name = "de")
  private String de;

  @Column(name = "mun")
  private String mun;

  @Column(name = "distr")
  private String distr;

  @Column(name = "tipo_esc")
  private Short tipoEsc;

  @Column(name = "tipo_esc_desc")
  private String tipoEscDesc;

  @Column(name = "codsit")
  private Short codsit;

  @Column(name = "codesc")
  private Long codesc;

  @OneToOne(
    mappedBy = "school",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private SchoolMetrics schoolMetrics;

  public void setSchoolMetrics(Map<String, Long> metrics) {
    if (this.schoolMetrics == null) {
      this.schoolMetrics = new SchoolMetrics();
      this.schoolMetrics.setSchool(this);
    }
    this.schoolMetrics.updateMetrics(metrics);
  }

  public String getName() {
    return this.nomeEsc;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy
      ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
      : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy
      ? ((HibernateProxy) this).getHibernateLazyInitializer()
        .getPersistentClass()
      : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    School school = (School) o;
    return getCode() != null && Objects.equals(getCode(), school.getCode());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
      ? ((HibernateProxy) this).getHibernateLazyInitializer()
        .getPersistentClass()
        .hashCode()
      : getClass().hashCode();
  }
}
