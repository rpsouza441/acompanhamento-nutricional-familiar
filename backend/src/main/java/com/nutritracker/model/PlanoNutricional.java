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
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "planos_nutricionais")
public class PlanoNutricional {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @Column(length = 200)
  private String profissional;

  @Column(columnDefinition = "TEXT")
  private String objetivo;

  @Column(name = "meta_agua_diaria_ml", nullable = false)
  private Integer metaAguaDiariaMl = 3000;

  @Column(name = "data_prescricao")
  private LocalDate dataPrescricao;

  @Column(name = "json_original", nullable = false, columnDefinition = "JSON")
  private String jsonOriginal;

  @Column(nullable = false)
  private boolean ativo = true;

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

  public String getProfissional() {
    return profissional;
  }

  public void setProfissional(String profissional) {
    this.profissional = profissional;
  }

  public String getObjetivo() {
    return objetivo;
  }

  public void setObjetivo(String objetivo) {
    this.objetivo = objetivo;
  }

  public Integer getMetaAguaDiariaMl() {
    return metaAguaDiariaMl;
  }

  public void setMetaAguaDiariaMl(Integer metaAguaDiariaMl) {
    this.metaAguaDiariaMl = metaAguaDiariaMl;
  }

  public LocalDate getDataPrescricao() {
    return dataPrescricao;
  }

  public void setDataPrescricao(LocalDate dataPrescricao) {
    this.dataPrescricao = dataPrescricao;
  }

  public String getJsonOriginal() {
    return jsonOriginal;
  }

  public void setJsonOriginal(String jsonOriginal) {
    this.jsonOriginal = jsonOriginal;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public void setAtivo(boolean ativo) {
    this.ativo = ativo;
  }

  public Instant getCriadoEm() {
    return criadoEm;
  }
}
