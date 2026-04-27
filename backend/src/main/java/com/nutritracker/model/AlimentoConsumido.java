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

@Entity
@Table(name = "alimentos_consumidos")
public class AlimentoConsumido {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "refeicao_registrada_id")
  private RefeicaoRegistrada refeicaoRegistrada;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opcao_id")
  private OpcaoAlimento opcao;

  @Column(name = "descricao_manual", length = 300)
  private String descricaoManual;

  @Column(name = "quantidade_personalizada", length = 100)
  private String quantidadePersonalizada;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public RefeicaoRegistrada getRefeicaoRegistrada() {
    return refeicaoRegistrada;
  }

  public void setRefeicaoRegistrada(RefeicaoRegistrada refeicaoRegistrada) {
    this.refeicaoRegistrada = refeicaoRegistrada;
  }

  public OpcaoAlimento getOpcao() {
    return opcao;
  }

  public void setOpcao(OpcaoAlimento opcao) {
    this.opcao = opcao;
  }

  public String getDescricaoManual() {
    return descricaoManual;
  }

  public void setDescricaoManual(String descricaoManual) {
    this.descricaoManual = descricaoManual;
  }

  public String getQuantidadePersonalizada() {
    return quantidadePersonalizada;
  }

  public void setQuantidadePersonalizada(String quantidadePersonalizada) {
    this.quantidadePersonalizada = quantidadePersonalizada;
  }
}
