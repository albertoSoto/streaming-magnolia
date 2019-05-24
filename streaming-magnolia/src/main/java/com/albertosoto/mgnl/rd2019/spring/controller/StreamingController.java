package com.albertosoto.mgnl.rd2019.spring.controller;


import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.objectfactory.Components;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * com.albertosoto.mgnl.rd2019.spring.controller
 * Class StreamingController
 * 09/05/2019
 *
 * <p>
 * Adds progressive download support to magnolia via Spring Framework 5
 * <p>
 *
 * <p>
 * Usual Streaming video, as opposed to progressive download, does not need to be downloaded before it plays.
 * It almost plays immediately(5-8sec.) and you can jump further up in the time line, which is impossible on progressive downloads.
 * <p>
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Controller
public class StreamingController extends AbstractStreamingController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final AssetProviderRegistry assetProviderRegistry;

    @Autowired
    public StreamingController() {
        this.assetProviderRegistry = Components.getComponent(AssetProviderRegistry.class);
    }

    @Override
    protected AssetProviderRegistry getAssetProvider() {
        return this.assetProviderRegistry;
    }


    private ResponseEntity<Resource> getValidResponse(Resource resource, String mediaTypeTxt) {
        try {
            if (resource != null) {
                ResponseEntity a = new ResponseEntity<Resource>(HttpStatus.OK);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(getValidMediaType(resource, mediaTypeTxt))
                        .body(resource);
            } else {
                //if reach point, is not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new FileUrlResource(StringUtils.EMPTY));
            }
        } catch (Exception e) {
            log.error("videoOutputError", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
        }
    }


    /*
     * Java Spring doc says;
     * Should only be used if no other specific Resource implementation is applicable. In particular,
     * prefer ByteArrayResource or any of the file-based Resource implementations where possible.
     * In contrast to other Resource implementations, this is a descriptor for an already opened resource
     * - therefore returning true from isOpen(). Do not use an InputStreamResource if you need to keep the resource
     * descriptor somewhere, or if you need to read from a stream multiple times.
     * <p>
     * <p>
     * As of this writing, spring-web:5.0.2.RELEASE has a ResourceHttpMessageConverter that has a boolean supportsReadStreaming, which if set, and the response type is InputStreamResource, returns InputStreamResource; otherwise it returns a ByteArrayResource. So clearly, you're not the only one that asked for streaming support.
     * <p>
     * However, there is a problem: RestTemplate closes the response soon after the HttpMessageConverter runs. Thus, even if you asked for InputStreamResource, and got it, it's no good, because the response stream has been closed. I think this is a design flaw that they overlooked; it should've been dependent on the response type. So unfortunately, for reading, you must consume the response fully; you can't pass it around if using RestTemplate.
     * <p>
     * Writing is no problem though. If you want to stream an InputStream, ResourceHttpMessageConverter will do it for you. Under the hood, it uses org.springframework.util.StreamUtils to write 4096 bytes at a time from the InputStream to the OutputStream.
     * <p>
     * Some of the HttpMessageConverter support all media types, so depending on your requirement, you may have to remove the default ones from RestTemplate, and set the ones you need, being mindful of their relative ordering.
     * <p>
     * Last but not the least, implementations of ClientHttpRequestFactory has a boolean bufferRequestBody that you can, and should, set to false if you are uploading a large stream. Otherwise, you know, OutOfMemoryError. As of this writing, SimpleClientHttpRequestFactory (JDK client) and HttpComponentsClientHttpRequestFactory (Apache HTTP client) support this feature, but not OkHttp3ClientHttpRequestFactory. Again, design oversight.
     * <p>
     * <p>
     * <p>
     * Full support for heading and streaming profileService allowing jumps into specific times for any browser
     * Chromium supported
     * <p>
     * /dam/jcr:7ecd4045-45a0-4c81-b2b6-f4c4b0cd24ad/<whatever, this is ignored anyway
     * Translates info.magnolia.dam.core.download.DamDownloadServlet to Spring 5
     * <p>
     * > endless loop
     * <p>
     * https://stackoverflow.com/questions/36379835/getting-inputstream-with-resttemplate
     *

     */


    /**
     * This is the final achievement
     * Allows mini request of 1M, instead of sending to whole file
     *
     * @param fileRQ  dam uri
     * @param headers httpHeader by IoC
     * @return ResourceRegion of file
     */
    @CrossOrigin
    @GetMapping("/streaming")
    public ResponseEntity<ResourceRegion> regionStreaming(@RequestParam String fileRQ
            , @RequestHeader HttpHeaders headers) {
        Resource aux;
        try {
            Asset asset = getAssetBasedOnIdentifier(fileRQ);
            if (asset != null) {
                InputStream in = asset.getContentStream();
                aux = new ByteArrayResource(IOUtils.toByteArray(in));
                ResourceRegion region = getResourceRegion(aux, asset.getFileSize(), headers);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(MediaTypeFactory
                                .getMediaType(asset.getMimeType())
                                .orElse(MediaType.APPLICATION_OCTET_STREAM))
                        .body(region);
            }
        } catch (Exception e) {
            log.error("On regionStreaming", e);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
    }


    /**
     * Basic render without streaming - old school way
     *
     * @param response servletResponse
     * @throws IOException exception for jcr Data
     */
    @RequestMapping(value = "/jcr-full-stream", method = RequestMethod.GET)
    public void getImageAsByteArray(HttpServletResponse response,
                                    @RequestParam(required = false, defaultValue = EXAMPLE_JCR_URI) String jcrPath) throws IOException {
        Asset asset = getAssetBasedOnIdentifier(jcrPath);
        InputStream in = asset.getContentStream();
        response.setContentType(asset.getMimeType());
        IOUtils.copy(in, response.getOutputStream());
    }

    /**
     * Static file base example.
     * Change file uri to try performance
     *
     * @return Resource
     * @throws IOException exception for file not found
     */
    @GetMapping("/partial-file-stream")
    public ResponseEntity<Resource> fileBasedStream(@RequestParam(required = false, defaultValue = EXAMPLE_FILE_URI) String filePath
    ) throws IOException {
        File file = new File(filePath);
        UrlResource video = new UrlResource(file.toURI());
        return getValidResponse(video, null);
    }

}