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
import java.time.LocalTime;

@Entity
@Table(name = "refeicoes_registradas")
public class RefeicaoRegistrada {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "registro_diario_id")
  private RegistroDiario registroDiario;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "refeicao_id")
  private Refeicao refeicao;

  @Column(name = "horario_realizado")
  private LocalTime horarioRealizado;

  @Column(nullable = false)
  private boolean concluida = false;

  @Column(columnDefinition = "TEXT")
  private String observacoes;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public RegistroDiario getRegistroDiario() {
    return registroDiario;
  }

  public void setRegistroDiario(RegistroDiario registroDiario) {
    this.registroDiario = registroDiario;
  }

  public Refeicao getRefeicao() {
    return refeicao;
  }

  public void setRefeicao(Refeicao refeicao) {
    this.refeicao = refeicao;
  }

  public LocalTime getHorarioRealizado() {
    return horarioRealizado;
  }

  public void setHorarioRealizado(LocalTime horarioRealizado) {
    this.horarioRealizado = horarioRealizado;
  }

  public boolean isConcluida() {
    return concluida;
  }

  public void setConcluida(boolean concluida) {
    this.concluida = concluida;
  }

  public String getObservacoes() {
    return observacoes;
  }

  public void setObservacoes(String observacoes) {
    this.observacoes = observacoes;
  }
}
