package sonia.scm.support.collector;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.config.ScmConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScmConfigurationCollectorTest {

  public static final String BASE_URL = "http://localhost/";
  private CollectorContext context;
  private ByteArrayOutputStream outputStream;

  @BeforeEach
  void mockContext() throws IOException {
    context = mock(CollectorContext.class);
    outputStream = new ByteArrayOutputStream();
    when(context.createOutputStream("config.xml")).thenReturn(outputStream);
  }

  @Test
  void shouldKeepConfigurationUntouched() throws IOException {
    ScmConfiguration configuration = mockConfiguration();

    new ScmConfigurationCollector(configuration).collect(context);

    Assertions.assertThat(configuration.getBaseUrl()).isEqualTo(BASE_URL);
  }

  @Test
  void shouldWriteConfiguration() throws IOException {
    ScmConfiguration configuration = mockConfiguration();

    new ScmConfigurationCollector(configuration).collect(context);

    Assertions.assertThat(outputStream.toString()).contains(BASE_URL);
  }

  private ScmConfiguration mockConfiguration() {
    ScmConfiguration configuration = new ScmConfiguration();
    configuration.setBaseUrl(BASE_URL);
    return configuration;
  }
}
