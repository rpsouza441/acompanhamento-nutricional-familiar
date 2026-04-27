package com.nutritracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "conquistas")
public class Conquista {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String codigo;

  @Column(nullable = false, length = 100)
  private String nome;

  @Column(columnDefinition = "TEXT")
  private String descricao;

  @Column(length = 10)
  private String icone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "ENUM('dias_consecutivos','dias_totais','adesao_percentual','agua_diaria')")
  private TipoConquista tipo;

  @Column(name = "valor_meta", nullable = false)
  private Integer valorMeta;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public String getIcone() {
    return icone;
  }

  public void setIcone(String icone) {
    this.icone = icone;
  }

  public TipoConquista getTipo() {
    return tipo;
  }

  public void setTipo(TipoConquista tipo) {
    this.tipo = tipo;
  }

  public Integer getValorMeta() {
    return valorMeta;
  }

  public void setValorMeta(Integer valorMeta) {
    this.valorMeta = valorMeta;
  }
}
