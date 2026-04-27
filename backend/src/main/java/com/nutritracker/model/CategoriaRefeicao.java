package com.nutritracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias_refeicao")
public class CategoriaRefeicao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "refeicao_id")
  private Refeicao refeicao;

  @Column(nullable = false, length = 100)
  private String nome;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_selecao", nullable = false, columnDefinition = "ENUM('escolha_uma','escolha_multipla','livre')")
  private TipoSelecao tipoSelecao = TipoSelecao.escolha_uma;

  private boolean obrigatorio = true;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Refeicao getRefeicao() {
    return refeicao;
  }

  public void setRefeicao(Refeicao refeicao) {
    this.refeicao = refeicao;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public TipoSelecao getTipoSelecao() {
    return tipoSelecao;
  }

  public void setTipoSelecao(TipoSelecao tipoSelecao) {
    this.tipoSelecao = tipoSelecao;
  }

  public boolean isObrigatorio() {
    return obrigatorio;
  }

  public void setObrigatorio(boolean obrigatorio) {
    this.obrigatorio = obrigatorio;
  }
}
