import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
public class ShipTableCellRenderer extends DefaultTableCellRenderer {
    @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value != null && value.equals("К")) {
            c.setBackground(Color.BLUE); // Установка синего цвета для ячеек с кораблями
        } else {
            c.setBackground(Color.WHITE); // Другие ячейки остаются белыми
        }
        return c;
    }
}

