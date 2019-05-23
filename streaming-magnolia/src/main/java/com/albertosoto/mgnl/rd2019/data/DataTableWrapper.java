package com.albertosoto.mgnl.rd2019.data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * com.albertosoto.mgnl.rd2019.data
 * Created by Alberto Soto Fernandez in 19/05/2019.
 * Description:
 * Turns JSON response from spring rest into something more usable
 */
public class DataTableWrapper<T> {
    List<T> data;


    public DataTableWrapper() {
        this(new ArrayList<T>());
    }

    public DataTableWrapper(List<T> t) {
        setData(t);
    }

    /**
     * @return the persons
     */
    public List<T> getData() {
        return data;
    }

    /**
     * @param persons the persons to set
     */
    public void setData(List<T> persons) {
        this.data = persons;
    }
}
