package com.albertosoto.mgnl.rd2019.spring.controller;

import com.albertosoto.mgnl.rd2019.data.JCRHelper;
import com.albertosoto.mgnl.rd2019.data.StreamingConfig;
import info.magnolia.cms.security.JCRSessionOp;
import info.magnolia.context.MgnlContext;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.core.download.DamDownloadServlet;
import info.magnolia.repository.RepositoryConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * com.albertosoto.mgnl.rd2019.spring.controller
 * Class AbstractStreamingController
 * 14/05/2019
 *
 * @author berto (alberto.soto@gmail.com)
 * <p>
 * Implements reusable methods for Spring controllers through Magnolia
 */
public abstract class AbstractStreamingController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Integer CONST_1MB = 1024 * 1024;
    private final Integer CONST_ERROR_SIZE = 5;
    private StreamingConfig streamingConfig = null;
    final String EXAMPLE_JCR_URI = "/dam-static/jcr:639c2e45-c566-42d8-8a0e-437ce172b32d/Dear%20Basketball.mp4";
    final String EXAMPLE_FILE_URI = "E:\\src\\java\\magnolia\\mgnl-rd2019-sandbox\\streaming-magnolia\\src\\main\\resources\\media\\big_buck_bunny.mp4";
    private static final String CONFIG_MODULE_PATH = "/modules/streaming-magnolia/config/streamingParameters";

    /**
     * Checks on init the configuration
     */
    AbstractStreamingController() {
        super();
        Node node = getConfigNode();
        if (node != null) {
            StreamingConfig aux = new StreamingConfig();
            try {
                streamingConfig = JCRHelper.getBean(aux, node);
            } catch (Exception e) {
                log.error("on module config", e);
            }
        } else {
            streamingConfig = new StreamingConfig();
        }
    }

    /**
     * Sets system chunk stream size
     *
     * @return number of MB for response
     */
    protected Integer getChunkSize() {
        if (streamingConfig.getChunkSize() != null && streamingConfig.getChunkSize() > 0) {
            return streamingConfig.getChunkSize() * CONST_1MB;
        } else {
            return CONST_ERROR_SIZE * CONST_1MB;
        }
    }

    /**
     * JCR Mapped config on start up
     * @return Streaming Config
     */
    protected StreamingConfig getStreamingConfig() {
        return this.streamingConfig;
    }

    /**
     * Returns a valid response entity for Spring context
     *
     * @param resource     Any resource
     * @param mediaTypeTxt default mediaType
     * @return Server response
     */
    ResponseEntity<Resource> getValidResponse(Resource resource, String mediaTypeTxt) {
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
     * Returns the config node
     *
     * @return Streamingconfig node or null
     */
    private Node getConfigNode() {
        try {
            return MgnlContext.doInSystemContext(new JCRSessionOp<Node>(RepositoryConstants.CONFIG) {
                @Override
                public Node exec(final Session session) throws RepositoryException {
                    return session.getNode(CONFIG_MODULE_PATH);
                }
            });
        } catch (RepositoryException e) {
            log.debug("Unable to obtain site node");
        }
        return null;
    }

    protected abstract AssetProviderRegistry getAssetProvider();

    /**
     * Handle pathInfo containing identifier. The following cases are handled:<br>
     * extract all between the first and the second '/'
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
     * <p>
     * Based on https://melgenek.github.io/spring-video-service
     *
     * @param video    resource File from bytearray or asset
     * @param fileSize asset file size
     * @param headers  request headers
     * @return resourceRegion with file bounds
     */
    ResourceRegion getResourceRegion(Resource video, Long fileSize, HttpHeaders headers) {
        try {
            final long blockValue = getChunkSize();
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
