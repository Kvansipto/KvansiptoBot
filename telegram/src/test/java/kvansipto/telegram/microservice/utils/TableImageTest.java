package kvansipto.telegram.microservice.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TableImageTest {

  private final String[] headers = {"Header1", "Header2", "Header3"};
  private final String[][] data = {
      {"Data1", "Data2", "Data3"},
      {"Data4", "Data5", "Data6"}
  };
  private File imageFile;

  @BeforeEach
  void setUp() {
    // Ensure the resources directory exists
    new File("resources").mkdirs();
  }

  @AfterEach
  void tearDown() {
    if (imageFile != null && imageFile.exists()) {
      imageFile.delete();
    }
  }

  @Test
  void drawTableImage_shouldCreateImageFile() throws IOException {
    // Act
    imageFile = TableImage.drawTableImage(headers, data);

    // Assert
    assertThat(imageFile).isNotNull().exists();

    // Verify image
    BufferedImage image = ImageIO.read(imageFile);
    assertThat(image).isNotNull();
    assertThat(image.getWidth()).isEqualTo(headers.length * 100); // Default colWidth
    assertThat(image.getHeight()).isEqualTo((data.length + 1) * 30); // Default rowHeight
  }
}
