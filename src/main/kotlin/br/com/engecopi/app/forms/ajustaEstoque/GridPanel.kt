package br.com.engecopi.app.forms.ajustaEstoque

import br.com.consutec.modelo.Produtos
import com.github.mvysny.karibudsl.v8.column
import com.github.mvysny.karibudsl.v8.grid
import com.github.mvysny.karibudsl.v8.panel
import com.github.mvysny.karibudsl.v8.showColumns
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.CssLayout
import com.vaadin.ui.renderers.NumberRenderer
import com.vaadin.ui.themes.ValoTheme
import java.text.DecimalFormat

class GridPanel(val ajustaEstoqueForm: AjustaEstoqueForm): CssLayout() {
  
  val grid = grid(Produtos::class, null, ListDataProvider(emptyList())) {
    setSizeFull()
    
    column(Produtos::prdno) {
      caption = "Código Prod"
    }
    
    column(Produtos::grade) {
      caption = "Grade"
    }
    
    column(Produtos::descricao) {
      caption = "Descrição"
    }
    
    column(Produtos::fornecedor) {
      caption = "Fornecedor"
    }
    
    column(Produtos::centrodelucro) {
      caption = "Centro de Lucro"
    }
    
    column(Produtos::tipo) {
      caption = "Tipo"
    }
    
    column(Produtos::qtdNfForn) {
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator {"v-align-right"}
      caption = "Qtde Nfs"
    }
    
    column(Produtos::qtdAtacado) {
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator {"v-align-right"}
      caption = "Qtde Atacado"
    }
    
    column(Produtos::qtdConsiderada) {
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator {"v-align-right"}
      caption = "Qtde Considerada"
    }
    
    column(Produtos::custo) {
      setRenderer(NumberRenderer(DecimalFormat("0.00")))
      setStyleGenerator {"v-align-right"}
      caption = "Custo"
    }
    
    column(Produtos::total) {
      setRenderer(NumberRenderer(DecimalFormat("0.00")))
      setStyleGenerator {"v-align-right"}
      caption = "Total"
    }
    
    showColumns(Produtos::prdno,
                Produtos::grade,
                Produtos::descricao,
                Produtos::fornecedor,
                Produtos::centrodelucro,
                Produtos::tipo,
                Produtos::qtdNfForn,
                Produtos::qtdAtacado,
                Produtos::qtdConsiderada,
                Produtos::custo,
                Produtos::total)
  }
  
  fun setProdutos(produtos: List<Produtos>) {
    grid.setItems(produtos)
  }
  
  init {
    caption = "Produtos"
    setSizeFull()
    styleName = ValoTheme.LAYOUT_WELL
    panel {
      setSizeFull()
      content = grid
    }
  }
}