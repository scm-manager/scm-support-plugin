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

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import sonia.scm.security.Role;
import sonia.scm.store.Blob;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("plugins/support")
public class SupportResource
{

  /** Field description */
  private static final String MEDIA_TYPE_ZIP = "application/zip";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param supportManager
   */
  @Inject
  public SupportResource(SupportManager supportManager)
  {
    this.supportManager = supportManager;

    Subject subject = SecurityUtils.getSubject();

    subject.checkRole(Role.ADMIN);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @GET
  @Produces(MEDIA_TYPE_ZIP)
  public Response createSupportFile() throws IOException
  {
    return createBlobResponse(supportManager.collectSupportData());
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @GET
  @Path("logging/disable")
  @Produces(MEDIA_TYPE_ZIP)
  public String disableTraceLogging() throws IOException
  {
    return supportManager.disableTraceLogging().getId();
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @POST
  @Path("logging/enable")
  public Response enableTraceLogging() throws IOException
  {
    supportManager.enableTraceLogging();

    return Response.noContent().build();
  }

  /**
   * Method description
   *
   *
   * @param blob
   *
   * @return
   */
  private Response createBlobResponse(Blob blob)
  {
    //J-
    return Response.ok(
      new BlobStreamingOutput(blob)
    )
    .header(
      "Content-Disposition", 
      "attachment; filename=\"".concat(blob.getId()).concat(".zip\"")
    )
    .build();
    //J+
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/02/16
   * @author         Enter your name here...
   */
  public static class BlobStreamingOutput implements StreamingOutput
  {

    /**
     * Constructs ...
     *
     *
     * @param blob
     */
    public BlobStreamingOutput(Blob blob)
    {
      this.blob = blob;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param output
     *
     * @throws IOException
     * @throws WebApplicationException
     */
    @Override
    public void write(OutputStream output)
      throws IOException, WebApplicationException
    {
      InputStream input = null;

      try
      {
        input = blob.getInputStream();
        ByteStreams.copy(input, output);
      }
      finally
      {
        Closeables.close(input, true);
      }
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Blob blob;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final SupportManager supportManager;
}
