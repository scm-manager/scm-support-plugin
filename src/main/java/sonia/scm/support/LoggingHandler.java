/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
      "com.aragost.javahg", "com.cloudogu");

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
      e.getKey().setLevel(e.getValue());
    }
    levelCache.clear();
    
    LoggerContext lc = supportHandler.getLoggerContext();
    for (String name : LOGGERNAMES)
    {
      Logger logger = lc.getLogger(name);
      logger.detachAppender(supportHandler.getOutputStreamAppender());
    }
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
      enableTraceLogging(logger);
      logger.addAppender(supportHandler.getOutputStreamAppender());
    }
    
    // set log level for existing loggers
    for ( Logger logger : lc.getLoggerList() )
    {
      if ( logger.getLevel() != null )
      {
        String name = logger.getName();
        boolean enableTrace = false;
        for ( String n : LOGGERNAMES )
        {
          if ( name.startsWith(n) && ! name.equals(n) )
          {
            enableTrace = true;
            break;
          }
        }
        if ( enableTrace )
        {
          enableTraceLogging(logger);
        }
      }
    }
  }
  
  private void enableTraceLogging(Logger logger) throws IOException
  {
    levelCache.put(logger, logger.getLevel());
    logger.setLevel(Level.TRACE);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Map<Logger, Level> levelCache = Maps.newHashMap();

  /** Field description */
  private final SupportHandler supportHandler;
}
