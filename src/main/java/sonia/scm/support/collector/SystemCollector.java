/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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
