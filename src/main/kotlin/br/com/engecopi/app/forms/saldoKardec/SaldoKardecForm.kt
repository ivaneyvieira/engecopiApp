package br.com.engecopi.app.forms.saldoKardec

import br.com.engecopi.app.model.FiltroSaldoKardec
import br.com.engecopi.saci.beans.SaldoKardec
import br.com.engecopi.saci.saci
import br.com.engecopi.utils.toDate
import com.vaadin.ui.VerticalLayout
import org.vaadin.addons.excelexporter.ExportToExcel
import org.vaadin.addons.excelexporter.configuration.builder.ExportExcelComponentConfigurationBuilder
import org.vaadin.addons.excelexporter.configuration.builder.ExportExcelConfigurationBuilder
import org.vaadin.addons.excelexporter.configuration.builder.ExportExcelSheetConfigurationBuilder
import org.vaadin.addons.excelexporter.model.ExportType
import java.lang.Boolean.TRUE
import java.time.LocalDate
import java.util.*

class SaldoKardecForm : VerticalLayout() {
  val filtroPanel = FiltroPanel()
  val gridPanel = GridPanel()
  val progressPanel = ProgressPanel()

  init {
    isSpacing = true
    filtroPanel.execFiltro = this::execFiltro
    filtroPanel.execExcel = this::execExcel
    progressPanel.isVisible = false
    setSizeFull()
    addComponents(filtroPanel, progressPanel)
    addComponentsAndExpand(gridPanel)
  }

  fun updateDataFiltro() {
    saci.datasProcessamento()?.let { datasProcessamento ->
      filtroPanel.dataInicial.value = datasProcessamento.dataInicial.toDate()
      filtroPanel.dataFinal.value = datasProcessamento.dataFinal.toDate() ?: LocalDate.now()
    }
  }

  private fun execExcel() {
    gridPanel.setItens(saci.pesquisaSaldoKardec())
    val exportToExcelUtility = customizeExportExcelUtility(ExportType.XLSX)
    exportToExcelUtility.export()
  }

  private fun execFiltro(filtro: FiltroSaldoKardec) {
    val dataInicial = filtro.dataInicial ?: LocalDate.now()
    val dataFinal = filtro.dataFinal ?: LocalDate.now()
    saci.saldoKardec(dataInicial, dataFinal, this::monitor)
  }

  private fun monitor(caption: String, parte: Int, total: Int) {
    ui.access {
      progressPanel.update(caption, parte * 1.0f / total)
      if (parte == total) {
        gridPanel.setItens(saci.pesquisaSaldoKardec())
      }
    }
    ui.push()
  }

  private fun customizeExportExcelUtility(exportType: ExportType): ExportToExcel<SaldoKardec> {

    val grid = gridPanel.grid
    val componentConfig = ExportExcelComponentConfigurationBuilder<SaldoKardec>()
            .withGrid(grid)
           .withVisibleProperties(Arrays.asList("codigo", "grade", "loja",
                                                 "mes_ano", "saldoEstoque", "saldoKardec",
                                                 "diferecenca").toTypedArray())
            /*  .withHeaderConfigs(Arrays.asList(ComponentHeaderConfigurationBuilder().withAutoFilter(true)
                                                      .withColumnKeys(Arrays.asList("Código", "Grade", "Loja",
                                                                                    "Mes/Ano", "Estoque", "Kardec",
                                                                                    "Diferença").toTypedArray())
                                                      .build()))*/

            .build()


    /* Configuring Sheets */
    val sheetConfig = ExportExcelSheetConfigurationBuilder<SaldoKardec>()
            .withReportTitle("Saldo Kardec")
            .withSheetName("Saldo Kardec")
            .withComponentConfigs(Arrays.asList(componentConfig))
            .withIsHeaderSectionRequired(TRUE)
            .withDateFormat("dd/MM/yyyy")
            .build()


    /* Configuring Excel */
    val config = ExportExcelConfigurationBuilder<SaldoKardec>()
            .withGeneratedBy("Engecopi")
            .withSheetConfigs(Arrays.asList(sheetConfig))
            .build()

    return ExportToExcel<SaldoKardec>(exportType, config)
  }

}
