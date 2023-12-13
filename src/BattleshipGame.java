import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
public class BattleshipGame{
    private JFrame frame;
    private JTable playerTable;
    private JTable computerTable;
    private DefaultTableModel playerModel;
    private DefaultTableModel computerModel;
    public BattleshipGame() {
        initialize();
    }
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 2));
        playerModel = new DefaultTableModel(10, 10);
        playerTable = new JTable(playerModel);
        placePlayerShip(0, 0); // Расстановка кораблей игрока
        allowPlayerToPlaceShips(); // Разрешение игроку расставлять корабли
        frame.add(new JScrollPane(playerTable));
        computerModel = new DefaultTableModel(10, 10);
        computerTable = new JTable(computerModel);
        placeShips(computerModel);// Расстановка кораблей компьютера
        playerTable.setDefaultRenderer(Object.class, new ShipTableCellRenderer());
        computerTable.setDefaultRenderer(Object.class, new ShipTableCellRenderer());
        computerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = computerTable.rowAtPoint(e.getPoint());
                int col = computerTable.columnAtPoint(e.getPoint());
                // Логика игры для выбора ячейки
                computerTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int row = computerTable.rowAtPoint(e.getPoint());
                        int col = computerTable.columnAtPoint(e.getPoint());
                        attackEnemy(row, col, computerModel, computerTable);
                        // Дополнительная логика после атаки
                    }
                });
            }

        });
        frame.add(new JScrollPane(computerTable));
        frame.setVisible(true);
    }
    private void placeShips(DefaultTableModel model) {
        clearField(model);
        int[] shipSizes = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                placed = placeShipRandomly(model, size);
            }
        }
        if (model == playerModel) {
            playerTable.repaint();
        } else if (model == computerModel) {
            computerTable.repaint();
        }
    }
        private boolean placeShipRandomly(DefaultTableModel model, int size) {
        Random rand = new Random();
        int row = rand.nextInt(10);
        int col = rand.nextInt(10);
        boolean horizontal = rand.nextBoolean();
        // Проверка, можно ли поставить корабль
        for (int i = -1; i <= size; i++) {
            for (int j = -1; j <= 1; j++) {
                int r = row + (horizontal ? j : i);
                int c = col + (horizontal ? i : j);
                if (r < 0 || r >= 10 || c < 0 || c >= 10 || model.getValueAt(r, c) != null) {
                    return false; // Не хватает места, уже занято или близко к другому кораблю
                }
            }
        }
        // Расстановка корабля
        for (int i = 0; i < size; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);
            model.setValueAt("К", r, c); // "К" означает корабль
        }
        return true;
    }
    private void allowPlayerToPlaceShips() {
        playerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = playerTable.rowAtPoint(e.getPoint());
                int col = playerTable.columnAtPoint(e.getPoint());
                placePlayerShip(row, col);
            }
        });
    }
    private void placePlayerShip(int row, int col) {
        // Запрашиваем размер корабля и ориентацию
        Integer[] shipSizes = {1, 2, 3, 4};
        int shipSize = JOptionPane.showOptionDialog(null, "Выберите размер корабля",
                "Размер корабля", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, shipSizes, shipSizes[0]);

        Object[] options = {"Горизонтально", "Вертикально"};
        int orientation = JOptionPane.showOptionDialog(null, "Выберите ориентацию корабля",
                "Ориентация", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (shipSize >= 0 && orientation >= 0) {
            // Расстановка корабля в зависимости от выбранной ориентации
            boolean horizontal = orientation == 0;
            for (int i = 0; i <= shipSize; i++) {
                int r = row + (horizontal ? 0 : i);
                int c = col + (horizontal ? i : 0);
                if (r < 10 && c < 10) {
                    playerModel.setValueAt("К", r, c); // "К" означает корабль
                }
            }
        }
        playerTable.repaint();
    }
    private boolean isPlayerTurn = true;// Переменная для отслеживания хода
    private Point lastHit = null; // Последнее успешное попадание
    private void computerMove() {
        Random rand = new Random();
        int hitCount = 0; // Счетчик успешных попаданий
        int maxHitsInTurn = 3; // Максимальное количество попаданий за один ход

        while (hitCount < maxHitsInTurn) {
            int row, col;
            Object cellValue;

            if (lastHit != null && hasMoreTargets(lastHit)) {
                Point nextTarget = getNextTarget();
                if (nextTarget != null) {
                    row = nextTarget.x;
                    col = nextTarget.y;
                } else {
                    lastHit = null;
                    continue;
                }
            } else {
                row = rand.nextInt(10);
                col = rand.nextInt(10);
            }
            cellValue = playerModel.getValueAt(row, col);
            if (cellValue != null && !cellValue.equals("К") && !cellValue.equals("")) {
                continue; // Пропускаем уже атакованные ячейки
            }

            // Атака
            if ("К".equals(cellValue)) {
                playerModel.setValueAt("X", row, col); // Попадание
                lastHit = new Point(row, col); // Обновление последнего успешного попадания
                hitCount++; // Увеличение счетчика попаданий

                if (isAllShipsSunk(playerModel)) {
                    JOptionPane.showMessageDialog(null, "Компьютер выиграл!");
                    return; // Выход из метода и завершение игры
                }
            } else {
                playerModel.setValueAt("O", row, col); // Промах
                if (lastHit != null && !hasMoreTargets(lastHit)) {
                    lastHit = null;
                }
                isPlayerTurn = true; // Передача хода игроку после промаха
                break; // Прерывание цикла после промаха
            }

            playerTable.repaint();
        }
    }
    private Point getNextTarget() {
        int[] dx = {0, 1, 0, -1}; // Смещения по X
        int[] dy = {-1, 0, 1, 0}; // Смещения по Y
        for (int i = 0; i < 4; i++) {
            int newRow = lastHit.x + dx[i];
            int newCol = lastHit.y + dy[i];
            if (newRow >= 0 && newRow < 10 && newCol >= 0 && newCol < 10) {
                Object cellValue = playerModel.getValueAt(newRow, newCol);
                if (cellValue == null) { // Ячейка не атакована
                    return new Point(newRow, newCol);
                }
            }
        }
        return null; // Если нет доступных целей вокруг
    }
    private boolean hasMoreTargets(Point hit) {
        int[] dx = {0, 1, 0, -1}; // Смещения по X
        int[] dy = {-1, 0, 1, 0}; // Смещения по Y

        for (int i = 0; i < 4; i++) {
            int newRow = hit.x + dx[i];
            int newCol = hit.y + dy[i];

            if (newRow >= 0 && newRow < 10 && newCol >= 0 && newCol < 10) {
                Object cellValue = playerModel.getValueAt(newRow, newCol);
                if (cellValue == null) { // Ячейка не атакована
                    return true;
                }
            }
        }

        return false;// Если нет доступных целей вокруг
    }
    private void attackEnemy(int row, int col, DefaultTableModel enemyModel, JTable enemyTable) {
        if (!isPlayerTurn) {
            return; // Если не ход игрока, выходим из метода
        }

        Object cellValue = enemyModel.getValueAt(row, col);

        if (cellValue != null && cellValue.equals("К")) {
            // Попадание по кораблю
            enemyModel.setValueAt("X", row, col);
            // Проверяем, потоплен ли весь флот противника
            if (isAllShipsSunk(enemyModel)) {
                JOptionPane.showMessageDialog(null, "Вы выиграли!");
                // Здесь код для завершения или перезапуска игры
            }
        } else if (cellValue == null) {
            // Промах
            enemyModel.setValueAt("O", row, col);
            isPlayerTurn = false; // Смена хода
            // Здесь может быть вызов метода для хода компьютера
        }
        if (!isPlayerTurn) {
            computerMove(); // Ход компьютера
        }

        enemyTable.repaint(); // Обновление визуализации таблицы
    }

    private boolean isAllShipsSunk(DefaultTableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < model.getColumnCount(); j++) {
                if ("К".equals(model.getValueAt(i, j))) {
                    return false; // Найден непотопленный корабль
                }
            }
        }
        return true; // Все корабли потоплены
    }
    private void clearField(DefaultTableModel model) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                model.setValueAt(null, i, j);
            }
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                BattleshipGame game = new BattleshipGame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}


