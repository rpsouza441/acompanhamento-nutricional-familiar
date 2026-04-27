package com.nutritracker.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.nutritracker.dto.ConquistaUsuarioResponse;
import com.nutritracker.dto.RelatorioDetalheResponse;
import com.nutritracker.dto.RelatorioDiaResponse;
import com.nutritracker.dto.RelatorioRefeicaoResponse;
import com.nutritracker.dto.RelatorioResponse;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.model.RegistroDiario;
import com.nutritracker.repository.AlimentoConsumidoRepository;
import com.nutritracker.repository.RefeicaoRegistradaRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatorioService {
  private final UsuarioRepository usuarioRepository;
  private final AdesaoMetricsService metricsService;
  private final RefeicaoRegistradaRepository refeicaoRegistradaRepository;
  private final AlimentoConsumidoRepository alimentoConsumidoRepository;
  private final ConquistaService conquistaService;

  public RelatorioService(
      UsuarioRepository usuarioRepository,
      AdesaoMetricsService metricsService,
      RefeicaoRegistradaRepository refeicaoRegistradaRepository,
      AlimentoConsumidoRepository alimentoConsumidoRepository,
      ConquistaService conquistaService) {
    this.usuarioRepository = usuarioRepository;
    this.metricsService = metricsService;
    this.refeicaoRegistradaRepository = refeicaoRegistradaRepository;
    this.alimentoConsumidoRepository = alimentoConsumidoRepository;
    this.conquistaService = conquistaService;
  }

  @Transactional(readOnly = true)
  public RelatorioResponse gerar(Long usuarioId, LocalDate inicio, LocalDate fim) {
    if (inicio.isAfter(fim)) {
      throw new BusinessException("Periodo invalido");
    }
    var usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));
    var registros = metricsService.registrosNoPeriodo(usuarioId, inicio, fim);

    List<RelatorioDiaResponse> dias = new ArrayList<>();
    List<RelatorioDetalheResponse> detalhes = new ArrayList<>();
    Map<String, int[]> refeicoesResumo = new LinkedHashMap<>();

    int totalRefeicoes = 0;
    int totalConcluidas = 0;
    int diasMetaAgua = 0;
    String profissional = null;

    for (RegistroDiario registro : registros) {
      if (profissional == null && registro.getPlano() != null) {
        profissional = registro.getPlano().getProfissional();
      }
      var refeicoes = refeicaoRegistradaRepository.findByRegistroDiarioId(registro.getId());
      int concluidas = (int) refeicoes.stream().filter(refeicao -> refeicao.isConcluida()).count();
      totalRefeicoes += refeicoes.size();
      totalConcluidas += concluidas;
      if (registro.getAguaConsumidaMl() >= registro.getPlano().getMetaAguaDiariaMl()) {
        diasMetaAgua++;
      }
      dias.add(
          new RelatorioDiaResponse(
              registro.getDataRegistro(),
              registro.getAguaConsumidaMl(),
              registro.getPlano().getMetaAguaDiariaMl(),
              refeicoes.size(),
              concluidas,
              percentual(concluidas, refeicoes.size())));

      for (var refeicao : refeicoes) {
        int[] resumo = refeicoesResumo.computeIfAbsent(refeicao.getRefeicao().getNome(), key -> new int[2]);
        resumo[0]++;
        if (refeicao.isConcluida()) {
          resumo[1]++;
        }
        String alimentos =
            alimentoConsumidoRepository.findByRefeicaoRegistradaId(refeicao.getId()).stream()
                .map(
                    alimento ->
                        alimento.getOpcao() != null
                            ? alimento.getOpcao().getAlimento()
                            : alimento.getDescricaoManual())
                .filter(texto -> texto != null && !texto.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        detalhes.add(
            new RelatorioDetalheResponse(
                registro.getDataRegistro(),
                refeicao.getRefeicao().getNome(),
                alimentos,
                refeicao.getHorarioRealizado(),
                refeicao.getObservacoes()));
      }
    }

    List<RelatorioRefeicaoResponse> refeicoes =
        refeicoesResumo.entrySet().stream()
            .map(
                entry ->
                    new RelatorioRefeicaoResponse(
                        entry.getKey(),
                        entry.getValue()[0],
                        entry.getValue()[1],
                        percentual(entry.getValue()[1], entry.getValue()[0])))
            .toList();

    return new RelatorioResponse(
        usuarioId,
        usuario.getNome(),
        inicio,
        fim,
        profissional,
        percentual(totalConcluidas, totalRefeicoes),
        metricsService.maiorSequenciaAltaAdesao(registros),
        registros.size(),
        diasMetaAgua,
        dias,
        refeicoes,
        detalhes,
        conquistaService.listarDoUsuario(usuarioId));
  }

  @Transactional(readOnly = true)
  public byte[] gerarPdf(Long usuarioId, LocalDate inicio, LocalDate fim) {
    RelatorioResponse relatorio = gerar(usuarioId, inicio, fim);
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      PdfWriter writer = new PdfWriter(output);
      PdfDocument pdf = new PdfDocument(writer);
      Document document = new Document(pdf);

      document.add(new Paragraph("NutriTracker").setFontSize(18).setBold());
      document.add(
          new Paragraph(
              "Paciente: "
                  + relatorio.usuarioNome()
                  + " | Periodo: "
                  + relatorio.inicio()
                  + " a "
                  + relatorio.fim()));
      document.add(new Paragraph("Profissional: " + valor(relatorio.profissional())));

      document.add(new Paragraph("Resumo Executivo").setBold());
      Table resumo = new Table(UnitValue.createPercentArray(new float[] {2, 1}));
      resumo.setWidth(UnitValue.createPercentValue(100));
      addRow(resumo, "Adesao geral", formatPercent(relatorio.adesaoGeralPercentual()));
      addRow(resumo, "Maior sequencia", relatorio.maiorSequenciaDias() + " dias");
      addRow(resumo, "Dias registrados", String.valueOf(relatorio.diasRegistrados()));
      addRow(resumo, "Dias com meta de agua", String.valueOf(relatorio.diasMetaAgua()));
      document.add(resumo);

      document.add(new Paragraph("Adesao por Refeicao").setBold());
      Table refeicoes = new Table(UnitValue.createPercentArray(new float[] {3, 1, 1, 1}));
      refeicoes.setWidth(UnitValue.createPercentValue(100));
      header(refeicoes, "Refeicao", "Total", "Concluidas", "Adesao");
      for (var refeicao : relatorio.refeicoes()) {
        addRow(
            refeicoes,
            refeicao.nome(),
            String.valueOf(refeicao.total()),
            String.valueOf(refeicao.concluidas()),
            formatPercent(refeicao.adesaoPercentual()));
      }
      document.add(refeicoes);

      if (!relatorio.dias().isEmpty()) {
        document.add(new Paragraph("Grafico de Agua").setBold());
        Image imagemGrafico = new Image(ImageDataFactory.create(graficoAgua(relatorio)));
        imagemGrafico.setAutoScale(true);
        document.add(imagemGrafico);
      }

      document.add(new Paragraph("Tabela Detalhada").setBold());
      Table detalhes = new Table(UnitValue.createPercentArray(new float[] {1, 2, 4, 1, 3}));
      detalhes.setWidth(UnitValue.createPercentValue(100));
      header(detalhes, "Data", "Refeicao", "Alimentos", "Horario", "Observacoes");
      for (var detalhe : relatorio.detalhes()) {
        addRow(
            detalhes,
            String.valueOf(detalhe.data()),
            detalhe.refeicao(),
            valor(detalhe.alimentosConsumidos()),
            detalhe.horario() == null ? "" : detalhe.horario().toString(),
            valor(detalhe.observacoes()));
      }
      document.add(detalhes);

      document.add(new Paragraph("Conquistas").setBold());
      for (ConquistaUsuarioResponse conquista : relatorio.conquistas()) {
        if (conquista.desbloqueada()) {
          document.add(
              new Paragraph(
                  conquista.nome()
                      + " - desbloqueada em "
                      + conquista.desbloqueadaEm()));
        }
      }

      document.add(
          new Paragraph(
                  "Gerado em "
                      + Instant.now()
                      + " | Periodo "
                      + relatorio.inicio()
                      + " a "
                      + relatorio.fim()
                      + " | Profissional "
                      + valor(relatorio.profissional()))
              .setFontSize(9));

      document.close();
      return output.toByteArray();
    } catch (Exception exception) {
      throw new BusinessException("Nao foi possivel gerar o PDF");
    }
  }

  private byte[] graficoAgua(RelatorioResponse relatorio) throws java.io.IOException {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    for (RelatorioDiaResponse dia : relatorio.dias()) {
      String data = dia.data().toString();
      dataset.addValue(dia.aguaConsumidaMl(), "Agua consumida", data);
      dataset.addValue(dia.metaAguaMl(), "Meta", data);
    }
    var chart =
        ChartFactory.createLineChart(
            "Agua diaria",
            "Data",
            "ml",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            false,
            false);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ChartUtils.writeChartAsPNG(output, chart, 900, 360);
    return output.toByteArray();
  }

  private void header(Table table, String... values) {
    for (String value : values) {
      table.addHeaderCell(new Cell().add(new Paragraph(value).setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
    }
  }

  private void addRow(Table table, String... values) {
    for (String value : values) {
      table.addCell(new Cell().add(new Paragraph(valor(value))));
    }
  }

  private double percentual(int parte, int total) {
    return total == 0 ? 0.0 : (parte * 100.0) / total;
  }

  private String formatPercent(double value) {
    return String.format(java.util.Locale.US, "%.1f%%", value);
  }

  private String valor(String value) {
    return value == null ? "" : value;
  }
}
