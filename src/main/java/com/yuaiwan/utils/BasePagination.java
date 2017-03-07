package com.yuaiwan.utils;
 
import java.util.List;

/**
 * 分页的处理类 
 * @author guangyu
 */
@SuppressWarnings("serial")
public abstract class BasePagination<T> implements java.io.Serializable {
	protected int pageNum;
	protected int pageSize;
	protected int totalPage;
	protected int totalCount;
	protected List<T> results;
	protected int halfPageShow = 2;
	protected int pageShow = 5;
	
	public BasePagination() {}
	
	public BasePagination(int pageNum, int pageSize, int totalCount, List<T> list) {
		if(pageNum == 0){
			pageNum = 1;
		}
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.totalCount = totalCount;
		this.results = list;
		if(pageSize==0){
			pageSize = totalCount;
		}
		if (totalCount % pageSize == 0) {
			this.totalPage = totalCount / pageSize;
		} else {
			this.totalPage = totalCount / pageSize + 1;
		}
	}
	
	public int getHalfPageShow() {
		return halfPageShow;
	}
	
	public void setHalfPageShow(int halfPageShow) {
		pageShow = halfPageShow * 2 + 1;
		this.halfPageShow = halfPageShow;
	}
	

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		if(pageNum==0){
			pageNum = 1;
		}
		this.pageNum = pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		if (totalCount % pageSize == 0) {
			this.totalPage = totalCount / pageSize;
		} else {
			this.totalPage = totalCount / pageSize + 1;
		}
		this.totalCount = totalCount;
	}

	public void getTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getTotalPage() {
		return totalPage;
	}

	/**
	 * 是否有上一页
	 * @return
	 */
	public boolean hasPreviousPage(){
		return pageNum>1;
	}

	/**
	 * 是否有下一页
	 * @return
	 */
	public boolean hasNextPage(){
		return pageNum<totalPage;
	}

	/**
	 * @return
	 */
	public int getPreviousPage(){
		if(hasPreviousPage()){
			return pageNum - 1;
		}
		return 1;
	}
	
	/**
	 * @return
	 */
	public int getNextPage(){
		if(hasNextPage()){
			return pageNum+1;
		}
		return totalPage ;
	}
  
    public abstract String getPages();
}
