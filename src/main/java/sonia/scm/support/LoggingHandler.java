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



package sonia.scm.support;

//~--- non-JDK imports --------------------------------------------------------

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Sebastian Sdorra
 */
public final class LoggingHandler
{

  /** Field description */
  private static final ImmutableSet<String> LOGGERNAMES =
    ImmutableSet.of("sonia", "org.eclipse.jgit", "org.tmatesoft.svn", "svnkit",
      "com.aragost.javahg");

  public SupportHandler getSupportHandler()
  {
    return supportHandler;
  }
  
  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param supportHandler
   */
  public LoggingHandler(SupportHandler supportHandler)
  {
    this.supportHandler = supportHandler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  public synchronized void disable() throws IOException
  {
    for (Entry<Logger, Level> e : levelCache.entrySet())
    {
      Logger logger = e.getKey();

      logger.detachAppender(supportHandler.getOutputStreamAppender());
      logger.setLevel(e.getValue());
    }

    levelCache.clear();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  public synchronized void enable() throws IOException
  {
    LoggerContext lc = supportHandler.getLoggerContext();

    for (String name : LOGGERNAMES)
    {
      Logger logger = lc.getLogger(name);

      levelCache.put(logger, logger.getLevel());
      logger.setLevel(Level.TRACE);
      logger.addAppender(supportHandler.getOutputStreamAppender());
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Map<Logger, Level> levelCache = Maps.newHashMap();

  /** Field description */
  private final SupportHandler supportHandler;
}
