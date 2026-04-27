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
import java.math.BigDecimal;

@Entity
@Table(name = "opcoes_alimento")
public class OpcaoAlimento {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "categoria_id")
  private CategoriaRefeicao categoria;

  @Column(nullable = false, length = 200)
  private String alimento;

  @Column(length = 100)
  private String porcao;

  @Column(name = "peso_valor", precision = 8, scale = 2)
  private BigDecimal pesoValor;

  @Column(length = 20)
  private String unidade;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public CategoriaRefeicao getCategoria() {
    return categoria;
  }

  public void setCategoria(CategoriaRefeicao categoria) {
    this.categoria = categoria;
  }

  public String getAlimento() {
    return alimento;
  }

  public void setAlimento(String alimento) {
    this.alimento = alimento;
  }

  public String getPorcao() {
    return porcao;
  }

  public void setPorcao(String porcao) {
    this.porcao = porcao;
  }

  public BigDecimal getPesoValor() {
    return pesoValor;
  }

  public void setPesoValor(BigDecimal pesoValor) {
    this.pesoValor = pesoValor;
  }

  public String getUnidade() {
    return unidade;
  }

  public void setUnidade(String unidade) {
    this.unidade = unidade;
  }
}
