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
@Table(name = "refeicoes")
public class Refeicao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "plano_id")
  private PlanoNutricional plano;

  @Column(nullable = false, length = 50)
  private String identificador;

  @Column(nullable = false, length = 100)
  private String nome;

  @Column(name = "horario_sugerido")
  private LocalTime horarioSugerido;

  @Column(nullable = false)
  private Integer ordem;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public PlanoNutricional getPlano() {
    return plano;
  }

  public void setPlano(PlanoNutricional plano) {
    this.plano = plano;
  }

  public String getIdentificador() {
    return identificador;
  }

  public void setIdentificador(String identificador) {
    this.identificador = identificador;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public LocalTime getHorarioSugerido() {
    return horarioSugerido;
  }

  public void setHorarioSugerido(LocalTime horarioSugerido) {
    this.horarioSugerido = horarioSugerido;
  }

  public Integer getOrdem() {
    return ordem;
  }

  public void setOrdem(Integer ordem) {
    this.ordem = ordem;
  }
}
