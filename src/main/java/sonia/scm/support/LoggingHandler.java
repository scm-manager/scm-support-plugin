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
