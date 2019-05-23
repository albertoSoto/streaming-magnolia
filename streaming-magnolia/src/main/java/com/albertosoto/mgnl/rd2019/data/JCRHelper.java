package com.albertosoto.mgnl.rd2019.data;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.ui.framework.message.Node2MapUtil;
import org.apache.commons.beanutils.BeanUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * com.albertosoto.mgnl.rd2019.data
 * Class JCRHelper
 * 20/05/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class JCRHelper {

    /**
     * Manages a JCR query in the most simply way
     *
     * @param repo  desired repo
     * @param query jcr2 query
     * @return query result
     * @throws Exception should be cath
     */
    public static QueryResult doQuery(String repo, String query) throws Exception {
        Session jcrSession = MgnlContext.getJCRSession(repo);
        QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();
        return jcrQueryManager.createQuery(query, Query.JCR_SQL2).execute();
    }

    /**
     * Populates jcr properties into bean container
     *
     * @param item bean
     * @param node jcrNode
     * @param <T>  type
     * @return replaced item with jcr properties
     */
    public static <T extends IdentifiedJCRItem> T getBean(T item, Node node) throws Node2BeanException, RepositoryException, InvocationTargetException, IllegalAccessException {
        Map<String, Object> aux = Node2MapUtil.node2map(node);
        BeanUtils.populate(item, aux);
        item.setId(node.getIdentifier());
        return item;
    }

}
