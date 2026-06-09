package top.ortus.lightmark.backend.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * 分页响应包装类，用于封装分页数据
 * @param <T> 数据项的类型
 */
public class PageResponse<T> {
    private long total;
    private int page;
    private int size;
    private List<T> list;

    public PageResponse() {
    }

    public PageResponse(long total, int page, int size, List<T> list) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.list = list;
    }

    public PageResponse(long total, List<T> list) {
        this(total, 1, list == null ? 0 : list.size(), list);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    /**
     * Backward-compatible alias.
     */
    @JsonIgnore
    public List<T> getItems() {
        return list;
    }

    /**
     * Backward-compatible alias.
     */
    public void setItems(List<T> items) {
        this.list = items;
    }
}