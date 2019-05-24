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

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    final String EXAMPLE_JCR_URI = "/dam-static/jcr:639c2e45-c566-42d8-8a0e-437ce172b32d/Dear%20Basketball.mp4";
    final String EXAMPLE_FILE_URI = "E:\\src\\java\\magnolia\\mgnl-rd2019-sandbox\\streaming-magnolia\\src\\main\\resources\\media\\big_buck_bunny.mp4";

    protected abstract AssetProviderRegistry getAssetProvider();

    /**
     * Handle pathInfo containing identifier. The following cases are handled:<br>
     *     extract all between the first and the second '/'
     * - /dam/jcr:7ecd4045-45a0-4c81-b2b6-f4c4b0cd24ad/<whatever...<br>
     *
     * @return the found {@link Asset}, null otherwise.
     * @see DamDownloadServlet#getAssetBasedOnIdentifier(String)
     */
    Asset getAssetBasedOnIdentifier(String pathInfo) {
        String keyStr = pathInfo.split("/")[2];
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
    MediaType getValidMediaType(Resource resource, String mediaTypeTxt) {
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
     * Creates a proper ResourceRegion from header request data for a file
     *
     * Based on https://melgenek.github.io/spring-video-service
     *
     * @param video resource File from bytearray or asset
     * @param fileSize asset file size
     * @param headers request headers
     * @return resourceRegion with file bounds
     */
    ResourceRegion getResourceRegion(Resource video, Long fileSize, HttpHeaders headers) {
        try {
            final long blockValue = 5 * 1024 * 1024;
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
