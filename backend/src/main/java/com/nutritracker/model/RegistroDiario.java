package com.nutritracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "registros_diarios",
    uniqueConstraints = @UniqueConstraint(name = "uq_usuario_data", columnNames = {"usuario_id", "data_registro"}))
public class RegistroDiario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "plano_id")
  private PlanoNutricional plano;

  @Column(name = "data_registro", nullable = false)
  private LocalDate dataRegistro;

  @Column(name = "agua_consumida_ml", nullable = false)
  private Integer aguaConsumidaMl = 0;

  @Column(name = "observacoes_gerais", columnDefinition = "TEXT")
  private String observacoesGerais;

  @Column(name = "criado_em", insertable = false, updatable = false)
  private Instant criadoEm;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public PlanoNutricional getPlano() {
    return plano;
  }

  public void setPlano(PlanoNutricional plano) {
    this.plano = plano;
  }

  public LocalDate getDataRegistro() {
    return dataRegistro;
  }

  public void setDataRegistro(LocalDate dataRegistro) {
    this.dataRegistro = dataRegistro;
  }

  public Integer getAguaConsumidaMl() {
    return aguaConsumidaMl;
  }

  public void setAguaConsumidaMl(Integer aguaConsumidaMl) {
    this.aguaConsumidaMl = aguaConsumidaMl;
  }

  public String getObservacoesGerais() {
    return observacoesGerais;
  }

  public void setObservacoesGerais(String observacoesGerais) {
    this.observacoesGerais = observacoesGerais;
  }

  public Instant getCriadoEm() {
    return criadoEm;
  }
}
