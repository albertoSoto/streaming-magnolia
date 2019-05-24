package com.albertosoto.mgnl.rd2019.restpoints.v1;

import com.albertosoto.mgnl.rd2019.data.DataTableWrapper;
import com.albertosoto.mgnl.rd2019.data.JCRHelper;
import com.albertosoto.mgnl.rd2019.data.MediaItem;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.jcr.DamConstants;
import info.magnolia.dam.templating.functions.DamTemplatingFunctions;
import info.magnolia.imaging.functions.ImagingTemplatingFunctions;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.resourceloader.jcr.JcrResourceOrigin;
import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.EndpointDefinition;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.QueryResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * com.albertosoto.mgnl.rd2019.restpoints.v1
 * Class BrowseMediaEndPoint
 * By berto. 08/05/2019
 * <p>
 * Dummy hello world example for magnolia cms rest end points
 * <p>
 * https://documentation.magnolia-cms.com/display/DOCS56/How+to+create+a+custom+Java-based+REST+endpoint
 * https://documentation.magnolia-cms.com/display/DOCS56/REST+module
 * <p>
 * info.magnolia.rest.delivery.jcr.v2.JcrDeliveryEndpoint
 */
@Api(value = "/streaming/v1")
@Path("/streaming/v1")
public class BrowseMediaEndPoint<D extends EndpointDefinition> extends AbstractEndpoint<D> {
    private static final String REPO = "mediaCenter";
    private  DamTemplatingFunctions dam;
    private final Logger log = LoggerFactory.getLogger(this.getClass());


    public BrowseMediaEndPoint(D endpointDefinition, DamTemplatingFunctions dam) {
        super(endpointDefinition);
        this.dam = dam;
    }

    @Path("/getAll")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAll() {
        try {
            DataTableWrapper<MediaItem> response = new DataTableWrapper<>();
            List<MediaItem> items = new ArrayList<>();
            QueryResult result = JCRHelper.doQuery(REPO, "select * from [lib:media]");
            NodeIterator nodeIterator = result.getNodes();
            nodeIterator.forEachRemaining(node -> {
                try {
                    MediaItem item = new MediaItem();
                    item = JCRHelper.getBean(item, (Node) node);
                    if (StringUtils.isNotEmpty(item.getImage())) {
                        Asset asset = dam.getAsset(item.getImage());
                        //check!!! returns default
                        String link = dam.getRendition(asset,"thumbnail").getLink();
                        item.setImageLink(link);
                    }
                    if (StringUtils.isNotEmpty(item.getVideo())){
                        Asset asset = dam.getAsset(item.getVideo());
                        String link = String.format(".spring/streaming?fileRQ=%s",asset.getLink());
                        item.setVideoLink(link);
                        item.setVideoType(asset.getMimeType());
                    }
                    items.add(item);
                } catch (Exception e) {
                    log.error("on convertion", e);
                }
            });
            response.setData(items);
            return Response.ok(response).build();
        } catch (Exception e) {
            log.error("on query ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}