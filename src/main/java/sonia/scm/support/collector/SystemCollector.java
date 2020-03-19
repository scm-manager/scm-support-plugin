/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package sonia.scm.support.collector;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.ServletContainerDetector;
import sonia.scm.Type;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.util.SystemUtil;

//~--- JDK imports ------------------------------------------------------------

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;

import java.text.DecimalFormat;

import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class SystemCollector extends WriterCollector
{

  @Inject
  public SystemCollector(RepositoryManager repositoryManager, Provider<HttpServletRequest> requestProvider,
                         ConfigurationStoreFactory configurationStoreFactory, ConfigurationEntryStoreFactory configurationEntryStoreFactory, DataStoreFactory dataStoreFactory)
  {
    super("system.txt");
    this.repositoryManager = repositoryManager;
    this.requestProvider = requestProvider;
    this.configurationStoreFactory = configurationStoreFactory;
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
    this.dataStoreFactory = dataStoreFactory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param writer
   */
  @Override
  protected void collect(PrintWriter writer)
  {
    SCMContextProvider contextProvider = SCMContext.getContext();

    writer.println("[version]");
    writer.append("Version: ").println(contextProvider.getVersion());
    writer.append("Stage: ").println(contextProvider.getStage());
    writer.append("ConfigurationStoreFactory: ").println(configurationStoreFactory.getClass());
    writer.append("ConfigurationEntryStoreFactory: ").println(configurationEntryStoreFactory.getClass());
    writer.append("DataStoreFactory: ").println(dataStoreFactory.getClass());

    writer.println();
    writer.println("[system]");
    writer.append("Operating System: ").println(SystemUtil.getOS());
    writer.append("Architecture: ").println(SystemUtil.getArch());
    writer.append("Java: ").append(System.getProperty("java.vendor"));
    writer.append("/").println(System.getProperty("java.version"));
    writer.append("Container: ").println(ServletContainerDetector.detect(requestProvider.get()));
    writer.append("Locale: ").println(Locale.getDefault());
    writer.append("TimeZone: ").println(TimeZone.getDefault().getID());
    writer.println();

    Runtime runtime = Runtime.getRuntime();

    writer.println("[runtime]");
    writer.append("Available processors (cores): ");
    writer.println(runtime.availableProcessors());
    writer.append("Free memory: ");
    writer.println(humanReadableSize(runtime.freeMemory()));
    writer.append("Maximum memory: ");
    writer.println(humanReadableSize(runtime.maxMemory()));
    writer.append("Total memory available to JVM: ");
    writer.println(humanReadableSize(runtime.totalMemory()));

    writer.println();
    writer.println("[handlers]");

    for (Type type : repositoryManager.getConfiguredTypes())
    {
      writer.append(type.getName()).append(": ");

      RepositoryHandler handler = repositoryManager.getHandler(type.getName());

      writer.println(handler.getVersionInformation());
    }
  }

  /**
   * Method description
   *
   *
   * @param size
   *
   * @return
   */
  private String humanReadableSize(long size)
  {
    if (size <= 0)
    {
      return "0";
    }

    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

    return new DecimalFormat("#,##0.#").format(size
      / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }

  //~--- fields ---------------------------------------------------------------

  private final RepositoryManager repositoryManager;

  private final Provider<HttpServletRequest> requestProvider;
  private final ConfigurationStoreFactory configurationStoreFactory;
  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;
  private final DataStoreFactory dataStoreFactory;
}
