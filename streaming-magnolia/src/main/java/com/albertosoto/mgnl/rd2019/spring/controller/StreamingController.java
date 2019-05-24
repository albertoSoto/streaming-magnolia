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
 * <p>
 * <p>
 * Adds progressive download support to magnolia via Spring Framework 5
 * <p>
 * <p>
 * <p>
 * <p>
 * Low level explanation: Magnolia DAM Module renders via DamDownloadServlet an Stream from an Asset in JCR
 * <p>
 * The problem is that the stream gets open in a blocking way and sent through the request.
 * In the inner side, the asset has an LazyInputStream, which means that is does not get open until it really is
 * on the stream, which causes that the stream gets open several times in a blocking way, creating a possible
 * bottle neck for users if the server load gets high
 * <p>
 * On the client side, the stream is open all time, and in every point the user picks another time in the html5 video
 * the video starts loading again. That is really painful for the server.
 * <p>
 * This implementation focus on the browser request, reading the bytes that it's requesting httpHeader, and sends only
 * the desired information, which causes a must better performance. On the other hand, the stream gets free faster,
 * wich makes the server faster to other users and it works in a non blocking way.
 * <p>
 * Moreover, the domLoad time drops, which is a better SEO positioning and score finally.
 * <p>
 * As the Asset stream is a LazyInputStream, Spring 5 documentation explains the following at their docs:
 * </p>
 * <p>
 * <p>
 * "LazyInputStream should only be used if no other specific Resource implementation is applicable. In particular,
 * prefer ByteArrayResource or any of the file-based Resource implementations where possible.
 * In contrast to other Resource implementations, this is a descriptor for an already opened resource.
 * Therefore returning true from isOpen(). Do not use an InputStreamResource if you need to keep the resource
 * descriptor somewhere, or if you need to read from a stream multiple times."
 * <p>
 * </p>
 * <p>
 * <p>
 * "Last but not the least, implementations of ClientHttpRequestFactory has a boolean bufferRequestBody that you can,
 * and should, set to false if you are uploading a large stream. Otherwise, you know, OutOfMemoryError.
 * As of this writing, SimpleClientHttpRequestFactory (JDK client) and HttpComponentsClientHttpRequestFactory
 * (Apache HTTP client) support this feature, but not OkHttp3ClientHttpRequestFactory. Again, design oversight."
 * </p>
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

    /**
     * Progressive download support
     * <p>
     * This is the final achievement
     * Allows mini request of 1M, instead of sending to whole file
     * <p>
     * Full support for heading and streaming profileService allowing jumps into specific times for any browser
     * Translates info.magnolia.dam.core.download.DamDownloadServlet to Spring 5
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