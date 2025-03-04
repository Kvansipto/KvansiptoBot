package microservice.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class TableImageService {

  //TODO CompletableFuture ?
  public byte[] drawTableImage(String[] headers, String[][] data) {
    var rowHeight = 30;
    var colWidth = 100;
    var commentColumnWidth = 300;
    var width = (headers.length - 1) * colWidth + commentColumnWidth;
    var height = (data.length + 1) * rowHeight;

    var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    var g2d = image.createGraphics();
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, width, height);
    g2d.setColor(Color.BLACK);

    // Получение FontMetrics для центрирования текста
    var metrics = g2d.getFontMetrics();

    // Рисуем текст в ячейках
    for (int row = 0; row <= data.length; row++) {
      for (int col = 0; col < headers.length; col++) {
        var text = row == 0 ? headers[col] : data[row - 1][col];
        if (text == null) {
          text = "";
        }
        var textWidth = metrics.stringWidth(text);
        var x = col * colWidth + (colWidth - textWidth) / 2;
        if (col == headers.length - 1) {
          x = (headers.length - 1) * colWidth + (commentColumnWidth - textWidth) / 2;
        }
        var y = row * rowHeight + (rowHeight - metrics.getHeight()) / 2 + metrics.getAscent();
        g2d.drawString(text, x, y);
      }
    }

    // Рисуем горизонтальные линии
    for (int row = 0; row <= data.length + 1; row++) {
      g2d.drawLine(0, row * rowHeight, width, row * rowHeight);
    }

    // Рисуем вертикальные линии
    for (int col = 0; col <= headers.length; col++) {
      var x = col == headers.length ? (headers.length - 1) * colWidth + commentColumnWidth : col * colWidth;
      g2d.drawLine(x, 0, x, (data.length + 1) * rowHeight);
    }

    g2d.dispose();

    try (var baos = new ByteArrayOutputStream()) {
      ImageIO.write(image, "PNG", baos);
      return baos.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
