package com.albertosoto.mgnl.rd2019.spring.controller;

import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.core.download.DamDownloadServlet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;

import java.time.ZonedDateTime;
import java.util.function.Function;

/**
 * com.albertosoto.mgnl.rd2019.spring.controller
 * Class AbstractStreamingController
 * 14/05/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public abstract class AbstractStreamingController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected final String EXAMPLE_JCR_URI = "/dam-static/jcr:639c2e45-c566-42d8-8a0e-437ce172b32d/Dear%20Basketball.mp4";
    protected final String EXAMPLE_FILE_URI = "E:\\src\\java\\magnolia\\mgnl-rd2019-sandbox\\streaming-magnolia\\src\\main\\resources\\media\\big_buck_bunny.mp4";

    protected static final String DOT = ".";
    protected static final String FILE_TMP_PREFIX = "mgnl-";

    protected abstract AssetProviderRegistry getAssetProvider();

    /**
     * Handle pathInfo containing identifier. The following cases are handled:<br>
     * - /dam/jcr:7ecd4045-45a0-4c81-b2b6-f4c4b0cd24ad/<whatever...<br>
     *
     * @return the found {@link Asset}, null otherwise.
     * @see DamDownloadServlet#getAssetBasedOnIdentifier(String)
     */
    protected Asset getAssetBasedOnIdentifier(String pathInfo) {
        // extract all between the first and the second '/'
        String keyStr = pathInfo.split("/")[2];
        // handling of /dam/jcr:7ecd4045-45a0-4c81-b2b6-f4c4b0cd24ad/<whatever...
        if (ItemKey.isValid(keyStr)) {
            final ItemKey itemKey = ItemKey.from(keyStr);
            return getAssetProvider().getProviderFor(itemKey).getAsset(itemKey);
        }
        return null;
    }


    /**
     * Checks resource MediaType or desired
     *
     * @param resource     Spring resource to asset
     * @param mediaTypeTxt default mediaType value. Nullable value
     * @return Default MediaType or OCTET_STREAM if error
     */
    protected MediaType getValidMediaType(Resource resource, String mediaTypeTxt) {
        MediaType mediaType;
        if (StringUtils.isEmpty(mediaTypeTxt)) {
            mediaType = MediaTypeFactory
                    .getMediaType(resource)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);
        } else {
            try {
                mediaType = MediaType.parseMediaType(mediaTypeTxt);
            } catch (Exception e) {
                mediaType = MediaTypeFactory
                        .getMediaType(mediaTypeTxt)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM);
            }
        }
        return mediaType;
    }


    /**
     * Tricky way of returning a valid partial content in any way
     *
     * @param resource     Resource to return
     * @param mediaTypeTxt default mediaType. Nullable
     * @return Valid responseEntity for partial rendering
     */
    protected ResponseEntity<Resource> getValidResponse(Resource resource, String mediaTypeTxt
            , Long fileSize
            , HttpHeaders httpHeaders) {
        try {
            if (resource != null) {
                ResourceRegion region = getResourceRegion(resource, fileSize, httpHeaders);
                String contentRangeValue = String.format("bytes %s-%s/%s", region.getPosition(), region.getCount(), fileSize);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(getValidMediaType(resource, mediaTypeTxt))
                        //.header(HttpHeaders.CONTENT_RANGE, contentRangeValue)//
                        //.header(HttpHeaders.CONTENT_LENGTH, String.valueOf(region.getCount()))//String.valueOf(r.length)
                        //.contentLength(size)
                        //.headers(httpHeaders)
                        .lastModified(ZonedDateTime.now())
                        .body(resource);
                //.body(region.getResource());
            } else {
                //if reach point, is not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new FileUrlResource(StringUtils.EMPTY));
            }
        } catch (Exception e) {
            log.error("valid Response credential", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
        }
    }

    /**
     * Tricky way of returning a valid partial content in any way
     *
     * @param resource     Resource to return
     * @param mediaTypeTxt default mediaType. Nullable
     * @return Valid responseEntity for partial rendering
     */
    protected ResponseEntity<ResourceRegion> getValidResponseRegion(Resource resource
            , String mediaTypeTxt
            , Long size
            , HttpHeaders httpHeaders) {
        try {
            if (resource != null) {
                ResourceRegion region = getResourceRegion(resource, size, httpHeaders);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(getValidMediaType(resource, mediaTypeTxt))
                        .contentLength(size)
                        //.header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                        .lastModified(ZonedDateTime.now())
                        .body(region);
            } else {
                //if reach point, is not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            log.error("valid Response credential", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
        }
    }

    /**
     * Functional for service process with partial content
     * https://melgenek.github.io/spring-video-service
     *
     * @param file fileName or id
     * @param f    Function to solve
     * @return valid response entity
     */
    protected ResponseEntity<Resource> streamResource(String file, String mediaType, Long size, HttpHeaders httpHeaders, Function<String, Resource> f) {
        ResponseEntity<Resource> rtn = ResponseEntity.status(HttpStatus.CREATED).body(null);
        try {
            Resource aux = f.apply(file);
            return getValidResponse(aux, mediaType, size, httpHeaders);
        } catch (Exception e) {
            // re-throw exception only if it's not a partial content response, else logs would fill up with (harmless)
            // broken pipe exceptions on some app servers (e.g. Tomcat)
            if (rtn.getStatusCode() != HttpStatus.PARTIAL_CONTENT) {
                throw e;
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
            }
        }
    }


    /**
     * Based on
     * https://melgenek.github.io/spring-video-service
     *
     * @param video
     * @param headers
     * @return
     */
    protected ResourceRegion getResourceRegion(Resource video, Long fileSize, HttpHeaders headers) {
        try {
            final long blockValue = 5 * 1024 * 1024;//1 * 1024 * 1024;
            //Long contentLength = 1L;
            if (!headers.getRange().isEmpty()) {
                HttpRange range = headers.getRange().get(0);//headers.range.firstOrNull();
                Long start = range.getRangeStart(fileSize);
                Long end = range.getRangeEnd(fileSize);
                if (start >= fileSize || start > end) {
                    //bug?
                    start = 0L;
                }
                Long rangeLength = Math.min(blockValue, end - start + 1);
                return new ResourceRegion(video, start, rangeLength);
            } else {
                Long rangeLength = Math.min(blockValue, fileSize);
                return new ResourceRegion(video, 0, rangeLength);
            }
        } catch (Exception e) {
            log.error("Calc region", e);
        }
        return null;
    }

}
