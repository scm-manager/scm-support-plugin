/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */

package sonia.scm.support;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.store.Blob;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Sebastian Sdorra
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Support Plugin", description = "Support plugin provided endpoints")
})
@Path("v2/plugins/support")
public class SupportResource {

  private static final Logger log = LoggerFactory.getLogger(SupportResource.class);
  private static final String MEDIA_TYPE_ZIP = "application/zip";

  @Inject
  public SupportResource(SupportManager supportManager, SupportLinks links) {
    this.supportManager = supportManager;
    this.links = links;
  }

  @GET
  @Path("")
  @Produces(MEDIA_TYPE_ZIP)
  @Operation(summary = "Collect and download data", description = "Collects data and downloads it as zip.", tags = "Support Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MEDIA_TYPE_ZIP
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"support:information\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response createSupportFile() {
    SupportPermissions.checkReadInformation();
    try {
      return createBlobResponse(supportManager.collectSupportData());
    } catch (Exception e) {
      return Response.ok("Could not create information package.\n" + e, MediaType.TEXT_PLAIN_TYPE).build();
    }
  }

  @GET
  @Path("logging/disable")
  @Produces(MEDIA_TYPE_ZIP)
  @Operation(summary = "Disable and download logging data", description = "Disable logging and download of collected support data as zip.", tags = "Support Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MEDIA_TYPE_ZIP
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"support:logging\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response disableTraceLogging() {
    SupportPermissions.checkStartLog();
    log.info("disable trace log");
    try {
      return createBlobResponse(supportManager.disableTraceLogging());
    } catch (Exception e) {
      return Response.ok("Could not create trace log package.\n" + e, MediaType.TEXT_PLAIN_TYPE).build();
    }
  }

  @POST
  @Path("logging/enable")
  @Operation(summary = "Activate logging", description = "Activates the logging for collecting support data.", tags = "Support Plugin")
  @ApiResponse(responseCode = "204", description = "no content")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"support:logging\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response enableTraceLogging() throws IOException {
    SupportPermissions.checkStartLog();
    log.info("enable trace log");
    supportManager.enableTraceLogging();
    return Response.noContent().build();
  }

  @GET
  @Path("logging")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get logging status", description = "Gets the logging status for collecting support data.", tags = "Support Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = LoggingStateDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"support:logging\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response loggingState() {
    SupportPermissions.checkStartLog();
    Links.Builder links = Links.linkingTo()
      .self(this.links.createLogStatusLink());
    if (!supportManager.isProcessingLog()) {
      if (supportManager.isTraceLoggingEnabled()) {
        links.single(Link.link("stopLog", this.links.createStopLogLink()));
      } else {
        links.single(Link.link("startLog", this.links.createStartLogLink()));
      }
    }
    return Response.ok(
      new LoggingStateDto(supportManager.isTraceLoggingEnabled(), supportManager.isProcessingLog(), links.build()))
      .build();
  }

  private Response createBlobResponse(Blob blob) {
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

  public static class BlobStreamingOutput implements StreamingOutput {

    public BlobStreamingOutput(Blob blob) {
      this.blob = blob;
    }

    //~--- methods ------------------------------------------------------------

    @Override
    public void write(OutputStream output)
      throws IOException, WebApplicationException {
      InputStream input = null;

      try {
        input = blob.getInputStream();
        ByteStreams.copy(input, output);
      } finally {
        Closeables.close(input, true);
      }
    }

    private final Blob blob;
  }

  private final SupportManager supportManager;
  private final SupportLinks links;
}
