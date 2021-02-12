package br.com.consutec.modelo;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class RowNumberTable extends JTable
				implements ChangeListener, PropertyChangeListener, TableModelListener {
	private final JTable main;

	public RowNumberTable(JTable table) {
		this.main = table;
		this.main.addPropertyChangeListener(this);
		this.main.getModel().addTableModelListener(this);
		setFocusable(false);
		setAutoCreateColumnsFromModel(false);
		setSelectionModel(this.main.getSelectionModel());
		TableColumn column = new TableColumn();
		column.setHeaderValue(" ");
		addColumn(column);
		column.setCellRenderer(new RowNumberRenderer());
		getColumnModel().getColumn(0).setPreferredWidth(50);
		setPreferredScrollableViewportSize(getPreferredSize());
	}

	public void addNotify() {
		super.addNotify();
		Component c = getParent();
		if (c instanceof JViewport) {
			JViewport viewport = (JViewport) c;
			viewport.addChangeListener(this);
		}
	}

	public int getRowCount() {
		return this.main.getRowCount();
	}

	public int getRowHeight(int row) {
		int rowHeight = this.main.getRowHeight(row);
		if (rowHeight != super.getRowHeight(row)) {
			setRowHeight(row, rowHeight);
		}
		return rowHeight;
	}

	public Object getValueAt(int row, int column) {
		return Integer.toString(row + 1);
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public void setValueAt(Object value, int row, int column) {
	}

	public void stateChanged(ChangeEvent e) {
		JViewport viewport = (JViewport) e.getSource();
		JScrollPane scrollPane = (JScrollPane) viewport.getParent();
		scrollPane.getVerticalScrollBar().setValue((viewport.getViewPosition()).y);
	}

	public void propertyChange(PropertyChangeEvent e) {
		if ("selectionModel".equals(e.getPropertyName())) {
			setSelectionModel(this.main.getSelectionModel());
		}
		if ("rowHeight".equals(e.getPropertyName())) {
			repaint();
		}
		if ("model".equals(e.getPropertyName())) {
			this.main.getModel().addTableModelListener(this);
			revalidate();
		}
	}

	public void tableChanged(TableModelEvent e) {
		revalidate();
	}

	private static class RowNumberRenderer extends DefaultTableCellRenderer {
		public RowNumberRenderer() {
			setHorizontalAlignment(0);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		                                               boolean hasFocus, int row, int column) {
			if (table != null) {
				JTableHeader header = table.getTableHeader();
				if (header != null) {
					setForeground(header.getForeground());
					setBackground(header.getBackground());
					setFont(header.getFont());
				}
			}
			if (isSelected) {
				setFont(getFont().deriveFont(1));
			}
			setText((value == null) ? "" : value.toString());
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			return this;
		}
	}
}


