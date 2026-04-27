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

@Entity
@Table(
    name = "usuario_conquistas",
    uniqueConstraints = @UniqueConstraint(name = "uq_usuario_conquista", columnNames = {"usuario_id", "conquista_id"}))
public class UsuarioConquista {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "conquista_id")
  private Conquista conquista;

  @Column(name = "desbloqueada_em", insertable = false, updatable = false)
  private Instant desbloqueadaEm;

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

  public Conquista getConquista() {
    return conquista;
  }

  public void setConquista(Conquista conquista) {
    this.conquista = conquista;
  }

  public Instant getDesbloqueadaEm() {
    return desbloqueadaEm;
  }
}
