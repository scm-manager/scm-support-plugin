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


import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.store.Blob;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("v2/plugins/support")
public class SupportResource
{

  private static final String MEDIA_TYPE_ZIP = "application/zip";

  @Inject
  public SupportResource(SupportManager supportManager, SupportLinks links)
  {
    this.supportManager = supportManager;
    this.links = links;
  }

  @GET
  @Path("")
  @Produces(MEDIA_TYPE_ZIP)
  public Response createSupportFile() throws IOException
  {
    SupportPermissions.checkReadInformation();
    return createBlobResponse(supportManager.collectSupportData());
  }

  @GET
  @Path("logging/disable")
  @Produces(MEDIA_TYPE_ZIP)
  public Response disableTraceLogging() throws IOException
  {
    SupportPermissions.checkStartTrace();
    return createBlobResponse(supportManager.disableTraceLogging());
  }

  @POST
  @Path("logging/enable")
  public Response enableTraceLogging() throws IOException
  {
    SupportPermissions.checkStartTrace();
    supportManager.enableTraceLogging();
    return Response.noContent().build();
  }

  @GET
  @Path("logging")
  public Response loggingState()
  {
    return Response.ok(
      new HalRepresentation(
        Links.linkingTo()
//          .self()
//          .single()
          .build()))
      .build();
  }

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

  public static class BlobStreamingOutput implements StreamingOutput
  {

    public BlobStreamingOutput(Blob blob)
    {
      this.blob = blob;
    }

    //~--- methods ------------------------------------------------------------

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

    private final Blob blob;
  }

  private final SupportManager supportManager;
  private final SupportLinks links;
}
