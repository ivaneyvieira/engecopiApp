package br.com.consutec.modelo;

import br.com.engecopi.app.model.Produtos;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProdutosModelo extends AbstractTableModel {
	private List<Produtos> dados;
	private final String[] colunas = new String[]{"Codigo Prod", "Grade", "Descrição", "Fornecedor", "Centro de Lucro", "Tipo", "Qtde Nfs", "Qtde Atacado", "Qtde Considerada", "Custo", "Total"};

	public ProdutosModelo() {
		this.dados = new ArrayList<>();
	}

	public Class getColumnClass(int c) {
		if (c == 0) {
			return Long.class;
		}
		if (c == 1) {
			return String.class;
		}
		if (c == 2) {
			return String.class;
		}
		if (c == 3) {
			return Long.class;
		}
		if (c == 4) {
			return String.class;
		}
		if (c == 5) {
			return Long.class;
		}
		if (c == 6) {
			return BigDecimal.class;
		}
		if (c == 7) {
			return BigDecimal.class;
		}
		if (c == 8) {
			return BigDecimal.class;
		}
		if (c == 9) {
			return BigDecimal.class;
		}
		if (c == 10) {
			return BigDecimal.class;
		}
		return String.class;
	}

	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public void setDados(List<Produtos> lista) {
		this.dados = lista;
	}

	public void addRow(Produtos op) {
		this.dados.add(op);
		fireTableDataChanged();
	}

	public String getColumnName(int num) {
		return this.colunas[num];
	}

	public int getRowCount() {
		return this.dados.size();
	}

	public int getColumnCount() {
		return this.colunas.length;
	}

	public Object getValueAt(int linha, int coluna) {
		DecimalFormat df = new DecimalFormat("###,##0.0000");
		switch (coluna) {
			case 0:
				return this.dados.get(linha).getPrdno();
			case 1:
				return this.dados.get(linha).getGrade();
			case 2:
				return this.dados.get(linha).getDescricao();
			case 3:
				return this.dados.get(linha).getFornecedor();
			case 4:
				return this.dados.get(linha).getCentrodelucro();
			case 5:
				return this.dados.get(linha).getTipo();
			case 6:
				return this.dados.get(linha).getQtdNfForn();
			case 7:
				return this.dados.get(linha).getQtdAtacado();
			case 8:
				return this.dados.get(linha).getQtdConsiderada();
			case 9:
				return this.dados.get(linha).getCusto();
			case 10:
				return this.dados.get(linha).getTotal();
		}
		return null;
	}

	public void setValueAt(Object value, int linha, int coluna) {
		switch (coluna) {
			case 0:
				this.dados.get(linha).setPrdno((String) value);
			case 1:
				this.dados.get(linha).setGrade((String) value);
			case 2:
				this.dados.get(linha).setDescricao((String) value);
			case 3:
				this.dados.get(linha).setFornecedor((Long) value);
			case 4:
				this.dados.get(linha).setCentrodelucro((String) value);
			case 5:
				this.dados.get(linha).setTipo((Long) value);
			case 6:
				this.dados.get(linha).setQtdNfForn((BigDecimal) value);
			case 7:
				this.dados.get(linha).setQtdAtacado((BigDecimal) value);
			case 8:
				this.dados.get(linha).setQtdConsiderada((BigDecimal) value);
			case 9:
				this.dados.get(linha).setCusto((BigDecimal) value);
			case 10:
				this.dados.get(linha).setTotal((BigDecimal) value);
				break;
		}
		fireTableCellUpdated(linha, coluna);
	}
}

