package kvansipto.telegram.microservice.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class TableImageServiceTest {

  private final TableImageService tableImageService = new TableImageService();

  private final String[] headers = {"Header1", "Header2", "Header3"};
  private final String[][] data = {
      {"Data1", "Data2", "Data3"},
      {"Data4", "Data5", "Data6"}
  };

  @Test
  void drawTableImage_shouldReturnImageBytes() throws IOException {
    // Act
    byte[] imageBytes = tableImageService.drawTableImage(headers, data);

    // Assert
    assertThat(imageBytes).isNotNull();
    assertThat(imageBytes.length).isGreaterThan(0);

    // Verify image properties
    try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
      BufferedImage image = ImageIO.read(bais);
      assertThat(image).isNotNull();
      assertThat(image.getWidth()).isEqualTo((headers.length - 1) * 100 + 300); // Columns + comment width
      assertThat(image.getHeight()).isEqualTo((data.length + 1) * 30); // Rows Height
    }
  }
}
