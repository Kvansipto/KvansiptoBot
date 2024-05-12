package io.project.kvansiptobot.service;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class TableImage {

  private TableImage() {
  }

  public static File drawTableImage(String[] headers, String[][] data) {
    int rowHeight = 30;
    int colWidth = 100;
    int width = headers.length * colWidth;
    int height = (data.length + 1) * rowHeight;

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, width, height);
    g2d.setColor(Color.BLACK);

    // Получение FontMetrics для центрирования текста
    FontMetrics metrics = g2d.getFontMetrics();

    // Рисуем текст в ячейках
    for (int row = 0; row <= data.length; row++) {
      for (int col = 0; col < headers.length; col++) {
        String text = row == 0 ? headers[col] : data[row - 1][col];
        int textWidth = metrics.stringWidth(text);
        int x = col * colWidth + (colWidth - textWidth) / 2;
        int y = row * rowHeight + (rowHeight - metrics.getHeight()) / 2 + metrics.getAscent();
        g2d.drawString(text, x, y);
      }
    }

    // Рисуем горизонтальные линии
    for (int row = 0; row <= data.length + 1; row++) {
      g2d.drawLine(0, row * rowHeight, width, row * rowHeight);
    }

    // Рисуем вертикальные линии
    for (int col = 1; col <= headers.length; col++) {
      g2d.drawLine(col * colWidth, 0, col * colWidth, (data.length + 1) * rowHeight);
    }

    g2d.dispose();

    try {
      File file = new File("table.png");
      ImageIO.write(image, "PNG", file);
      System.out.println("Изображение таблицы сохранено.");
      return file;
    } catch (IOException e) {
      System.err.println("Ошибка при сохранении изображения.");
      e.printStackTrace();
    }
    return null;
  }
}
